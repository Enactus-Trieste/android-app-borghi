package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.divider.MaterialDividerItemDecoration;

import it.units.borghisegreti.R;
import it.units.borghisegreti.adapters.ExperiencesAdapter;
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
        ExperiencesAdapter experiencesAdapter = new ExperiencesAdapter(requireContext());
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(viewBinding.completedExperiencesRecycler.getContext(), LinearLayoutManager.VERTICAL);
        viewBinding.completedExperiencesRecycler.setAdapter(experiencesAdapter);
        viewBinding.completedExperiencesRecycler.addItemDecoration(divider);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewBinding.completedExperiencesRecycler.setLayoutManager(layoutManager);


        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }
}