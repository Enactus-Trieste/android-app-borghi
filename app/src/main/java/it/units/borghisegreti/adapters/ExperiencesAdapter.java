package it.units.borghisegreti.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.units.borghisegreti.databinding.CompletedExperienceBinding;
import it.units.borghisegreti.models.Experience;

public class ExperiencesAdapter extends RecyclerView.Adapter<ExperienceViewHolder> {

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
        return new ExperienceViewHolder(CompletedExperienceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        final Experience experience = differ.getCurrentList().get(position);
        holder.bind(experience);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public void submitExperiences(@NonNull List<Experience> experiences) {
        differ.submitList(experiences);
    }

}
