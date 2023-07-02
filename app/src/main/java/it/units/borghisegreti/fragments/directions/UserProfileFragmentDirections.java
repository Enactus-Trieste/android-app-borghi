package it.units.borghisegreti.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.borghisegreti.R;

public class UserProfileFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionUserProfileFragmentToMapsFragment() {
        return new ActionOnlyNavDirections(R.id.action_userProfileFragment_to_mapsFragment);
    }

    @NonNull
    @Contract(" -> new")
    public static NavDirections actionUserProfileFragmentToExperiencesFragment() {
        return new ActionOnlyNavDirections(R.id.action_userProfileFragment_to_experiencesFragment);
    }
}
