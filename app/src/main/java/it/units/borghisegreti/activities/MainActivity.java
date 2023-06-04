package it.units.borghisegreti.activities;

import static it.units.borghisegreti.fragments.directions.ExperiencesFragmentDirections.actionExperiencesFragmentToMapsFragment;
import static it.units.borghisegreti.fragments.directions.ExperiencesFragmentDirections.actionExperiencesFragmentToUserProfileFragment;
import static it.units.borghisegreti.fragments.directions.MapsFragmentDirections.actionMapsFragmentToExperiencesFragment;
import static it.units.borghisegreti.fragments.directions.MapsFragmentDirections.actionMapsFragmentToUserProfileFragment;
import static it.units.borghisegreti.fragments.directions.UserProfileFragmentDirections.actionUserProfileFragmentToExperiencesFragment;
import static it.units.borghisegreti.fragments.directions.UserProfileFragmentDirections.actionUserProfileFragmentToMapsFragment;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(hostFragment, "Host fragment container view not found").getNavController();

        navController.addOnDestinationChangedListener(((controller, destination, arguments) -> {
            // here we can check the destination id and handle changes accordingly
            if (destination.getId() == R.id.mapsFragment) {
                viewBinding.bottomNavigation.setVisibility(View.VISIBLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.experiencesFragment) {
                viewBinding.bottomNavigation.setVisibility(View.VISIBLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.userProfileFragment) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.loginFragment) {
                viewBinding.bottomNavigation.setVisibility(View.GONE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.registrationFragment) {
                viewBinding.bottomNavigation.setVisibility(View.GONE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                viewBinding.bottomNavigation.setVisibility(View.VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }));

        viewBinding.bottomNavigation.setSelectedItemId(R.id.menu_map);
        NavigationUI.setupWithNavController(viewBinding.bottomNavigation, navController);
        viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (Objects.equals(navController.getCurrentDestination(), navController.findDestination(R.id.mapsFragment))) {
                if (item.getItemId() == R.id.menu_completed_experiences) {
                    navController.navigate(actionMapsFragmentToExperiencesFragment());
                    return true;
                } else if (item.getItemId() == R.id.menu_user) {
                    navController.navigate(actionMapsFragmentToUserProfileFragment());
                    return true;
                }
                return false;
            } else if (Objects.equals(navController.getCurrentDestination(), navController.findDestination(R.id.experiencesFragment))) {
                if (item.getItemId() == R.id.menu_map) {
                    navController.navigate(actionExperiencesFragmentToMapsFragment());
                    return true;
                } else if (item.getItemId() == R.id.menu_user) {
                    navController.navigate(actionExperiencesFragmentToUserProfileFragment());
                    return true;
                }
                return false;
            } else if (Objects.equals(navController.getCurrentDestination(), navController.findDestination(R.id.userProfileFragment))) {
                if (item.getItemId() == R.id.menu_map) {
                    navController.navigate(actionUserProfileFragmentToMapsFragment());
                    return true;
                } else if (item.getItemId() == R.id.menu_completed_experiences) {
                    navController.navigate(actionUserProfileFragmentToExperiencesFragment());
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // needed in case a NavigationDrawer with an AppBarConfiguration is added
        return super.onSupportNavigateUp();
    }
}