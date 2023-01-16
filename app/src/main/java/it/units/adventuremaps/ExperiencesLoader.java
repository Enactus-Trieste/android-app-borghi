package it.units.adventuremaps;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ExperiencesLoader {
    private final String TAG = "FIREBASE_DATABASE";
    private final FirebaseUser user;
    private ArrayList<Experience> experiences;
    private DataEventListener listener;

    public ExperiencesLoader(FirebaseUser user) {
        this.user = user;
        experiences = new ArrayList<>();
        loadExperiencesFromDatabase();
    }

    private void loadExperiencesFromDatabase() {
        Log.d(TAG, "loading Experiences...");
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference experiencesRef = database.getReference().child("experiences");
        Log.d(TAG, "get references: " + experiencesRef);

        experiencesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data Changed!");
                for (DataSnapshot dataExperience : dataSnapshot.getChildren()) {
                    String id = dataExperience.getKey();
                    String name = dataExperience.child("name").getValue(String.class);
                    String description = dataExperience.child("description").getValue(String.class);
                    ExperienceType type = dataExperience.child("type").getValue(ExperienceType.class);
                    Double latitude = dataExperience.child("coordinates").child("latitude").getValue(Double.class);
                    Double longitude = dataExperience.child("coordinates").child("longitude").getValue(Double.class);

                    LatLng coordinates = new LatLng(latitude, longitude);

                    experiences.add(new Experience(id, name, description, type, coordinates));
                }
                loadAndSetObjectiveExperienceFromDatabase();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "error: " + error.getMessage());
            }
        });
    }

    private void loadAndSetObjectiveExperienceFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference userRef = database.getReference().child("user_data").child(user.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String objectiveExperienceId = snapshot.child("objective").getValue(String.class);
                for (Experience experience : experiences) {
                    if (experience.getId().equals(objectiveExperienceId)) {
                        experience.setIsTheObjective(true);
                    }
                }
                listener.onDataAvailable(experiences);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "error: " + error.getMessage());
            }
        });
    }

    public void addDataEventListener(DataEventListener listener) {
        this.listener = listener;
    }
}
