package it.units.borghisegreti.fragments.dialogs;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import it.units.borghisegreti.R;
import it.units.borghisegreti.adapters.CarouselAdapter;
import it.units.borghisegreti.adapters.CarouselItem;
import it.units.borghisegreti.databinding.FragmentExperienceDialogBinding;
import it.units.borghisegreti.models.Experience;

public class ExperienceDialog {

    @NonNull
    AlertDialog dialog;

    private ExperienceDialog(@NonNull Context context, @NonNull Experience experience) {
        dialog = new MaterialAlertDialogBuilder(context)
                .create();
        FragmentExperienceDialogBinding binding = FragmentExperienceDialogBinding.inflate(LayoutInflater.from(dialog.getContext()));
        binding.dialogCarousel.setLayoutManager(new CarouselLayoutManager());
        CarouselAdapter adapter = new CarouselAdapter(
                (item, position) -> binding.dialogCarousel.scrollToPosition(position)
        );
        binding.dialogClose.setOnClickListener(view -> dialog.dismiss());
        binding.dialogTitle.setText(experience.getName());
        binding.dialogExperienceDescription.setText(experience.getDescription());
        binding.dialogExperiencePoints.setText(String.format(context.getString(R.string.gained_points), experience.getPoints()));
        binding.dialogCarousel.setAdapter(adapter);
        adapter.submitList(createItems());
        binding.dialogCarousel.setNestedScrollingEnabled(false);
        dialog.setView(binding.getRoot());

    }

    @NonNull
    public static AlertDialog getDialogInstance(@NonNull Context context, @NonNull Experience experience) {
        ExperienceDialog experienceDialog = new ExperienceDialog(context, experience);
        return experienceDialog.dialog;
    }

    @NonNull
    private List<CarouselItem> createItems() {
        return Arrays.asList(
                new CarouselItem(dialog.getContext(), "images/nature_alps.jpg"),
                new CarouselItem(dialog.getContext(), "images/restaurant_table.jpg"),
                new CarouselItem(dialog.getContext(), "images/nature_horse.jpg")
        );
    }
}
