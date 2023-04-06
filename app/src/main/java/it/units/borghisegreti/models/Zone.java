package it.units.borghisegreti.models;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class Zone {
    @NonNull
    private String name;
    private double latitude;
    private double longitude;

    public Zone(@NonNull String name, @NonNull LatLng coordinates) {
        this.name = name;
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
    }

    // required for Firebase's getValue(Zone.class)
    private Zone() {}

    @NonNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return Double.compare(zone.latitude, latitude) == 0 && Double.compare(zone.longitude, longitude) == 0 && name.equals(zone.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
}
