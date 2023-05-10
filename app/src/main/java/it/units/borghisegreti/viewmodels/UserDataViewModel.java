package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_TAG;
import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;

public class UserDataViewModel extends ViewModel {
    public static final String USER_DATA_REFERENCE = "user_data";
    public static final String OBJECTIVE_REFERENCE = "objective";
    public static final String COMPLETED_EXPERIENCES_REFERENCE = "completed_experiences";
    public static final String POINTS_REFERENCE = "points";
    @NonNull
    private final MutableLiveData<Map<String, Experience>> databaseCompletedExperiencesById;
    @NonNull
    private final MutableLiveData<String> databaseObjectiveExperienceId;
    @NonNull
    private final MutableLiveData<Integer> databaseUserPoints;
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final String userId;
    @NonNull
    private final ValueEventListener completedExperiencesListener;
    @NonNull
    private final ValueEventListener objectiveExperienceListener;
    @NonNull
    private final ValueEventListener userPointsListener;

    public UserDataViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseCompletedExperiencesById = new MutableLiveData<>();
        databaseObjectiveExperienceId = new MutableLiveData<>();
        databaseUserPoints = new MutableLiveData<>();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
        completedExperiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Experience> completedExperiencesById = new HashMap<>();
                for (DataSnapshot completedExperienceSnapshot : snapshot.getChildren()) {
                    String experienceId = completedExperienceSnapshot.getKey();
                    Experience completedExperience = completedExperienceSnapshot.getValue(Experience.class);
                    completedExperiencesById.put(experienceId, completedExperience);
                }
                databaseCompletedExperiencesById.setValue(completedExperiencesById);
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
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .addValueEventListener(completedExperiencesListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .addValueEventListener(objectiveExperienceListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .addValueEventListener(userPointsListener);
    }

    /**
     *
     * @return A map of completed experiences, accessible by experience ID
     */
    public LiveData<Map<String, Experience>> getCompletedExperiencesMap() {
        return databaseCompletedExperiencesById;
    }

    /**
     *
     * @return All the experiences completed by the current user
     */
    public LiveData<List<Experience>> getCompletedExperiences() {
        return Transformations.map(databaseCompletedExperiencesById, completedExperiencesById -> new ArrayList<>(completedExperiencesById.values()));
    }

    /**
     *
     * @return The objective experience's ID for the current user, or null if the user has no objective experience set
     */
    public LiveData<String> getObjectiveExperienceId() {
        return databaseObjectiveExperienceId;
    }

    /**
     *
     * @return The number of points held by the current user
     */
    public LiveData<Integer> getUserPoints() {
        return databaseUserPoints;
    }

    @NonNull
    public Task<Void> setObjectiveExperience(@Nullable String experienceId) {
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .setValue(experienceId);
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

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .removeEventListener(completedExperiencesListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(OBJECTIVE_REFERENCE)
                .removeEventListener(objectiveExperienceListener);
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .removeEventListener(userPointsListener);
    }
}
