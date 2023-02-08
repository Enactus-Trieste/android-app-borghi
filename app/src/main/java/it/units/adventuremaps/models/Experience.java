package it.units.adventuremaps.models;


import com.google.android.gms.maps.model.LatLng;

import it.units.adventuremaps.utils.ExperienceType;

public class Experience {
    private String id;
    private String name;
    private String description;
    private ExperienceType type;
    private LatLng coordinates;
    private int points;
    private boolean isTheObjective;
    private boolean isCompletedByUser;

    public Experience(String id, String name, String description, ExperienceType type, LatLng coordinates, int points) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.coordinates = coordinates;
        this.points = points;
        this.isTheObjective = false;
        this.isCompletedByUser = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ExperienceType getType() {
        return type;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public int getPoints() {
        return points;
    }

    public boolean getIsTheObjective() {
        return isTheObjective;
    }

    public boolean getIsCompletedByUser() {
        return isCompletedByUser;
    }

    public void setIsTheObjective(boolean isTheObjective) {
        this.isTheObjective = isTheObjective;
    }

    public void setIsCompletedByUser(boolean isCompletedByUser) {
        this.isCompletedByUser = isCompletedByUser;
    }


}