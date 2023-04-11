package it.units.borghisegreti.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;

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
        }));

        // viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {});
    }

    @Override
    public boolean onSupportNavigateUp() {
        // needed in case a NavigationDrawer with an AppBarConfiguration is added
        return super.onSupportNavigateUp();
    }
}