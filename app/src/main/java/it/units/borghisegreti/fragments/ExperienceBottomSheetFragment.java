package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.utils.Database.DB_URL;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentExperienceBottomSheetBinding;
import it.units.borghisegreti.utils.IconBuilder;
import it.units.borghisegreti.viewmodels.ObjectiveDialogViewModel;

public class ExperienceBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String EXPERIENCE_ID = "EXPERIENCE_ID";
    public static final String FRAGMENT_TAG = "EXPERIENCE_BOTTOM_DIALOG";
    private static final String LOG_TAG = "EXP_DIALOG";
    @Nullable
    private String experienceId;
    private FragmentExperienceBottomSheetBinding viewBinding;
    private String objectiveExperienceId;
    private ObjectiveDialogViewModel viewModel;

    public ExperienceBottomSheetFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static ExperienceBottomSheetFragment newInstance(@NonNull String experienceId) {
        ExperienceBottomSheetFragment fragment = new ExperienceBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(EXPERIENCE_ID, experienceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            experienceId = getArguments().getString(EXPERIENCE_ID);
        }
        viewModel = new ViewModelProvider(this, new ObjectiveDialogViewModel.Factory(FirebaseDatabase.getInstance(DB_URL))).get(ObjectiveDialogViewModel.class);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentExperienceBottomSheetBinding.inflate(inflater, container, false);

        viewModel.getExperienceById(Objects.requireNonNull(experienceId, "Experience ID shouldn't be null when the dialog's been initialized")).observe(getViewLifecycleOwner(), experience -> {
            viewBinding.experienceTitle.setText(experience.getName());
            viewBinding.experienceDescription.setText(experience.getDescription());
            viewBinding.experiencePoints.setText(String.format(getString(R.string.gained_points), experience.getPoints()));
            try {
                IconBuilder iconBuilder = new IconBuilder(requireContext(), experience);
                InputStream iconImage = iconBuilder.getExperienceIcon();
                Drawable iconDrawable = Drawable.createFromStream(iconImage, null);
                viewBinding.experienceIcon.setImageDrawable(iconDrawable);
            } catch (IOException e) {
                viewBinding.experienceIcon.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
                Log.e(LOG_TAG, "Unable to load icon image", e);
            }
        });

        viewModel.getObjectiveExperienceId().observe(getViewLifecycleOwner(), objectiveExperienceId -> {
            this.objectiveExperienceId = objectiveExperienceId;
            if (objectiveExperienceId != null) {
                viewBinding.setObjectiveButton.setText(R.string.removeObjective_buttonText);
            } else {
                viewBinding.setObjectiveButton.setText(R.string.setObjective_buttonText);
            }
        });

        viewModel.getCompletedExperiencesMap().observe(getViewLifecycleOwner(), completedExperiences -> {
            if (completedExperiences.containsKey(experienceId)) {
                viewBinding.setObjectiveButton.setVisibility(View.GONE);
                viewBinding.completedTextView.setText(String.format("%s %s", getString(R.string.completed), completedExperiences.get(experienceId).getFormattedDateOfCompletion()));
                viewBinding.completedTextView.setVisibility(View.VISIBLE);
            }
        });

        viewBinding.setObjectiveButton.setOnClickListener(view -> {
            if (objectiveExperienceId == null) {
                viewModel.setObjectiveExperience(experienceId)
                        .addOnSuccessListener(task -> Log.d(LOG_TAG, "New objective successfully uploaded"))
                        .addOnFailureListener(exception -> Log.e(LOG_TAG, "Error while uploading new objective", exception));
            } else {
                viewModel.setObjectiveExperience(null)
                        .addOnSuccessListener(task -> Log.d(LOG_TAG, "New objective successfully uploaded"))
                        .addOnFailureListener(exception -> Log.e(LOG_TAG, "Error while uploading new objective", exception));
            }
        });
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }
}