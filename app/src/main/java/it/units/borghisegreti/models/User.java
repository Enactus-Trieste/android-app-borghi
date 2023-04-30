package it.units.borghisegreti.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {

    @NonNull
    private String userId;
    private int points;
    @Nullable
    private Map<String, Experience> completedExperiences;
    @Nullable
    private String objectiveExperienceId;
    @NonNull
    private String email;

    public User(@NonNull String userId, @NonNull String email) {
        this.email = email;
        points = 0;
        this.userId = userId;
    }

    // required for Firebase's getValue(User.class)
    private User() {}

    public int getPoints() {
        return points;
    }

    @Nullable
    public Map<String, Experience> getCompletedExperiences() {
        return completedExperiences;
    }

    @Nullable
    public String getObjectiveExperienceId() {
        return objectiveExperienceId;
    }

    @NonNull
    public String getId() {
        return userId;
    }

    public void addCompletedExperience(@NonNull Experience experience) {
        if (completedExperiences != null) {
            completedExperiences.put(experience.getId(), experience);
        } else {
            completedExperiences = new HashMap<>();
            completedExperiences.put(experience.getId(), experience);
        }
        points += experience.getPoints();
    }

    @NonNull
    @Override
    public String toString() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
