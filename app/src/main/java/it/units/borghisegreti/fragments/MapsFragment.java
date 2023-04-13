package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.fragments.ExperienceBottomSheetFragment.FRAGMENT_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentMapsBinding;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.utils.IconBuilder;
import it.units.borghisegreti.viewmodels.MapViewModel;
import it.units.borghisegreti.viewmodels.UserDataViewModel;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String MAPS_TAG = "MAPS_FRAGMENT";
    private MapViewModel mapViewModel;
    private UserDataViewModel userDataViewModel;
    @Nullable
    private List<Experience> experiences;
    @Nullable
    private List<Zone> zones;
    private GoogleMap map;
    private final Map<Marker, Experience> experiencesOnTheMapByMarker = new HashMap<>();
    private final Map<Marker, Zone> zonesOnTheMapByMarker = new HashMap<>();
    private FragmentMapsBinding viewBinding;
    @Nullable
    private String objectiveExperienceId;
    private ActivityResultLauncher<String[]> requestMapPermissions;
    private ActivityResultLauncher<Intent> requestLocationSourceSetting;
    @Nullable
    private Marker userMarker;

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
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        userDataViewModel = new ViewModelProvider(this).get(UserDataViewModel.class);

        requestMapPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), arePermissionsGranted -> {
            if (areBothPermissionsGranted(arePermissionsGranted)) {
                viewBinding.map.getMapAsync(this);
            } else {
                // could also change view appearance
                Snackbar.make(requireView(), R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        });
        requestLocationSourceSetting = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                buildAndSubmitLocationRequest();
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
            Log.d(MAPS_TAG, "Start to retrieve Google map");
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
        return fragmentView;
    }

    private boolean shouldShowRequestPermissionsRationale() {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean arePermissionsAlreadyGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewBinding.map.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewBinding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewBinding.map.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewBinding.map.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding.map.onDestroy();
        viewBinding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        viewBinding.map.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        viewBinding.map.onLowMemory();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(MAPS_TAG, "Map is ready, entering callback");
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setMinZoomPreference(7f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.879688, 13.564337), 8f));
        map.setOnCameraMoveListener(this::drawMarkers);

        userDataViewModel.getObjectiveExperienceId().observe(getViewLifecycleOwner(), experienceId -> {
            objectiveExperienceId = experienceId;
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
            if (map.getCameraPosition().zoom >= 12) {
                drawAllExperienceMarkers();
            } else {
                Log.d(MAPS_TAG, "Zoom is less than 12, not drawing experience markers");
            }
            viewBinding.completeExpButton.setOnClickListener(view -> {
                Experience objectiveExperience = null;
                for (Experience experience : Objects.requireNonNull(this.experiences, "Database should always have at least one experience available")) {
                    if (experience.getId().equals(objectiveExperienceId)) {
                        objectiveExperience = experience;
                        break;
                    }
                }
                if (objectiveExperience != null) {
                    userDataViewModel.setExperienceAsCompleted(objectiveExperience)
                            .addOnSuccessListener(task -> Log.d(MAPS_TAG, "Experience set as completed"))
                            .addOnFailureListener(exception -> Log.e(MAPS_TAG, "Unable to set experience as completed", exception));
                    userDataViewModel.setObjectiveExperience(null)
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
            buildAndSubmitLocationRequest();
        } else {
            requestLocationSourceSetting.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @SuppressLint("MissingPermission")
    private void buildAndSubmitLocationRequest() {
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5);
        builder.setMaxUpdateDelayMillis(0);
        LocationRequest locationRequest = builder.build();
        locationProviderClient.requestLocationUpdates(locationRequest, location -> {
            if (isObjectiveInRange(location)) {
                Log.d(MAPS_TAG, "Objective experience is in range");
                viewBinding.completeExpButton.setVisibility(View.VISIBLE);
            } else {
                Log.d(MAPS_TAG, "Objective experience is out of range");
                viewBinding.completeExpButton.setVisibility(View.GONE);
            }
        }, Looper.myLooper());
        map.setMyLocationEnabled(true);
    }

    private boolean isObjectiveInRange(Location location) {
        if (experiences != null) {
            Experience objectiveExperience = null;
            for (Experience experience : experiences) {
                if (experience.getId().equals(objectiveExperienceId)) {
                    objectiveExperience = experience;
                    break;
                }
            }
            double distanceBetweenPoints = 500;
            if (objectiveExperience != null) {
                distanceBetweenPoints = computeDistanceBetweenPoints(objectiveExperience.getLatitude(),
                        objectiveExperience.getLongitude(), location.getLatitude(), location.getLongitude());
            } else {
                Log.e(MAPS_TAG, "Unable to find objective among experiences");
            }
            Log.d(MAPS_TAG, "Distance from the objective is: " + distanceBetweenPoints);
            return distanceBetweenPoints < 50;
        }
        Log.w(MAPS_TAG, "Experiences not loaded yet");
        return false;
    }

    private double computeDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(degreeToRadians(lat1)) * Math.sin(degreeToRadians(lat2)) + Math.cos(degreeToRadians(lat1)) * Math.cos(degreeToRadians(lat2)) * Math.cos(degreeToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegree(dist);
        dist = dist * 60 * 1.1515 * 1609.344;
        return dist;
    }

    private double degreeToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double radiansToDegree(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void drawMarkers() {
        Log.d(MAPS_TAG, "Starting to draw markers");
        if (experiences != null && zones != null) {
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
    }

    private void drawAllExperienceMarkers() {
        for (Experience experience : Objects.requireNonNull(experiences, "No experiences found, value is null")) {
            if (!experiencesOnTheMapByMarker.containsValue(experience)) {
                drawExperienceMarker(experience);
            } else {
                Log.d(MAPS_TAG, "Marker for experience " + experience + " already on the map, not drawing");
            }
            if (experience.getId().equals(objectiveExperienceId)) {
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
    private Marker findMarkerAssociatedToExperience(@NonNull Experience experience) {
        for (Map.Entry<Marker, Experience> entry : experiencesOnTheMapByMarker.entrySet()) {
            if (experience.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawExperienceMarker(@NonNull Experience experience) {
        Marker marker = findMarkerAssociatedToExperience(experience);
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(experience.getCoordinates())
                    .title(experience.getName())
                    .snippet(experience.getDescription()));
            experiencesOnTheMapByMarker.put(marker, experience);
        }
        if (marker == null) {
            Log.e(MAPS_TAG, "Error while drawing marker in drawExperienceMarker");
        } else {
            IconBuilder markerBuilder = new IconBuilder(requireContext(), experience);
            marker.setIcon(markerBuilder.buildMarkerDescriptor());
            marker.setAlpha(markerBuilder.getMarkerAlpha());
        }
    }

    private void drawAllZoneMarkers() {
        for (Zone zone : Objects.requireNonNull(zones, "No zones found, value is null")) {
            if (!zonesOnTheMapByMarker.containsValue(zone)) {
                Marker zoneMarker = map.addMarker(new MarkerOptions()
                        .position(zone.getCoordinates())
                        .title(zone.getName())
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromAsset("icons/BorgoIcon.png")));
                zonesOnTheMapByMarker.put(zoneMarker, zone);
            } else {
                Log.d(MAPS_TAG, "Marker for zone " + zone + " already on the map, not drawing");
            }
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
        } else if (experience != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    ExperienceBottomSheetFragment dialog = ExperienceBottomSheetFragment.newInstance(experience.getId());
                    dialog.show(getParentFragmentManager(), FRAGMENT_TAG);
                }

                @Override
                public void onCancel() {
                }
            });
        }
        return true;
    }
}