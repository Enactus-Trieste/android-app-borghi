package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import it.units.borghisegreti.R;

public class ExperienceBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String EXPERIENCE_ID = "EXPERIENCE_ID";
    public static final String TAG = "EXPERIENCE_BOTTOM_DIALOG";
    private String experienceId;

    public ExperienceBottomSheetFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static ExperienceBottomSheetFragment newInstance(String experienceId) {
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_experience_bottom_sheet, container, false);
    }
}