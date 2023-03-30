package it.units.borghisegreti.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.borghisegreti.R;

public class LoginFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionLoginFragmentToMapsFragment() {
        return new ActionOnlyNavDirections(R.id.action_loginFragment_to_mapsFragment);
    }
}
