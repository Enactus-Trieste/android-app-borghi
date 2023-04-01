package it.units.borghisegreti.utils;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.io.InputStream;

import it.units.borghisegreti.models.Experience;

public class IconBuilder {

    private Context context;
    private final Experience experience;

    public IconBuilder(Context context, Experience experience) {
        this.context = context;
        this.experience = experience;
    }

    public BitmapDescriptor buildMarkerDescriptor() {
        BitmapDescriptor descriptor;

        switch (experience.getType()) {
            case MOUNTAIN:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/MountainIcon.png");
                break;
            case NATURALISTIC_AREA:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/ForestIcon.png");
                break;
            case PANORAMIC_VIEW:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/PanoramicViewIcon.png");
                break;
            case POINT_OF_HISTORICAL_INTEREST:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/InfoIcon.png");
                break;
            case RESTAURANT:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/RestaurantIcon.png");
                break;
            case RIVER_WATERFALL:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/RiverIcon.png");
                break;
            case TYPICAL_FOOD:
                descriptor = BitmapDescriptorFactory.fromAsset("markers/BasketIcon.png");
                break;
            default:
                descriptor = BitmapDescriptorFactory.defaultMarker();
        }

        if (experience.isTheObjective()) {
            descriptor = BitmapDescriptorFactory.fromAsset("icons/star.png");
        }

        return descriptor;
    }

    public float getMarkerAlpha() {
        if (experience.getIsCompletedByUser()) {
            return 0.5f;
        }
        return 1f;
    }

    public InputStream getExperienceIcon() throws IOException {
        InputStream iconImage;
        switch (experience.getType()) {
            case MOUNTAIN:
                iconImage = context.getAssets().open("icons/MountainIcon.png");
                break;
            case NATURALISTIC_AREA:
                iconImage = context.getAssets().open("icons/ForestIcon.png");
                break;
            case PANORAMIC_VIEW:
                iconImage = context.getAssets().open("icons/PanoramicViewIcon.png");
                break;
            case POINT_OF_HISTORICAL_INTEREST:
                iconImage = context.getAssets().open("icons/InfoIcon.png");
                break;
            case RESTAURANT:
                iconImage = context.getAssets().open("icons/RestaurantIcon.png");
                break;
            case RIVER_WATERFALL:
                iconImage = context.getAssets().open("icons/RiverIcon.png");
                break;
            case TYPICAL_FOOD:
                iconImage = context.getAssets().open("icons/BasketIcon.png");
                break;
            default:
                iconImage = context.getAssets().open("icons/star.png");
        }

        return iconImage;
    }

}
