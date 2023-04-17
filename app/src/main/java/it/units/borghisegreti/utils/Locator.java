package it.units.borghisegreti.utils;

import static androidx.lifecycle.Lifecycle.State.STARTED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Collections;
import java.util.List;

import it.units.borghisegreti.models.Experience;

public class Locator implements DefaultLifecycleObserver {

    private boolean hasStarted = false;
    @NonNull
    private final Context context;
    @NonNull
    private final Lifecycle lifecycle;
    private static final int MAX_DELAY_MILLIS = 500;
    private static final int INTERVAL_MILLIS = 200;
    public static final String LOCATOR_TAG = "LOCATOR";
    @Nullable
    private String objectiveExperienceId;
    @NonNull
    private List<Experience> experiences = Collections.emptyList();
    @NonNull
    private final LocationListener locationListener;
    private FusedLocationProviderClient locationProviderClient;

    public Locator(@NonNull Context context, @NonNull Lifecycle lifecycle, @NonNull Callback callback) {
        this.context = context;
        this.lifecycle = lifecycle;

        locationListener = location -> {
            if (isObjectiveInRange(location)) {
                callback.onObjectiveInRange();
            } else {
                callback.onObjectiveOutOfRange();
            }
        };
    }

    public void start() {
        Log.d(LOCATOR_TAG, "Inside start() method now");
        if (lifecycle.getCurrentState().isAtLeast(STARTED) && !hasStarted) {
            startRequestingLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startRequestingLocationUpdates() {
        hasStarted = true;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MILLIS);
        builder.setMaxUpdateDelayMillis(MAX_DELAY_MILLIS);
        LocationRequest locationRequest = builder.build();
        locationProviderClient.requestLocationUpdates(locationRequest, locationListener, context.getMainLooper())
                .addOnSuccessListener(task -> Log.d(LOCATOR_TAG, "location updates successfully requested"))
                .addOnFailureListener(exception -> Log.e(LOCATOR_TAG, "Error while requesting location updates", exception));
    }

    private boolean isObjectiveInRange(Location location) {
        Experience objectiveExperience = null;
        for (Experience experience : experiences) {
            if (experience.getId().equals(objectiveExperienceId)) {
                objectiveExperience = experience;
                break;
            }
        }
        double distanceBetweenPoints = Double.MAX_VALUE;
        if (objectiveExperience != null) {
            distanceBetweenPoints = computeDistanceBetweenPoints(objectiveExperience.getLatitude(),
                    objectiveExperience.getLongitude(), location.getLatitude(), location.getLongitude());
            Log.d(LOCATOR_TAG, "Distance from the objective is: " + distanceBetweenPoints);
        } else {
            Log.w(LOCATOR_TAG, "Unable to find objective among the given experiences");
        }
        return distanceBetweenPoints < 50;
    }

    private double computeDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(degreeToRadians(lat1)) * Math.sin(degreeToRadians(lat2)) + Math.cos(degreeToRadians(lat1)) * Math.cos(degreeToRadians(lat2)) * Math.cos(degreeToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegree(dist);
        dist = dist * 60 * 1.1515 * 1609.344;
        return dist;
    }

    private double degreeToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double radiansToDegree(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (!hasStarted) {
            startRequestingLocationUpdates();
        } else {
            Log.d(LOCATOR_TAG, "Locator already started");
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        locationProviderClient.removeLocationUpdates(locationListener)
                .addOnSuccessListener(task -> Log.d(LOCATOR_TAG, "Location updates successfully removed"))
                .addOnFailureListener(exception -> Log.e(LOCATOR_TAG, "Error while removing location updates", exception));
        hasStarted = false;
    }

    public void submitObjectiveId(@NonNull String objectiveExperienceId) {
        this.objectiveExperienceId = objectiveExperienceId;
    }

    public void submitExperiences(@NonNull List<Experience> experiences) {
        this.experiences = experiences;
    }

    public interface Callback {
        void onObjectiveInRange();
        void onObjectiveOutOfRange();
    }
}
