package it.units.borghisegreti.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
import it.units.borghisegreti.utils.ExperienceType;


public class MapViewModel extends ViewModel {
    public static final String DB_URL = "https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String DB_TAG = "FIREBASE_DB_CONNECTOR";
    public static final String ZONES_REFERENCE = "zones";
    public static final String EXPERIENCES_REFERENCE = "experiences";
    private final FirebaseDatabase database;
    private final ValueEventListener experiencesListener;
    private final ValueEventListener zonesListener;
    private final MutableLiveData<List<Zone>> databaseZones;
    private final MutableLiveData<List<Experience>> databaseExperiences;

    public MapViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseZones = new MutableLiveData<>();
        databaseExperiences = new MutableLiveData<>();
        experiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Experience> experiences = new ArrayList<>();
                for (DataSnapshot experienceSnapshot : snapshot.getChildren()) {
                    Log.d(DB_TAG, "Received new remote experiences from database");
                    Experience databaseExperience = experienceSnapshot.getValue(Experience.class);
                    experiences.add(databaseExperience);
                }
                MapViewModel.this.databaseExperiences.setValue(experiences);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "error: " + error.getMessage());
            }
        };
        zonesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Zone> zones = new ArrayList<>();
                for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                    Log.d(DB_TAG, "Received new remote zones from database");
                    Zone databaseZone = zoneSnapshot.getValue(Zone.class);
                    zones.add(databaseZone);
                }
                MapViewModel.this.databaseZones.setValue(zones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "error: " + error.getMessage());
            }
        };
        database.getReference(ZONES_REFERENCE).addValueEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).addValueEventListener(experiencesListener);
    }

    public LiveData<List<Zone>> getZones() {
        return databaseZones;
    }

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

    public Task<Void> uploadNewExperience(@NonNull String name,
                                          @NonNull String description,
                                          @NonNull ExperienceType type,
                                          @NonNull LatLng coordinates,
                                          int points) {
        DatabaseReference experienceReference = database.getReference(EXPERIENCES_REFERENCE).push();
        Experience experience = new Experience(Objects.requireNonNull(experienceReference.getKey(), "This reference should never point at database root"),
                name,
                description,
                type,
                coordinates,
                points);
        return experienceReference.setValue(experience);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(ZONES_REFERENCE).removeEventListener(zonesListener);
        database.getReference(EXPERIENCES_REFERENCE).removeEventListener(experiencesListener);
    }
}
