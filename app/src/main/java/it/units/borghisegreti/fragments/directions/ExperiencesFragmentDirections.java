package it.units.borghisegreti.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.borghisegreti.R;

public class ExperiencesFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionExperiencesFragmentToMapsFragment() {
        return new ActionOnlyNavDirections(R.id.action_experiencesFragment_to_mapsFragment);
    }

    @NonNull
    @Contract(" -> new")
    public static NavDirections actionExperiencesFragmentToUserProfileFragment() {
        return new ActionOnlyNavDirections(R.id.action_experiencesFragment_to_userProfileFragment);
    }
}
