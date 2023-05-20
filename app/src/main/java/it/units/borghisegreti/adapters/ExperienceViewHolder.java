package it.units.borghisegreti.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.CompletedExperienceBinding;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.utils.IconBuilder;

public class ExperienceViewHolder extends RecyclerView.ViewHolder {
    @NonNull
    private final CompletedExperienceBinding binding;

    public ExperienceViewHolder(@NonNull CompletedExperienceBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(@NonNull Experience experience) {
        binding.completedExperienceTitle.setText(experience.getName());
        binding.completedExperienceDescription.setText(experience.getDescription());
       binding.completedExperiencePoints.setText(String.valueOf(experience.getPoints()));
        try {
            IconBuilder iconBuilder = new IconBuilder(itemView.getContext(), experience);
            InputStream iconStream = iconBuilder.getExperienceIcon();
            Drawable icon = Drawable.createFromStream(iconStream, null);
            binding.completedExperienceLogo.setImageDrawable(icon);
        } catch (IOException e) {
            Log.e(ExperiencesAdapter.EXP_ADAPTER_TAG, "Unable to load the icon for experience: " + experience, e);
            binding.completedExperienceLogo.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
        }
    }
}
