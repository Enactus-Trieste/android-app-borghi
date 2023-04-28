package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_TAG;
import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;
import static it.units.borghisegreti.viewmodels.UserDataViewModel.COMPLETED_EXPERIENCES_REFERENCE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private final MutableLiveData<Map<String, Experience>> databaseCompletedExperiencesById;
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final String userId;
    @NonNull
    private final ValueEventListener completedExperiencesListener;

    public ExperiencesViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseCompletedExperiencesById = new MutableLiveData<>();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
        completedExperiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Experience> completedExperiencesById = new HashMap<>();
                for (DataSnapshot completedExperienceSnapshot : snapshot.child(COMPLETED_EXPERIENCES_REFERENCE).getChildren()) {
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
    }

    public LiveData<List<Experience>> getCompletedExperiences() {
        return Transformations.map(databaseCompletedExperiencesById, completedExperiencesById -> new ArrayList<>(completedExperiencesById.values()));
    }
}
