package it.units.borghisegreti.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.units.borghisegreti.models.Zone;


public class MapsViewModel extends ViewModel {
    public static final String DB_URL = "https://adventuremaps-1205-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String DB_TAG = "FIREBASE_DB_CONNECTOR";
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
                List<Zone> zones = new ArrayList<>();
                for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                    Log.d(DB_TAG, "onDataChange: ZONES");
                    // a better approach would be Zone zone = zoneSnapshot.getValue(Zone.class)
                    String zoneName = zoneSnapshot.child("name").getValue(String.class);
                    Double latitude = zoneSnapshot.child("coordinates").child("latitude").getValue(Double.class);
                    Double longitude = zoneSnapshot.child("coordinates").child("longitude").getValue(Double.class);
                    LatLng zoneCoordinates = new LatLng(latitude, longitude);
                    zones.add(new Zone(zoneName, zoneCoordinates));
                }
                MapsViewModel.this.zones.setValue(zones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "error: " + error.getMessage());
            }
        };
        database.getReference(ZONES_REFERENCE).addValueEventListener(zonesListener);
    }

    public LiveData<List<Zone>> getZones() {
        return zones;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(ZONES_REFERENCE).removeEventListener(zonesListener);
    }
}
