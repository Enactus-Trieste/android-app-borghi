package it.units.borghisegreti.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class User {

    private int points;
    @NonNull
    private final Map<String, Experience> completedExperiences;
    @Nullable
    private String objectiveExperienceId;

    public User() {
        completedExperiences = new HashMap<>();
        this.points = 0;
    }

    public int getPoints() {
        return points;
    }

    @NonNull
    public Map<String, Experience> getCompletedExperiences() {
        return completedExperiences;
    }

    @Nullable
    public String getObjectiveExperienceId() {
        return objectiveExperienceId;
    }

    public void addCompletedExperience(@NonNull Experience experience) {
        completedExperiences.put(experience.getId(), experience);
        points += experience.getPoints();
    }
}
