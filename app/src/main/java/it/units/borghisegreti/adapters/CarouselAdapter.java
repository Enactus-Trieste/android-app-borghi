package it.units.borghisegreti.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import it.units.borghisegreti.databinding.ExperienceDialogCarouselBinding;

public class CarouselAdapter extends ListAdapter<CarouselItem, CarouselItemViewHolder> {

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
        holder.bind(experienceImage);
    }

    public interface ItemListener {
        void onItemClicked(CarouselItem item, int position);
    }
}
