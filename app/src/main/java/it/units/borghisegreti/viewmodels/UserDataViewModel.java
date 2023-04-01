package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_TAG;
import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserDataViewModel extends ViewModel {
    public static final String USER_DATA_REFERENCE = "user_data";
    public static final String OBJECTIVE_REFERENCE = "objective";
    public static final String COMPLETED_EXPERIENCES_REFERENCE = "completed_experiences";
    public static final String POINTS_REFERENCE = "points";
    @NonNull
    private final MutableLiveData<Map<String, String>> databaseCompletedExperiencesFormattedDatesById;
    @NonNull
    private final ValueEventListener completedExperiencesListener;
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final String userId;

    public UserDataViewModel(@NonNull String userId) {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseCompletedExperiencesFormattedDatesById = new MutableLiveData<>();
        this.userId = userId;
        completedExperiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> completedExperiencesFormattedDatesById = new HashMap<>();
                for (DataSnapshot experienceSnapshot : snapshot.getChildren()) {
                    String experienceId = experienceSnapshot.getKey();
                    String formattedCompletionDate = experienceSnapshot.getValue(String.class);
                    completedExperiencesFormattedDatesById.put(experienceId, formattedCompletionDate);
                }
                databaseCompletedExperiencesFormattedDatesById.setValue(completedExperiencesFormattedDatesById);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error: " + error.getMessage());
            }
        };
        database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(this.userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .addValueEventListener(completedExperiencesListener);
    }

    /**
     *
     * @return A map of formatted experiences' completion dates, accessible by experience ID
     */
    public LiveData<Map<String, String>> getCompletedExperiencesMap() {
        return databaseCompletedExperiencesFormattedDatesById;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference()
                .child(USER_DATA_REFERENCE)
                .child(userId)
                .child(COMPLETED_EXPERIENCES_REFERENCE)
                .removeEventListener(completedExperiencesListener);
    }
}
