package it.units.borghisegreti.adapters;

import androidx.annotation.DrawableRes;

import java.util.Objects;

public class CarouselItem {
    @DrawableRes
    private final int drawableResource;

    public CarouselItem(@DrawableRes int drawableResource) {
        this.drawableResource = drawableResource;
    }

    public int getDrawableResource() {
        return drawableResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarouselItem that = (CarouselItem) o;
        return drawableResource == that.drawableResource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(drawableResource);
    }
}
