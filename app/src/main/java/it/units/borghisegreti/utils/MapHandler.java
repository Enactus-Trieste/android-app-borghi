package it.units.borghisegreti.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.units.borghisegreti.fragments.dialogs.ExperienceDialog;
import it.units.borghisegreti.fragments.exceptions.MarkerNotDrawnException;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.models.Zone;

public class MapHandler {

    private static final int ZONE_TO_EXPERIENCES_ZOOM_THRESHOLD = 12;
    private static final String MAP_DRAW_TAG = "MAP_DRAW";
    private static final LatLng DEFAULT_INITIAL_COORDINATES = new LatLng(45.879688, 13.564337);
    private static final float DEFAULT_INITIAL_ZOOM = 8f;
    private static final float ZONE_ANIMATION_ZOOM = 12f;
    private static final float EXPERIENCE_ANIMATION_ZOOM = 16f;
    private static final float OBJECTIVE_ELEVATION = 1f;
    private static final float MIN_ZOOM = 7f;
    @NonNull
    private final Context context;
    @NonNull
    private final GoogleMap map;
    @NonNull
    private final Map<Marker, Zone> zonesOnTheMapByMarker = new HashMap<>();
    @NonNull
    private final Map<Marker, Experience> experiencesOnTheMapByMarker = new HashMap<>();
    @Nullable
    private String objectiveExperienceId;

    @SuppressLint("MissingPermission")
    public MapHandler(@NonNull Context context,
                      @NonNull GoogleMap map,
                      @Nullable Double initialLatitude,
                      @Nullable Double initialLongitude,
                      @Nullable Float initialZoom) {
        this.context = context;
        this.map = map;

        map.setMinZoomPreference(MIN_ZOOM);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);
        if (initialLatitude != null && initialLongitude != null && initialZoom != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initialLatitude, initialLongitude), initialZoom));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_INITIAL_COORDINATES, DEFAULT_INITIAL_ZOOM));
        }
        setupMapListeners();
    }

    private void setupMapListeners() {
        map.setOnMarkerClickListener(marker -> {
            Zone zone = zonesOnTheMapByMarker.get(marker);
            Experience experience = experiencesOnTheMapByMarker.get(marker);
            if (zone != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZONE_ANIMATION_ZOOM));
                return true;
            } else if (experience != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), EXPERIENCE_ANIMATION_ZOOM), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        AlertDialog dialog = ExperienceDialog.getDialogInstance(context, experience);
                        dialog.show();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                return true;
            }
            return false;
        });
        map.setOnCameraMoveListener(this::showMarkers);
    }

    private void showMarkers() {
        if (map.getCameraPosition().zoom < ZONE_TO_EXPERIENCES_ZOOM_THRESHOLD) {
            experiencesOnTheMapByMarker.keySet().forEach(marker -> marker.setVisible(false));
            zonesOnTheMapByMarker.keySet().forEach(marker -> marker.setVisible(true));
        } else {
            experiencesOnTheMapByMarker.keySet().forEach(marker -> marker.setVisible(true));
            zonesOnTheMapByMarker.keySet().forEach(marker -> marker.setVisible(false));
        }
    }

    public void drawExperienceMarkers(@NonNull List<Experience> experiences) {
        for (Experience experience : experiences) {
            if (!experiencesOnTheMapByMarker.containsValue(experience)) {
                try {
                    drawExperienceMarker(experience);
                } catch (MarkerNotDrawnException e) {
                    Log.e(MAP_DRAW_TAG, e.getMessage(), e);
                }
            } else {
                Log.d(MAP_DRAW_TAG, "Marker for experience " + experience + " already on the map, not drawing");
            }
        }
    }

    private void drawExperienceMarker(@NonNull Experience experience) throws MarkerNotDrawnException {
        Marker experienceMarker = map.addMarker(new MarkerOptions()
                .position(experience.getCoordinates())
                .title(experience.getName())
                .snippet(experience.getDescription()));
        if (experienceMarker == null) {
            throw new MarkerNotDrawnException("Error while drawing marker for experience " + experience);
        } else {
            experiencesOnTheMapByMarker.put(experienceMarker, experience);
            IconBuilder iconBuilder = new IconBuilder(context, experience);
            experienceMarker.setIcon(iconBuilder.buildMarkerDescriptor());
            experienceMarker.setAlpha(iconBuilder.getMarkerAlpha());
            if (map.getCameraPosition().zoom < ZONE_TO_EXPERIENCES_ZOOM_THRESHOLD) {
                experienceMarker.setVisible(false);
            }
            if (experience.getId().equals(objectiveExperienceId)) {
                experienceMarker.setZIndex(OBJECTIVE_ELEVATION);
            }
        }
    }

    public void drawZoneMarkers(@NonNull List<Zone> zones) {
        for (Zone zone : zones) {
            if (!zonesOnTheMapByMarker.containsValue(zone)) {
                try {
                    drawZoneMarker(zone);
                } catch (MarkerNotDrawnException e) {
                    Log.e(MAP_DRAW_TAG, e.getMessage(), e);
                }
            } else {
                Log.d(MAP_DRAW_TAG, "Marker for zone " + zone + " already on the map, not drawing");
            }
        }
    }

    private void drawZoneMarker(@NonNull Zone zone) throws MarkerNotDrawnException {
        Marker zoneMarker = map.addMarker(new MarkerOptions()
                .position(zone.getCoordinates())
                .title(zone.getName())
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromAsset("markers/TaipanaIcon.png")));
        if (zoneMarker == null) {
            throw new MarkerNotDrawnException("Error while drawing marker for zone " + zone);
        } else {
            zonesOnTheMapByMarker.put(zoneMarker, zone);
            if (map.getCameraPosition().zoom >= ZONE_TO_EXPERIENCES_ZOOM_THRESHOLD) {
                zoneMarker.setVisible(false);
            }
        }
    }

    public void submitObjectiveId(@NonNull String experienceId) {
        objectiveExperienceId = experienceId;
        Marker marker = findMarkerAssociatedToExperience(experienceId);
        if (marker != null) {
            marker.setZIndex(OBJECTIVE_ELEVATION);
        } else {
            Log.w(MAP_DRAW_TAG, "Experience with id " + experienceId + " not found on the map");
        }
    }

    @Nullable
    private Marker findMarkerAssociatedToExperience(@NonNull String experienceId) {
        Optional<Map.Entry<Marker, Experience>> optionalMarkerForExperience = experiencesOnTheMapByMarker.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(experienceId))
                .findFirst();
        return optionalMarkerForExperience.map(Map.Entry::getKey).orElse(null);
    }

    @NonNull
    public GoogleMap getMap() {
        return map;
    }
}
