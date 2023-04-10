package it.units.borghisegreti.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.borghisegreti.R;

public class RegistrationFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionRegistrationFragmentToMapsFragment() {
        return new ActionOnlyNavDirections(R.id.action_registrationFragment_to_mapsFragment);
    }
}
