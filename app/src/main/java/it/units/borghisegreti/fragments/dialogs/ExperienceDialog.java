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
                ((item, position) -> binding.dialogCarousel.scrollToPosition(position))
        );
        binding.dialogClose.setOnClickListener(view -> dialog.dismiss());
        binding.dialogTitle.setText(experience.getName());
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
    private static List<CarouselAdapter.CarouselItem> createItems() {
        return Arrays.asList(
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24),
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24),
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24),
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24),
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24),
                new CarouselAdapter.CarouselItem(R.drawable.ic_baseline_image_not_supported_24)
        );
    }
}
