package it.units.borghisegreti.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

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
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.userProfileFragment) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (destination.getId() == R.id.loginFragment) {
                viewBinding.bottomNavigation.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.registrationFragment) {
                viewBinding.bottomNavigation.setVisibility(View.GONE);
            } else {
                viewBinding.bottomNavigation.setVisibility(View.VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }));

        viewBinding.bottomNavigation.setSelectedItemId(R.id.menu_map);
        viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_completed_experiences) {
                // this prevents page reload in case the user already reached this destination
                if (navController.getCurrentDestination().getId() != R.id.experiencesFragment) {
                    navController.navigate(new ActionOnlyNavDirections(R.id.action_global_experiencesFragment));
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_user) {
                if (navController.getCurrentDestination().getId() != R.id.userProfileFragment) {
                    navController.navigate(new ActionOnlyNavDirections(R.id.action_global_userProfileFragment));
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_map) {
                // this prevents page reload in case the user already reached this destination
                if (navController.getCurrentDestination().getId() != R.id.mapsFragment) {
                    navController.navigate(new ActionOnlyNavDirections(R.id.action_global_mapsFragment));
                    return true;
                }
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