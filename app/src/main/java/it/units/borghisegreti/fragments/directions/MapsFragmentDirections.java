package it.units.borghisegreti.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.borghisegreti.R;

public class MapsFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionMapsFragmentToUserProfileFragment() {
        return new ActionOnlyNavDirections(R.id.action_mapsFragment_to_userProfileFragment);
    }

    @NonNull
    @Contract(" -> new")
    public static NavDirections actionMapsFragmentToExperiencesFragment() {
        return new ActionOnlyNavDirections(R.id.action_mapsFragment_to_experiencesFragment);
    }
}
