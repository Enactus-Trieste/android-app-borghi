package it.units.borghisegreti.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import it.units.borghisegreti.utils.ExperienceType;

public class Experience {
    // stored in the database
    @NonNull
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private String type;
    private double latitude;
    private double longitude;
    private int points;

    // not stored in the database
    private boolean isTheObjective;
    private boolean isCompletedByUser;
    @Nullable
    private String formattedDateOfCompletion;

    public Experience(
            @NonNull String id,
            @NonNull String name,
            @NonNull String description,
            @NonNull ExperienceType type,
            @NonNull LatLng coordinates, int points) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type.toString();
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
        this.points = points;
        this.isTheObjective = false;
        this.isCompletedByUser = false;
    }

    // required to use Firebase's getValue(Experience.class)
    private Experience() {}

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public LatLng getCoordinates() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getPoints() {
        return points;
    }
    public boolean isTheObjective() {
        return isTheObjective;
    }

    public boolean isCompletedByUser() {
        return isCompletedByUser;
    }

    @Nullable
    public String getFormattedDateOfCompletion() {
        return formattedDateOfCompletion;
    }

    public void setIsTheObjective(boolean isTheObjective) {
        this.isTheObjective = isTheObjective;
    }

    public void setIsCompletedByUser(boolean isCompletedByUser) {
        this.isCompletedByUser = isCompletedByUser;
    }

    public void setFormattedDateOfCompletion(@NonNull String date) {
        this.formattedDateOfCompletion = date;
    }


}
