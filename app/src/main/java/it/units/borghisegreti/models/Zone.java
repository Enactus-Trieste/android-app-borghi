package it.units.borghisegreti.models;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Contract;

public class Zone {
    private String name;
    private double latitude;
    private double longitude;

    public Zone(@NonNull String name, @NonNull LatLng coordinates) {
        this.name = name;
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
    }

    // required for Firebase's getValue(Zone.class)
    public Zone() {

    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getCoordinates() {
        return new LatLng(latitude, longitude);
    }
}
