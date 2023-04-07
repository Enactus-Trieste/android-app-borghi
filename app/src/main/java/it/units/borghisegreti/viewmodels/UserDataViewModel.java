package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_TAG;
import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;

public class UserDataViewModel extends ViewModel {
    public static final String USER_DATA_REFERENCE = "user_data";
    public static final String OBJECTIVE_REFERENCE = "objective";
    public static final String COMPLETED_EXPERIENCES_REFERENCE = "completed_experiences";
    public static final String POINTS_REFERENCE = "points";
    @NonNull
    private final MutableLiveData<Map<String, String>> databaseCompletedExperiencesFormattedDatesById;
    @NonNull
    private final MutableLiveData<String> databaseObjectiveExperienceId;
    @NonNull
    private final MutableLiveData<Integer> databaseUserPoints;
    @NonNull
    private final ValueEventListener userDataListener;
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final String userId;

    public UserDataViewModel(@NonNull String userId) {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseCompletedExperiencesFormattedDatesById = new MutableLiveData<>();
        databaseObjectiveExperienceId = new MutableLiveData<>();
        databaseUserPoints = new MutableLiveData<>();
        this.userId = userId;
        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // completed experiences
                Log.d(DB_TAG, "Received new remote user data from database");
                Map<String, String> completedExperiencesFormattedDatesById = new HashMap<>();
                for (DataSnapshot completedExperienceSnapshot : snapshot.child(COMPLETED_EXPERIENCES_REFERENCE).getChildren()) {
                    String experienceId = completedExperienceSnapshot.getKey();
                    String formattedCompletionDate = completedExperienceSnapshot.getValue(String.class);
                    completedExperiencesFormattedDatesById.put(experienceId, formattedCompletionDate);
                }
                databaseCompletedExperiencesFormattedDatesById.setValue(completedExperiencesFormattedDatesById);

                // objective experience
                String objectiveExperienceId = snapshot.child(OBJECTIVE_REFERENCE).getValue(String.class);
                databaseObjectiveExperienceId.setValue(objectiveExperienceId);

                // user points
                Integer points = snapshot.child(POINTS_REFERENCE).getValue(Integer.class);
                databaseUserPoints.setValue(points);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error: " + error.getMessage());
            }
        };
        database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(this.userId)
                .addValueEventListener(userDataListener);
    }

    /**
     *
     * @return A map of formatted experiences' completion dates, accessible by experience ID
     */
    public LiveData<Map<String, String>> getCompletedExperiencesMap() {
        return databaseCompletedExperiencesFormattedDatesById;
    }

    public LiveData<String> getObjectiveExperienceId() {
        return databaseObjectiveExperienceId;
    }

    public LiveData<Integer> getUserPoints() {
        return databaseUserPoints;
    }

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
        Map<String, String> mapToUpload = new HashMap<>();
        mapToUpload.put(experience.getId(), experience.getFormattedDateOfCompletion());
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .setValue(mapToUpload);
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
    private Task<DataSnapshot> getUserUpdatedPoints() {
        return database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(POINTS_REFERENCE)
                .get();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .removeEventListener(userDataListener);
    }
}
