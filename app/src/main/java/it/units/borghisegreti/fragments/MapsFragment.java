package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import it.units.borghisegreti.R;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.viewmodels.MapsViewModel;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String MAPS_TAG = "MAPS_FRAGMENT";
    private MapsViewModel viewModel;
    private List<Experience> experiences;
    private List<Zone> zones;
    private GoogleMap map;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapsViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_maps, container, false);
        // TODO move map handling from MapActivity here
        getMapAsync(this);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(MAPS_TAG, "Map is ready, entering callback");
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setMinZoomPreference(7f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.879688, 13.564337), 8f));
        map.setOnCameraMoveListener(this::drawMarkers);
        viewModel.getExperiences().observe(getViewLifecycleOwner(), experiences -> {
            this.experiences = experiences;
            drawMarkers();
        });
        viewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            this.zones = zones;
            drawMarkers();
        });

    }

    private void drawMarkers() {
        if (map.getCameraPosition().zoom < 12) {
            drawAllZoneMarkers();
        } else {
            drawAllExperienceMarkers();
        }
    }

    private void drawAllExperienceMarkers() {

    }

    private void drawAllZoneMarkers() {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
}