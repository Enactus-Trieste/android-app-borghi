package it.units.borghisegreti.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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
            @NonNull Type type,
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

    @NonNull
    public LatLng getCoordinates() {
        return new LatLng(latitude, longitude);
    }

    @NonNull
    public Type getEnumType() {
        return Type.valueOf(type);
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

    public void setDateOfCompletion(@NonNull Date date) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ITALY);
        this.formattedDateOfCompletion = dateFormat.format(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experience that = (Experience) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public enum Type {
        PANORAMIC_VIEW,
        RESTAURANT,
        TYPICAL_FOOD,
        RIVER_WATERFALL,
        NATURALISTIC_AREA,
        MOUNTAIN,
        POINT_OF_HISTORICAL_INTEREST
    }
}
