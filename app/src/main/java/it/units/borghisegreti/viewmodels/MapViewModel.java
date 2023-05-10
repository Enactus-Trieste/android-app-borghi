package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.utils.Database.DB_TAG;
import static it.units.borghisegreti.utils.Database.EXPERIENCES_REFERENCE;
import static it.units.borghisegreti.utils.Database.ZONES_REFERENCE;
import static it.units.borghisegreti.utils.Database.COMPLETED_EXPERIENCES_REFERENCE;
import static it.units.borghisegreti.utils.Database.OBJECTIVE_REFERENCE;
import static it.units.borghisegreti.utils.Database.POINTS_REFERENCE;
import static it.units.borghisegreti.utils.Database.USER_DATA_REFERENCE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;


public class MapViewModel extends ViewModel {
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
    private final ValueEventListener userPointsListener;
    @NonNull
    private final ValueEventListener objectiveExperienceListener;
    @NonNull
    private final MutableLiveData<List<Zone>> databaseZones;
    @NonNull
    private final MutableLiveData<List<Experience>> databaseExperiences;
    @NonNull
    private final MutableLiveData<Integer> databaseUserPoints;
    @NonNull
    private final MutableLiveData<String> databaseObjectiveExperienceId;
    @NonNull
    private final SavedStateHandle savedState;
    @NonNull
    private final String userId;

    public MapViewModel(@NonNull FirebaseDatabase database, @NonNull SavedStateHandle savedState) {
        this.savedState = savedState;
        this.database = database;
        databaseZones = new MutableLiveData<>();
        databaseExperiences = new MutableLiveData<>();
        databaseUserPoints = new MutableLiveData<>();
        databaseObjectiveExperienceId = new MutableLiveData<>();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
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
        userPointsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer points = snapshot.getValue(Integer.class);
                databaseUserPoints.setValue(points);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error: " + error.getMessage(), error.toException());
            }
        };
        objectiveExperienceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String objectiveExperienceId = snapshot.getValue(String.class);
                databaseObjectiveExperienceId.setValue(objectiveExperienceId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error: " + error.getMessage(), error.toException());
            }
        };
        database.getReference(ZONES_REFERENCE).addValueEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).addValueEventListener(experiencesListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .addValueEventListener(userPointsListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .addValueEventListener(objectiveExperienceListener);
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

    @NonNull
    public Task<Void> setExperienceAsCompleted(@NonNull Experience experience) {
        experience.setDateOfCompletion(new Date());
        setUserPoints(experience.getPoints() + Objects.requireNonNull(databaseUserPoints.getValue(), "User points should already be initialized")).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(DB_TAG, "Successfully updated user points");
            } else {
                Log.e(DB_TAG, "Error while updating user points");
            }
        });
        Map<String, Object> completedExperienceById = new HashMap<>();
        completedExperienceById.put(experience.getId(), experience);
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .updateChildren(completedExperienceById);
    }

    @NonNull
    private Task<Void> setUserPoints(int points) {
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .setValue(points);
    }

    @NonNull
    public Task<Void> setObjectiveExperience(@Nullable String experienceId) {
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .setValue(experienceId);
    }

    /**
     *
     * @return The objective experience's ID for the current user, or null if the user has no objective experience set
     */
    public LiveData<String> getObjectiveExperienceId() {
        return databaseObjectiveExperienceId;
    }

    @Nullable
    public Double getCameraLatitude() {
        return savedState.get(CAMERA_LATITUDE);
    }

    public void saveCameraLatitude(double latitude) {
        savedState.set(CAMERA_LATITUDE, latitude);
    }

    @Nullable
    public Double getCameraLongitude() {
        return savedState.get(CAMERA_LONGITUDE);
    }

    public void saveCameraLongitude(double longitude) {
        savedState.set(CAMERA_LONGITUDE, longitude);
    }

    @Nullable
    public Float getCameraZoom() {
        return savedState.get(CAMERA_ZOOM);
    }

    public void saveCameraZoom(float zoom) {
        savedState.set(CAMERA_ZOOM, zoom);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(ZONES_REFERENCE).removeEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).removeEventListener(experiencesListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .removeEventListener(userPointsListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .removeEventListener(objectiveExperienceListener);
    }

    public static class Factory extends AbstractSavedStateViewModelFactory {

        @NonNull
        private final FirebaseDatabase database;

        public Factory(@NonNull FirebaseDatabase database) {
            this.database = database;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
            return (T) new MapViewModel(database, handle);
        }
    }
}
