package it.units.borghisegreti.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import it.units.borghisegreti.R;
import it.units.borghisegreti.activities.MapsActivity;
import it.units.borghisegreti.interfaces.OnObjectiveInRangeEventListener;
import it.units.borghisegreti.interfaces.OnUserLocationUpdateListener;
import it.units.borghisegreti.models.Experience;


public class Locator {

    private static final int RANGE = 50;
    private final MapsActivity callingActivity;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private OnUserLocationUpdateListener locationUpdateListener;
    private OnObjectiveInRangeEventListener objectiveInRangeListener;

    public Locator(MapsActivity activity, boolean testMode) {
        this.callingActivity = activity;
        if (testMode) {
            Location overriddenUserLocation = new Location("");
            overriddenUserLocation.setLongitude(0);
            overriddenUserLocation.setLatitude(0);
            userLocation = overriddenUserLocation;
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(callingActivity);
            getLastLocation();
        }
    }

    public void addOnUserLocationUpdateEventListener(OnUserLocationUpdateListener listener) {
        this.locationUpdateListener = listener;
    }

    public void addOnObjectiveInRangeEventListener(OnObjectiveInRangeEventListener listener) {
        this.objectiveInRangeListener = listener;
    }

    public boolean isTheDistanceBetweenUserAndObjectiveLessThan50Meters() {
        Experience objectiveExperience = callingActivity.getObjectiveExperience();
        double distanceBetweenPoints = 500;
        if (objectiveExperience != null) {
            distanceBetweenPoints = computeDistanceBetweenPoints(objectiveExperience.getCoordinates().latitude,
                    objectiveExperience.getCoordinates().longitude, userLocation.getLatitude(), userLocation.getLongitude());
        }
        Log.d("AM_LOCATION", "distance from obj = " + distanceBetweenPoints);
        return distanceBetweenPoints < RANGE;
    }

    public void updateUserLocation(Location updatedUserLocation) {
        userLocation = updatedUserLocation;
        if (locationUpdateListener != null) {
            locationUpdateListener.onUserLocationUpdate(userLocation);
        }
        if (objectiveInRangeListener != null) {
            if (isTheDistanceBetweenUserAndObjectiveLessThan50Meters()) {
                objectiveInRangeListener.onObjectiveInRange();
            } else {
                objectiveInRangeListener.onObjectiveOutOfRange();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {

            if (isLocationEnabled()) {

                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        updateUserLocation(location);
                    }
                    requestNewLocationData();
                });
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(callingActivity);
                alertDialogBuilder.setMessage(R.string.activate_location_message)
                        .setTitle(R.string.activate_location_title);

                alertDialogBuilder.setPositiveButton(R.string.activate_location_positive_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                callingActivity.startActivity(intent);
                            }
                        });
                alertDialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                            }
                        });

                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
            }
        } else {
            callingActivity.requestLocalizationPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5);
        builder.setMaxUpdateDelayMillis(0);
        LocationRequest locationRequest = builder.build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(callingActivity);
        fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            assert mLastLocation != null;
            updateUserLocation(mLastLocation);
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) callingActivity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void permissionGranted() {
        getLastLocation();
    }


    private double computeDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(degreeToRadians(lat1)) * Math.sin(degreeToRadians(lat2)) + Math.cos(degreeToRadians(lat1)) * Math.cos(degreeToRadians(lat2)) * Math.cos(degreeToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegree(dist);
        dist = dist * 60 * 1.1515 * 1609.344;
        return (dist);
    }

    private double degreeToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double radiansToDegree(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
