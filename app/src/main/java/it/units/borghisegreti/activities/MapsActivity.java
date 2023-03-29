package it.units.borghisegreti.activities;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import it.units.borghisegreti.FirebaseDatabase;
import it.units.borghisegreti.R;
import it.units.borghisegreti.fragments.BottomSheetFragment;
import it.units.borghisegreti.interfaces.DataEventListener;
import it.units.borghisegreti.interfaces.Database;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.utils.Locator;
import it.units.borghisegreti.interfaces.OnObjectiveInRangeEventListener;
import it.units.borghisegreti.interfaces.OnUserLocationUpdateListener;
import it.units.borghisegreti.utils.IconBuilder;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private static final int PERMISSION_ID = 44;
    private static final LatLng startingCameraPoint = new LatLng(45.879688, 13.564337);
    private static final String TAG = "AM_LOCATION";
    private GoogleMap mMap;
    private Marker userLocationMarker;
    private boolean isTestModeEnabled;
    private boolean isMapReady = false;
    private ArrayList<Experience> experiences;
    private ArrayList<Zone> zones;
    private final Map<Marker, Experience> drawnMarkerExperienceMap = new HashMap<>();
    private final Map<Marker, Zone> drawnMarkerZoneMap = new HashMap<>();
    private boolean allMarkersAreSet = false;
    private Locator locator;
    private Database database;
    private Experience objectiveExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isTestModeEnabled = sharedPreferences.getBoolean(getString(R.string.test_key), false);
        Log.d(TAG, "TestMode = " + isTestModeEnabled);

        try {
            database = new FirebaseDatabase(FirebaseAuth.getInstance().getCurrentUser());
            database.addDataEventListener(new DataEventListener() {
                @Override
                public void onDataAvailable(ArrayList<Experience> experiencesLoaded, ArrayList<Zone> zonesLoaded) {
                    experiences = experiencesLoaded;
                    zones = zonesLoaded;
                    drawMarkers();
                }
                @Override
                public void onStatusExperiencesChanged(ArrayList<Experience> changedExperiences) {
                    if (allMarkersAreSet) {
                        boolean thereIsAnObjectiveExperience = false;
                        for (Experience experience : changedExperiences) {
                            drawExperienceMarker(experience);
                            if (experience.getIsTheObjective()) {
                                objectiveExperience = experience;
                                thereIsAnObjectiveExperience = true;
                            }
                        }
                        if (!thereIsAnObjectiveExperience) {
                            objectiveExperience = null;
                        }
                    }
                }
                @Override
                public void onPointsUpdated(int points) {}
            });
        } catch (FirebaseDatabase.NullUserException e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        locator = new Locator(this, isTestModeEnabled);
        locator.addOnUserLocationUpdateEventListener(new OnUserLocationUpdateListener() {
            @Override
            public void onUserLocationUpdate(Location userLocation) {
                drawUserLocationMarker(userLocation);
            }
        });
        Activity thisActivity = this;
        locator.addOnObjectiveInRangeEventListener(new OnObjectiveInRangeEventListener() {
            final Button completeExperienceButton = findViewById(R.id.complete_exp_button);
            @Override
            public void onObjectiveInRange() {
                Log.d(TAG, "objective in range");
                completeExperienceButton.setVisibility(View.VISIBLE);
                completeExperienceButton.setOnClickListener(view -> {
                    database.setExperienceAsCompletedForUser(objectiveExperience);
                    database.setObjectiveExperienceOfUser(null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                    builder.setMessage(String.format(Locale.getDefault(), getString(R.string.gained_points_alert), objectiveExperience.getPoints()))
                            .setTitle(R.string.experience_completed);

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(thisActivity, getString(R.string.next_objective_toast), Toast.LENGTH_SHORT).show();
                        }
                    });
                    objectiveExperience = null;
                    completeExperienceButton.setVisibility(View.GONE);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }

            @Override
            public void onObjectiveOutOfRange() {
                completeExperienceButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        isMapReady = true;
        mMap.setOnMarkerClickListener(this);
        mMap.setMinZoomPreference(7f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingCameraPoint, 8f));
        drawMarkers();

        if (isTestModeEnabled) {
            mMap.setOnMapLongClickListener(this);
        }

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                drawMarkers();
            }
        });
    }

    private void drawMarkers() {
        if (isMapReady && experiences != null && zones != null) {
            if (mMap.getCameraPosition().zoom < 12) {
                drawAllZoneMarkers();
            } else if (mMap.getCameraPosition().zoom >= 12) {
                drawAllExperienceMarkers();
            }
        }
    }

    private void drawAllZoneMarkers() {
        for (Marker marker : drawnMarkerExperienceMap.keySet()) {
            marker.remove();
        }
        drawnMarkerExperienceMap.clear();

        if (zones != null) {
            for (Zone zone : zones) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(zone.getCoordinates())
                        .title(zone.getName())
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromAsset("icons/BorgoIcon.png")));
                drawnMarkerZoneMap.put(marker, zone);
            }
        }
    }

    private void drawAllExperienceMarkers() {
        for (Marker marker : drawnMarkerZoneMap.keySet()) {
            marker.remove();
        }
        if (experiences != null) {
            for (Experience experience : experiences) {
                drawExperienceMarker(experience);
                if (experience.getIsTheObjective()) {
                    objectiveExperience = experience;
                    Objects.requireNonNull(findMarkerAssociatedToExperience(objectiveExperience)).setZIndex(1f);
                }
            }
            allMarkersAreSet = true;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {

        Zone zone = drawnMarkerZoneMap.get(marker);
        Experience experience = drawnMarkerExperienceMap.get(marker);
        if (zone != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12f));
        } else if (experience != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    BottomSheetFragment blankFragment = new BottomSheetFragment(experience, database);
                    blankFragment.show(getSupportFragmentManager(), blankFragment.getTag());
                }
                @Override
                public void onCancel() {}
            });
        }
        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Location overriddenUserLocation = new Location("");
        overriddenUserLocation.setLatitude(point.latitude);
        overriddenUserLocation.setLongitude(point.longitude);
        locator.updateUserLocation(overriddenUserLocation);
    }

    public void drawExperienceMarker(Experience experience) {
        Marker marker = findMarkerAssociatedToExperience(experience);
        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(experience.getCoordinates())
                    .title(experience.getName())
                    .snippet(experience.getDescription()));
            drawnMarkerExperienceMap.put(marker, experience);
        }
        IconBuilder markerBuilder = new IconBuilder(this, experience);
        Objects.requireNonNull(marker).setIcon(markerBuilder.buildMarkerDescriptor());
        Objects.requireNonNull(marker).setAlpha(markerBuilder.getMarkerAlpha());
    }

    private Marker findMarkerAssociatedToExperience(Experience experience) {
        for (Marker marker : drawnMarkerExperienceMap.keySet()) {
            Experience drawnExperience = drawnMarkerExperienceMap.get(marker);
            if (drawnExperience == experience) {
                return marker;
            }
        }
        return null;
    }

    private void drawUserLocationMarker(Location userLocation) {
        if (isMapReady) {
            LatLng userCoordinates = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            if (userLocationMarker != null) {
                userLocationMarker.setPosition(userCoordinates);
            } else {
                userLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(userCoordinates)
                        .title("User Location")
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromAsset("markers/UserIcon.png")));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(userCoordinates));
            }
        }
    }

    public void requestLocalizationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locator.permissionGranted();
            }
        }
    }

    public Experience getObjectiveExperience() {
        return objectiveExperience;
    }
}