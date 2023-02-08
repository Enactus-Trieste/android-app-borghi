package it.units.adventuremaps.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.units.adventuremaps.interfaces.DataEventListener;
import it.units.adventuremaps.interfaces.DatabaseConnector;
import it.units.adventuremaps.models.Experience;
import it.units.adventuremaps.FirebaseDatabaseConnector;
import it.units.adventuremaps.utils.Locator;
import it.units.adventuremaps.interfaces.OnObjectiveCompletedEventListener;
import it.units.adventuremaps.interfaces.OnUserLocationUpdateListener;
import it.units.adventuremaps.R;
import it.units.adventuremaps.activities.MainActivity;
import it.units.adventuremaps.utils.MarkerIconBuilder;


public class MapsActivityFragment extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private static final int PERMISSION_ID = 44;
    private static final String TAG = "AM_LOCATION";
    private GoogleMap mMap;
    private Marker userLocationMarker;
    private boolean isTestModeEnabled;
    private boolean isMapReady = false;
    private ArrayList<Experience> experiences;
    private final Map<Marker, Experience> drawnMarkerExperienceMap = new HashMap<>();
    private boolean allMarkersAreSet = false;
    private Locator locator;
    private DatabaseConnector databaseConnector;
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


        databaseConnector = new FirebaseDatabaseConnector(FirebaseAuth.getInstance().getCurrentUser());
        databaseConnector.addDataEventListener(new DataEventListener() {
            @Override
            public void onDataAvailable(ArrayList<Experience> experiencesLoaded) {
                experiences = experiencesLoaded;
                drawAllMarkerExperiences();
            }
            @Override
            public void onStatusExperiencesChanged(ArrayList<Experience> changedExperiences) {
                if (allMarkersAreSet) {
                    for (Experience experience : changedExperiences) {
                        drawExperienceMarker(experience);
                    }
                }
            }
            @Override
            public void onPointsUpdated(int points) {}
        });
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
        locator.addOnObjectiveCompletedEventListener(new OnObjectiveCompletedEventListener() {
            @Override
            public void onObjectiveCompleted() {
                Log.d(TAG, "Objective completed!");
                databaseConnector.setExperienceAsCompletedForUser(objectiveExperience);
                databaseConnector.setObjectiveExperienceOfUser(null);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        isMapReady = true;
        mMap.setOnMarkerClickListener(this);

        if (isTestModeEnabled) {
            mMap.setOnMapLongClickListener(this);
        }

        drawAllMarkerExperiences();
    }

    private void drawAllMarkerExperiences() {
        if (isMapReady && experiences != null) {
            for (Experience experience : experiences) {
                drawExperienceMarker(experience);
                if (experience.getIsTheObjective()) {
                    objectiveExperience = experience;
                }
            }
            allMarkersAreSet = true;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
        Experience experience = drawnMarkerExperienceMap.get(marker);
        BottomSheetFragment blankFragment = new BottomSheetFragment(experience, databaseConnector);

        blankFragment.show(getSupportFragmentManager(),blankFragment.getTag());
        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Location overriddenUserLocation = new Location("");
        overriddenUserLocation.setLatitude(point.latitude);
        overriddenUserLocation.setLongitude(point.longitude);
        locator.overrideUserLocation(overriddenUserLocation);
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
        MarkerIconBuilder markerBuilder = new MarkerIconBuilder(experience);
        Objects.requireNonNull(marker).setIcon(markerBuilder.buildDescriptor());
    }

    private BitmapDescriptor buildDescriptor(Experience experience) {
        BitmapDescriptor descriptor;

        if (experience.getIsTheObjective()) {
            objectiveExperience = experience;
            descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        } else if (experience.getIsCompletedByUser()) {
            descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
        } else {
            descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        }
        return descriptor;
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
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userCoordinates));
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
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    public Experience getObjectiveExperience() {
        return objectiveExperience;
    }
}