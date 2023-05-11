package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.utils.Database.DB_TAG;
import static it.units.borghisegreti.utils.Database.COMPLETED_EXPERIENCES_REFERENCE;
import static it.units.borghisegreti.utils.Database.USER_DATA_REFERENCE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;

public class ObjectiveDialogViewModel extends MapViewModel {

    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final String userId;
    @NonNull
    private final MutableLiveData<Map<String, Experience>> databaseCompletedExperiencesById;
    @NonNull
    private final ValueEventListener completedExperiencesListener;

    public ObjectiveDialogViewModel(@NonNull FirebaseDatabase database, @NonNull SavedStateHandle savedState) {
        super(database, savedState);
        this.database = database;
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
        databaseCompletedExperiencesById = new MutableLiveData<>();
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
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .addValueEventListener(completedExperiencesListener);
    }

    /**
     *
     * @return A map of completed experiences, accessible by experience ID
     */
    public LiveData<Map<String, Experience>> getCompletedExperiencesMap() {
        return databaseCompletedExperiencesById;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .removeEventListener(completedExperiencesListener);
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
            return (T) new ObjectiveDialogViewModel(database, handle);
        }
    }
}
