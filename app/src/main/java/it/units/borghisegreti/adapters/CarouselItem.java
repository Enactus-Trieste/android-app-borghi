package it.units.borghisegreti.adapters;

import static it.units.borghisegreti.adapters.CarouselAdapter.CAROUSEL_TAG;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import it.units.borghisegreti.R;

public class CarouselItem {
    @NonNull
    private Bitmap image;

    public CarouselItem(@NonNull Context context, @NonNull String imageFileName) {
        AssetManager assetManager = context.getAssets();
        try (InputStream is = assetManager.open(imageFileName)) {
            image = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            image = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_baseline_image_not_supported_24);
            Log.e(CAROUSEL_TAG, "Unable to decode carousel image", e);
        }
    }

    @NonNull
    public Bitmap getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarouselItem that = (CarouselItem) o;
        return image.equals(that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image);
    }
}
