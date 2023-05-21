package it.units.borghisegreti.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.units.borghisegreti.databinding.ExperienceDialogCarouselBinding;

public class CarouselItemViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    private final ExperienceDialogCarouselBinding binding;
    @NonNull
    private final CarouselAdapter.ItemListener listener;

    public CarouselItemViewHolder(@NonNull ExperienceDialogCarouselBinding binding, @NonNull CarouselAdapter.ItemListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    public void bind(@NonNull CarouselItem item) {
        binding.dialogCarouselImage.setImageBitmap(item.getImage());
        binding.getRoot().setOnClickListener(view -> listener.onItemClicked(item, getAdapterPosition()));
    }
}
