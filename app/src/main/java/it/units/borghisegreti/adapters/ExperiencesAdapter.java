package it.units.borghisegreti.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.CompletedExperienceBinding;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.utils.IconBuilder;

public class ExperiencesAdapter extends RecyclerView.Adapter<ExperiencesAdapter.ExperienceViewHolder> {

    @NonNull
    private static final DiffUtil.ItemCallback<Experience> DIFF_CALLBACK = new DiffUtil.ItemCallback<Experience>() {
        @Override
        public boolean areItemsTheSame(@NonNull Experience oldItem, @NonNull Experience newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Experience oldItem, @NonNull Experience newItem) {
            return oldItem.getId().equals(newItem.getId())
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getEnumType().equals(newItem.getEnumType())
                    && oldItem.getLatitude() == newItem.getLatitude()
                    && oldItem.getLongitude() == newItem.getLongitude()
                    && oldItem.getPoints() == newItem.getPoints();
        }
    };
    public static final String EXP_ADAPTER_TAG = "EXP_ADAPTER";
    @NonNull
    private final AsyncListDiffer<Experience> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);
    @NonNull
    private final Context context;

    public ExperiencesAdapter(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExperienceViewHolder(CompletedExperienceBinding.inflate(LayoutInflater.from(parent.getContext())));
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        final Experience experience = differ.getCurrentList().get(position);
        holder.binding.completedExperienceTitle.setText(experience.getName());
        holder.binding.completedExperienceDescription.setText(experience.getDescription());
        try {
            IconBuilder iconBuilder = new IconBuilder(context, experience);
            InputStream iconStream = iconBuilder.getExperienceIcon();
            Drawable icon = Drawable.createFromStream(iconStream, null);
            holder.binding.completedExperienceLogo.setImageDrawable(icon);
        } catch (IOException e) {
            Log.e(EXP_ADAPTER_TAG, "Unable to load the icon for experience: " + experience, e);
            holder.binding.completedExperienceLogo.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
        }
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public void submitExperiences(@NonNull List<Experience> experiences) {
        differ.submitList(experiences);
    }

    public static class ExperienceViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final CompletedExperienceBinding binding;

        public ExperienceViewHolder(@NonNull CompletedExperienceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
