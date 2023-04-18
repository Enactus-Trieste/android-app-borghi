package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentExperiencesBinding;

public class ExperiencesFragment extends Fragment {

    private FragmentExperiencesBinding viewBinding;

    public ExperiencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentExperiencesBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }
}