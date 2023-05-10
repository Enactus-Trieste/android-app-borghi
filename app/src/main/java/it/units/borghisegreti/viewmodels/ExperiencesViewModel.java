package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.utils.Database.DB_TAG;
import static it.units.borghisegreti.utils.Database.COMPLETED_EXPERIENCES_REFERENCE;
import static it.units.borghisegreti.utils.Database.POINTS_REFERENCE;
import static it.units.borghisegreti.utils.Database.USER_DATA_REFERENCE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;

public class ExperiencesViewModel extends ViewModel {
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final SavedStateHandle savedState;
    @NonNull
    private final String userId;
    @NonNull
    private final MutableLiveData<Map<String, Experience>> databaseCompletedExperiencesById;
    @NonNull
    private final MutableLiveData<Integer> databaseUserPoints;
    @NonNull
    private final ValueEventListener completedExperiencesListener;
    @NonNull
    private final ValueEventListener userPointsListener;

    public ExperiencesViewModel(@NonNull FirebaseDatabase database, @NonNull SavedStateHandle savedState) {
        this.database = database;
        this.savedState = savedState;
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
        databaseCompletedExperiencesById = new MutableLiveData<>();
        databaseUserPoints = new MutableLiveData<>();
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
                .child(POINTS_REFERENCE)
                .addValueEventListener(userPointsListener);
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
     * @return The number of points held by the current user
     */
    public LiveData<Integer> getUserPoints() {
        return databaseUserPoints;
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
                .child(POINTS_REFERENCE)
                .removeEventListener(userPointsListener);
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
            return (T) new ExperiencesViewModel(database, handle);
        }
    }
}
