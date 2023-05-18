package it.units.borghisegreti.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.carousel.CarouselLayoutManager;

import java.util.List;

import it.units.borghisegreti.adapters.CarouselAdapter;
import it.units.borghisegreti.databinding.FragmentExperienceDialogBinding;
import it.units.borghisegreti.fragments.MapsFragment;

public class ExperienceDialogFragment extends DialogFragment {

    private static final String EXPERIENCE_ID = "EXPERIENCE_ID";
    private FragmentExperienceDialogBinding viewBinding;
    private String experienceId;

    public ExperienceDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static ExperienceDialogFragment newInstance(@NonNull String experienceId) {
        Bundle args = new Bundle();
        args.putString(EXPERIENCE_ID, experienceId);
        ExperienceDialogFragment dialogFragment = new ExperienceDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            experienceId = getArguments().getString(EXPERIENCE_ID);
        }
    }

    // called to get the layout, regardless if it's displayed as a dialog or an embedded fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentExperienceDialogBinding.inflate(inflater, container, false);
        viewBinding.dialogCarousel.setLayoutManager(new CarouselLayoutManager());
        viewBinding.dialogCarousel.setNestedScrollingEnabled(false);

        CarouselAdapter adapter = new CarouselAdapter(
                (item, position) -> viewBinding.dialogCarousel.scrollToPosition(position)
        );
        viewBinding.dialogCarousel.setAdapter(adapter);
        List<CarouselAdapter.CarouselItem> testImages = MapsFragment.createItems();
        adapter.submitList(testImages);
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

    // called only when creating the layout in a dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

}