package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.units.borghisegreti.models.Experience;

public class ExperiencesViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<List<Experience>> databaseExperiences;
    @NonNull
    private final FirebaseDatabase database;
    @NonNull
    private final MutableLiveData<Map<String, String>> completedExperiences;
    @NonNull
    private final String userId;

    public ExperiencesViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseExperiences = new MutableLiveData<>();
        completedExperiences = new MutableLiveData<>();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
    }
}
