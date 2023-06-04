package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.utils.Database.DB_URL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.firebase.database.FirebaseDatabase;

import it.units.borghisegreti.adapters.ExperiencesAdapter;
import it.units.borghisegreti.databinding.FragmentExperiencesBinding;
import it.units.borghisegreti.viewmodels.ExperiencesViewModel;

public class ExperiencesFragment extends Fragment {

    private FragmentExperiencesBinding viewBinding;
    private ExperiencesViewModel viewModel;

    public ExperiencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ExperiencesViewModel.Factory(FirebaseDatabase.getInstance(DB_URL))).get(ExperiencesViewModel.class);
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
        viewModel.getCompletedExperiences().observe(getViewLifecycleOwner(), experiencesAdapter::submitExperiences);

        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }
}