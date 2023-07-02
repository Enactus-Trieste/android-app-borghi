package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.utils.Database.DB_URL;
import static it.units.borghisegreti.utils.Locator.LOCATOR_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentMapsBinding;
import it.units.borghisegreti.utils.Locator;
import it.units.borghisegreti.utils.MapHandler;
import it.units.borghisegreti.viewmodels.MapViewModel;

public class MapsFragment extends Fragment {

    private static final String MAPS_TAG = "MAPS_FRAGMENT";
    private MapViewModel mapViewModel;
    private FragmentMapsBinding viewBinding;
    private ActivityResultLauncher<String[]> requestMapPermissions;
    private ActivityResultLauncher<Intent> requestEnableLocation;
    private Locator locator;
    private MapHandler mapHandler;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

            @Override
            public void onLocationUpdate(Location location) {
                Log.d(LOCATOR_TAG, "New location data received: " + location);
            }
        });

        requestMapPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grantedPermissions -> {
            if (areBothPermissionsGranted(grantedPermissions)) {
                viewBinding.map.getMapAsync(this::setupDrawingOfMarkers);
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
        super.onCreate(savedInstanceState);
    }

    private boolean areBothPermissionsGranted(@NonNull Map<String, Boolean> arePermissionsGranted) {
        return Boolean.TRUE.equals(arePermissionsGranted.get(Manifest.permission.ACCESS_COARSE_LOCATION))
                && Boolean.TRUE.equals(arePermissionsGranted.get(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentMapsBinding.inflate(inflater, container, false);
        viewBinding.map.onCreate(savedInstanceState);
        if (arePermissionsAlreadyGranted()) {
            viewBinding.map.getMapAsync(this::setupDrawingOfMarkers);
        } else if (shouldShowRequestPermissionsRationale()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.educational_permission_request_title)
                    .setMessage(R.string.educational_permission_request_content)
                    .setPositiveButton(R.string.ok, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        } else {
            requestMapPermissions.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
        return viewBinding.getRoot();
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
        mapViewModel.saveCameraLatitude(mapHandler.getMap().getCameraPosition().target.latitude);
        mapViewModel.saveCameraLongitude(mapHandler.getMap().getCameraPosition().target.longitude);
        mapViewModel.saveCameraZoom(mapHandler.getMap().getCameraPosition().zoom);
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
        viewBinding.map.onLowMemory();
        super.onLowMemory();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setupDrawingOfMarkers(GoogleMap map) {
        mapHandler = new MapHandler(requireContext(), map, mapViewModel.getCameraLatitude(), mapViewModel.getCameraLongitude(), mapViewModel.getCameraZoom());

        mapViewModel.getObjectiveExperienceId().observe(getViewLifecycleOwner(), experienceId -> {
            locator.submitObjectiveId(experienceId);
            mapHandler.submitObjectiveId(experienceId);
        });
        mapViewModel.getExperiences().observe(getViewLifecycleOwner(), experiences -> {
            Log.d(MAPS_TAG, "Obtained " + experiences.size() + " experiences from Firebase");
            locator.submitExperiences(experiences);
            mapHandler.drawExperienceMarkers(experiences);
        });
        mapViewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            Log.d(MAPS_TAG, "Obtained " + zones.size() + " zones from Firebase");
            mapHandler.drawZoneMarkers(zones);
        });

        if (isLocationEnabled()) {
            locator.start();
        } else {
            requestEnableLocation.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

    }
}