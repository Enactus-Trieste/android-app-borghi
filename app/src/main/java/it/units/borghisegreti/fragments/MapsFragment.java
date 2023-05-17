package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.fragments.ExperienceBottomSheetFragment.FRAGMENT_TAG;
import static it.units.borghisegreti.utils.Locator.LOCATOR_TAG;
import static it.units.borghisegreti.utils.Database.DB_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentMapsBinding;
import it.units.borghisegreti.fragments.dialogs.ExperienceDialogFragment;
import it.units.borghisegreti.fragments.exceptions.MarkerNotDrawnException;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.utils.IconBuilder;
import it.units.borghisegreti.utils.Locator;
import it.units.borghisegreti.viewmodels.MapViewModel;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String MAPS_TAG = "MAPS_FRAGMENT";
    private MapViewModel mapViewModel;
    @NonNull
    private List<Experience> experiences = Collections.emptyList();
    @NonNull
    private List<Zone> zones = Collections.emptyList();
    private GoogleMap map;
    @NonNull
    private final Map<Marker, Experience> experiencesOnTheMapByMarker = new HashMap<>();
    @NonNull
    private final Map<Marker, Zone> zonesOnTheMapByMarker = new HashMap<>();
    private FragmentMapsBinding viewBinding;
    @Nullable
    private String objectiveExperienceId;
    private ActivityResultLauncher<String[]> requestMapPermissions;
    private ActivityResultLauncher<Intent> requestEnableLocation;
    private Locator locator;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            NavHostFragment.findNavController(this).navigate(new ActionOnlyNavDirections(R.id.action_global_loginFragment));
            return;
        }
        // view models must be initialized after the fragment is attached
        mapViewModel = new ViewModelProvider(this, new MapViewModel.Factory(FirebaseDatabase.getInstance(DB_URL))).get(MapViewModel.class);

        locator = new Locator(requireContext(), getLifecycle(), new Locator.Callback() {
            @Override
            public void onObjectiveInRange() {
                Log.d(LOCATOR_TAG, "Objective experience is in range");
                //viewBinding.completeExpButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onObjectiveOutOfRange() {
                Log.d(LOCATOR_TAG, "Objective experience is out of range");
                //viewBinding.completeExpButton.setVisibility(View.GONE);
            }
        });

        requestMapPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grantedPermissions -> {
            if (areBothPermissionsGranted(grantedPermissions)) {
                viewBinding.map.getMapAsync(this);
            } else {
                // could also change view appearance
                Snackbar.make(requireView(), R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        });
        requestEnableLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                locator.start();
            } else {
                // could also change view appearance
                Snackbar.make(requireView(), R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private boolean areBothPermissionsGranted(@NonNull Map<String, Boolean> arePermissionsGranted) {
        return Boolean.TRUE.equals(arePermissionsGranted.get(Manifest.permission.ACCESS_COARSE_LOCATION))
                && Boolean.TRUE.equals(arePermissionsGranted.get(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentMapsBinding.inflate(inflater, container, false);
        View fragmentView = viewBinding.getRoot();
        viewBinding.map.onCreate(savedInstanceState);
        if (arePermissionsAlreadyGranted()) {
            viewBinding.map.getMapAsync(this);
        } else if (shouldShowRequestPermissionsRationale()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.educational_permission_request_title)
                    .setMessage(R.string.educational_permission_request_content)
                    .setPositiveButton(R.string.ok, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        } else {
            requestMapPermissions.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }

        viewBinding.completeExpButton.setOnClickListener(view -> {
            Experience objectiveExperience = null;
            for (Experience experience : experiences) {
                if (experience.getId().equals(objectiveExperienceId)) {
                    objectiveExperience = experience;
                    break;
                }
            }
            if (objectiveExperience != null) {
                mapViewModel.setExperienceAsCompleted(objectiveExperience)
                        .addOnSuccessListener(task -> Log.d(MAPS_TAG, "Experience set as completed"))
                        .addOnFailureListener(exception -> Log.e(MAPS_TAG, "Unable to set experience as completed", exception));
                mapViewModel.setObjectiveExperience(null)
                        .addOnSuccessListener(task -> Log.d(MAPS_TAG, "Objective experience reset"))
                        .addOnFailureListener(exception -> Log.e(MAPS_TAG, "Failed to reset objective experience", exception));
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.experience_completed)
                        .setMessage(String.format(getString(R.string.gained_points_alert), objectiveExperience.getPoints()))
                        .setPositiveButton(R.string.ok, ((dialogInterface, i) -> Toast.makeText(requireContext(), R.string.next_objective_toast, Toast.LENGTH_SHORT).show()))
                        .create();
                viewBinding.completeExpButton.setVisibility(View.GONE);
                dialog.show();
            }
        });
        return fragmentView;
    }

    private boolean shouldShowRequestPermissionsRationale() {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean arePermissionsAlreadyGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // when using MapView, we need to manually forward lifecycle stages
    @Override
    public void onStart() {
        viewBinding.map.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        viewBinding.map.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        viewBinding.map.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        viewBinding.map.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            mapViewModel.saveCameraLatitude(map.getCameraPosition().target.latitude);
            mapViewModel.saveCameraLongitude(map.getCameraPosition().target.longitude);
            mapViewModel.saveCameraZoom(map.getCameraPosition().zoom);
        }
        if (viewBinding != null) {
            viewBinding.map.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDestroyView() {
        viewBinding.map.onDestroy();
        viewBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        viewBinding.map.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(MAPS_TAG, "Map is ready, entering callback");
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setMinZoomPreference(7f);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);
        Double cameraLatitude = mapViewModel.getCameraLatitude();
        Double cameraLongitude = mapViewModel.getCameraLongitude();
        Float cameraZoom = mapViewModel.getCameraZoom();
        if (cameraLatitude != null && cameraLongitude != null && cameraZoom != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cameraLatitude, cameraLongitude), cameraZoom));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.879688, 13.564337), 8f));
        }
        map.setOnCameraMoveListener(this::drawMarkers);

        mapViewModel.getObjectiveExperienceId().observe(getViewLifecycleOwner(), experienceId -> {
            objectiveExperienceId = experienceId;
            locator.submitObjectiveId(experienceId);
            for (Experience experienceOnTheMap : experiencesOnTheMapByMarker.values()) {
                if (experienceOnTheMap.getId().equals(objectiveExperienceId)) {
                    Marker marker = findMarkerAssociatedToExperience(experienceOnTheMap);
                    if (marker != null) {
                        marker.setZIndex(1f);
                    } else {
                        Log.w(MAPS_TAG, "No marker found on the map for the given experience");
                    }
                }
            }
        });
        mapViewModel.getExperiences().observe(getViewLifecycleOwner(), experiences -> {
            Log.d(MAPS_TAG, "Obtained " + experiences.size() + " experiences from Firebase");
            this.experiences = experiences;
            locator.submitExperiences(experiences);
            if (map.getCameraPosition().zoom >= 12) {
                drawAllExperienceMarkers();
            } else {
                Log.d(MAPS_TAG, "Zoom is less than 12, not drawing experience markers");
            }
        });
        mapViewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            Log.d(MAPS_TAG, "Obtained " + zones.size() + " zones from Firebase");
            this.zones = zones;
            if (map.getCameraPosition().zoom < 12) {
                drawAllZoneMarkers();
            } else {
                Log.d(MAPS_TAG, "Zoom is more than 12, not drawing zone markers");
            }
        });

        if (isLocationEnabled()) {
            locator.start();
        } else {
            requestEnableLocation.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void drawMarkers() {
        Log.d(MAPS_TAG, "Starting to draw markers");
        if (map.getCameraPosition().zoom < 12) {
            clearFromTheMap(experiencesOnTheMapByMarker.keySet());
            experiencesOnTheMapByMarker.clear();
            drawAllZoneMarkers();
        } else {
            clearFromTheMap(zonesOnTheMapByMarker.keySet());
            zonesOnTheMapByMarker.clear();
            drawAllExperienceMarkers();
        }
    }

    private void drawAllExperienceMarkers() {
        for (Experience experience : experiences) {
            if (!experiencesOnTheMapByMarker.containsValue(experience)) {
                try {
                    drawExperienceMarker(experience);
                } catch (MarkerNotDrawnException e) {
                    Log.e(MAPS_TAG, e.getMessage(), e);
                }
            } else {
                Log.d(MAPS_TAG, "Marker for experience " + experience + " already on the map, not drawing");
            }
            if (experience.getId().equals(objectiveExperienceId)) {
                Marker foundMarker = findMarkerAssociatedToExperience(experience);
                if (foundMarker != null) {
                    foundMarker.setZIndex(1f);
                } else {
                    Log.e(MAPS_TAG, "No marker found for the objective experience, even though it should be on the map");
                }
            }
        }
    }

    @Nullable
    private Marker findMarkerAssociatedToExperience(@NonNull Experience experience) {
        for (Map.Entry<Marker, Experience> entry : experiencesOnTheMapByMarker.entrySet()) {
            if (experience.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawExperienceMarker(@NonNull Experience experience) throws MarkerNotDrawnException {
        Marker experienceMarker = map.addMarker(new MarkerOptions()
                .position(experience.getCoordinates())
                .title(experience.getName())
                .snippet(experience.getDescription()));
        if (experienceMarker == null) {
            throw new MarkerNotDrawnException("Error while drawing marker for experience " + experience);
        } else {
            experiencesOnTheMapByMarker.put(experienceMarker, experience);
            IconBuilder iconBuilder = new IconBuilder(requireContext(), experience);
            experienceMarker.setIcon(iconBuilder.buildMarkerDescriptor());
            experienceMarker.setAlpha(iconBuilder.getMarkerAlpha());
        }
    }

    private void drawAllZoneMarkers() {
        for (Zone zone : zones) {
            if (!zonesOnTheMapByMarker.containsValue(zone)) {
                try {
                    drawZoneMarker(zone);
                } catch (MarkerNotDrawnException e) {
                    Log.e(MAPS_TAG, e.getMessage(), e);
                }
            } else {
                Log.d(MAPS_TAG, "Marker for zone " + zone + " already on the map, not drawing");
            }
        }
    }

    private void drawZoneMarker(@NonNull Zone zone) throws MarkerNotDrawnException {
        Marker zoneMarker = map.addMarker(new MarkerOptions()
                .position(zone.getCoordinates())
                .title(zone.getName())
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromAsset("icons/BorgoIcon.png")));
        if (zoneMarker == null) {
            throw new MarkerNotDrawnException("Error while drawing marker for zone " + zone);
        } else {
            zonesOnTheMapByMarker.put(zoneMarker, zone);
        }
    }

    private void clearFromTheMap(@NonNull Set<Marker> markersToClear) {
        for (Marker marker : markersToClear) {
            marker.remove();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Zone zone = zonesOnTheMapByMarker.get(marker);
        Experience experience = experiencesOnTheMapByMarker.get(marker);
        if (zone != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12f));
            return true;
        } else if (experience != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    ExperienceDialogFragment dialog = ExperienceDialogFragment.newInstance(experience.getId());
                    dialog.show(MapsFragment.this.getParentFragmentManager(), FRAGMENT_TAG);
                }

                @Override
                public void onCancel() {
                }
            });
            return true;
        }
        return false;
    }
}