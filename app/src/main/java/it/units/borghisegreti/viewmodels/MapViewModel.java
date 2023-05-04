package it.units.borghisegreti.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;


public class MapViewModel extends ViewModel {
    public static final String DB_URL = "https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String DB_TAG = "FIREBASE_DB";
    public static final String ZONES_REFERENCE = "zones";
    public static final String EXPERIENCES_REFERENCE = "experiences";
    private static final String CAMERA_LATITUDE = "latitude";
    private static final String CAMERA_LONGITUDE = "longitude";
    private static final String CAMERA_ZOOM = "zoom";
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final ValueEventListener experiencesListener;
    @NonNull
    private final ValueEventListener zonesListener;
    @NonNull
    private final MutableLiveData<List<Zone>> databaseZones;
    @NonNull
    private final MutableLiveData<List<Experience>> databaseExperiences;
    @NonNull
    private final SavedStateHandle savedState;

    public MapViewModel(@NonNull SavedStateHandle savedState) {
        this.savedState = savedState;
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseZones = new MutableLiveData<>();
        databaseExperiences = new MutableLiveData<>();
        experiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Experience> experiences = new ArrayList<>();
                for (DataSnapshot experienceSnapshot : snapshot.getChildren()) {
                    Experience databaseExperience = experienceSnapshot.getValue(Experience.class);
                    Log.d(DB_TAG, "Received new remote experience from database: " + databaseExperience);
                    experiences.add(databaseExperience);
                }
                MapViewModel.this.databaseExperiences.setValue(experiences);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error while getting new experiences: " + error.getMessage());
            }
        };
        zonesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Zone> zones = new ArrayList<>();
                for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                    Zone databaseZone = zoneSnapshot.getValue(Zone.class);
                    Log.d(DB_TAG, "Received new remote zone from database: " + databaseZone);
                    zones.add(databaseZone);
                }
                MapViewModel.this.databaseZones.setValue(zones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error while getting new zones: " + error.getMessage());
            }
        };
        database.getReference(ZONES_REFERENCE).addValueEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).addValueEventListener(experiencesListener);
    }

    /**
     *
     * @return All the zones stored in the database
     */
    public LiveData<List<Zone>> getZones() {
        return databaseZones;
    }

    /**
     *
     * @return All the experiences stored in the database
     */
    public LiveData<List<Experience>> getExperiences() {
        return databaseExperiences;
    }

    // example method on how to transform data directly in the ViewModel
    public LiveData<List<Experience>> getExperiencesByName(@NonNull String experienceName) {
        return Transformations.map(databaseExperiences, experiences -> {
            List<Experience> filteredExperiences = new ArrayList<>();
            for (Experience experience : experiences) {
                if (experience.getName().equals(experienceName)) {
                    filteredExperiences.add(experience);
                }
            }
            return filteredExperiences;
        });
    }

    /**
     *
     * @param experienceId - the ID that we're interested in
     * @return The experience associated with the given ID, or null if such an experience isn't found
     */
    public LiveData<Experience> getExperienceById(@NonNull String experienceId) {
        return Transformations.map(databaseExperiences, experiences -> {
            for (Experience experience : experiences) {
                if (experience.getId().equals(experienceId)) {
                    return experience;
                }
            }
            return null;
        });
    }

    @NonNull
    public Task<Void> uploadNewExperience(@NonNull String name,
                                          @NonNull String description,
                                          @NonNull Experience.Type type,
                                          @NonNull LatLng coordinates,
                                          int points) {
        DatabaseReference experienceReference = database.getReference(EXPERIENCES_REFERENCE).push();
        Experience experience = new Experience(Objects.requireNonNull(experienceReference.getKey(), "New experience reference should never point at database root"),
                name,
                description,
                type,
                coordinates,
                points);
        return experienceReference.setValue(experience);
    }

    public LiveData<Double> getCameraLatitude() {
        return savedState.getLiveData(CAMERA_LATITUDE);
    }

    public void saveCameraLatitude(double latitude) {
        savedState.set(CAMERA_LATITUDE, latitude);
    }

    public LiveData<Double> getCameraLongitude() {
        return savedState.getLiveData(CAMERA_LONGITUDE);
    }

    public void saveCameraLongitude(double longitude) {
        savedState.set(CAMERA_LONGITUDE, longitude);
    }

    public LiveData<Float> getCameraZoom() {
        return savedState.getLiveData(CAMERA_ZOOM);
    }

    public void saveCameraZoom(float zoom) {
        savedState.set(CAMERA_ZOOM, zoom);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(ZONES_REFERENCE).removeEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).removeEventListener(experiencesListener);
    }
}
