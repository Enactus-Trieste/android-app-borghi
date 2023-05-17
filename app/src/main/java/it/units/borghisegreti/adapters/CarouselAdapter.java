package it.units.borghisegreti.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import it.units.borghisegreti.databinding.ExperienceDialogCarouselBinding;

public class CarouselAdapter extends ListAdapter<CarouselAdapter.CarouselItem, CarouselAdapter.CarouselItemViewHolder> {

    private static final DiffUtil.ItemCallback<CarouselItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CarouselItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CarouselItem oldItem, @NonNull CarouselItem newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull CarouselItem oldItem, @NonNull CarouselItem newItem) {
            return oldItem.getDrawableResource() == newItem.getDrawableResource();
        }
    };

    @NonNull
    private final ItemListener listener;

    public CarouselAdapter(@NonNull ItemListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarouselItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CarouselItemViewHolder(ExperienceDialogCarouselBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselItemViewHolder holder, int position) {
        final CarouselItem experienceImage = getItem(position);
        holder.binding.dialogCarouselImage.setImageResource(experienceImage.getDrawableResource());
        holder.binding.getRoot().setOnClickListener(view -> holder.listener.onItemClicked(experienceImage, position));
    }

    public static class CarouselItemViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ExperienceDialogCarouselBinding binding;
        @NonNull
        private final ItemListener listener;

        public CarouselItemViewHolder(@NonNull ExperienceDialogCarouselBinding binding, @NonNull ItemListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }
    }

    public static class CarouselItem {
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

    public interface ItemListener {
        void onItemClicked(CarouselItem item, int position);
    }
}
