package it.units.borghisegreti.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentExperienceDialogBinding;

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
        return viewBinding.getRoot();
    }

    // called only when creating the layout in a dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }
}