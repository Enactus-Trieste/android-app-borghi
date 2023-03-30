package it.units.borghisegreti.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import it.units.borghisegreti.models.Zone;


public class MapsViewModel extends ViewModel {
    public static final String DB_URL = "https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String ZONES_REFERENCE = "zones";
    private final FirebaseDatabase database;
    private final ValueEventListener experiencesListener;
    private final ValueEventListener zonesListener;
    private final MutableLiveData<List<Zone>> zones;

    public MapsViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        zones = new MutableLiveData<>();
        experiencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        zonesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public LiveData<List<Zone>> getZones() {
        return zones;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
