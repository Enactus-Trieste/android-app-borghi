package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.units.borghisegreti.R;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.utils.IconBuilder;
import it.units.borghisegreti.viewmodels.MapsViewModel;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String MAPS_TAG = "MAPS_FRAGMENT";
    private MapsViewModel viewModel;
    private List<Experience> experiences;
    private List<Zone> zones;
    private GoogleMap map;
    private final Map<Marker, Experience> experiencesOnTheMap = new HashMap<>();
    private final Map<Marker, Zone> zonesOnTheMap = new HashMap<>();

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
            Log.d(MAPS_TAG, "New experiences received from Firebase");
            this.experiences = experiences;
            drawMarkers();
        });
        viewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            Log.d(MAPS_TAG, "New zones received from Firebase");
            this.zones = zones;
            drawMarkers();
        });

    }

    private void drawMarkers() {
        if (experiences != null && zones != null) {
            if (map.getCameraPosition().zoom < 12) {
                drawAllZoneMarkers();
            } else {
                drawAllExperienceMarkers();
            }
        }
    }

    private void drawAllExperienceMarkers() {
        clearFromTheMap(zonesOnTheMap.keySet());
        zonesOnTheMap.clear();
        for (Experience experience : experiences) {
            drawExperienceMarker(experience);
            if (experience.getIsTheObjective()) {
                Marker foundMarker = findMarkerAssociatedToExperience(experience);
                if (foundMarker != null) {
                    foundMarker.setZIndex(1f);
                } else {
                    Log.e(MAPS_TAG, "No marker found in findMarkerAssociatedToExperience");
                }
            }
        }
    }

    @Nullable
    private Marker findMarkerAssociatedToExperience(Experience experience) {
        for (Map.Entry<Marker, Experience> entry : experiencesOnTheMap.entrySet()) {
            if (experience.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawExperienceMarker(Experience experience) {
        Marker marker = findMarkerAssociatedToExperience(experience);
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(experience.getCoordinates())
                    .title(experience.getName())
                    .snippet(experience.getDescription()));
            experiencesOnTheMap.put(marker, experience);
        }
        if (marker == null) {
            Log.e(MAPS_TAG, "Error while drawing marker in drawExperienceMarker");
        } else {
            IconBuilder markerBuilder = new IconBuilder(getContext(), experience);
            marker.setIcon(markerBuilder.buildMarkerDescriptor());
            marker.setAlpha(markerBuilder.getMarkerAlpha());
        }
    }

    private void drawAllZoneMarkers() {
        clearFromTheMap(experiencesOnTheMap.keySet());
        experiencesOnTheMap.clear();
        for (Zone zone : zones) {
            Marker zoneMarker = map.addMarker(new MarkerOptions()
                    .position(zone.getCoordinates())
                    .title(zone.getName())
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromAsset("icons/BorgoIcon.png")));
            zonesOnTheMap.put(zoneMarker, zone);
        }
    }

    private void clearFromTheMap(@NonNull Set<Marker> markersToClear) {
        for (Marker marker : markersToClear) {
            marker.remove();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Zone zone = zonesOnTheMap.get(marker);
        Experience experience = experiencesOnTheMap.get(marker);
        if (zone != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12f));
        } else if (experience != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    // TODO insert BottomSheetDialogFragment
                }

                @Override
                public void onCancel() {
                }
            });
        }
        return true;
    }
}