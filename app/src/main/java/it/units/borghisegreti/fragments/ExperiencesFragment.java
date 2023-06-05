package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.models.Experience.Type.MOUNTAIN;
import static it.units.borghisegreti.models.Experience.Type.NATURALISTIC_AREA;
import static it.units.borghisegreti.models.Experience.Type.PANORAMIC_VIEW;
import static it.units.borghisegreti.models.Experience.Type.POINT_OF_HISTORICAL_INTEREST;
import static it.units.borghisegreti.models.Experience.Type.RESTAURANT;
import static it.units.borghisegreti.models.Experience.Type.RIVER_WATERFALL;
import static it.units.borghisegreti.models.Experience.Type.TYPICAL_FOOD;
import static it.units.borghisegreti.utils.Database.DB_URL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.units.borghisegreti.adapters.ExperiencesAdapter;
import it.units.borghisegreti.databinding.FragmentExperiencesBinding;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.viewmodels.ExperiencesViewModel;

public class ExperiencesFragment extends Fragment {

    private FragmentExperiencesBinding viewBinding;
    private ExperiencesViewModel viewModel;
    @NonNull
    private List<Experience> experiences = Collections.emptyList();
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
        viewModel.getCompletedExperiences().observe(getViewLifecycleOwner(), experiences -> {
            this.experiences = experiences;
            List<Integer> checkedChipIds = viewBinding.experiencesCategories.getCheckedChipIds();
            experiencesAdapter.submitExperiences(getFilteredExperiences(checkedChipIds));
        });
        viewBinding.experiencesCategories.setOnCheckedStateChangeListener((group, checkedIds) -> experiencesAdapter.submitExperiences(getFilteredExperiences(checkedIds)));
        return viewBinding.getRoot();
    }

    @NonNull
    private List<Experience> getFilteredExperiences(@NonNull List<Integer> checkedChipIds) {
        Set<Experience.Type> checkedExperienceTypes = getCheckedExperienceTypes(checkedChipIds);
        List<Experience> filteredExperiences = new ArrayList<>();
        for (Experience experience : experiences) {
            if (checkedExperienceTypes.contains(experience.getEnumType())) {
                filteredExperiences.add(experience);
            }
        }
        return filteredExperiences;
    }

    @NonNull
    private Set<Experience.Type> getCheckedExperienceTypes(@NonNull List<Integer> checkedChipIds) {
        Set<Experience.Type> checkedExperienceTypes = new HashSet<>();
        for (Integer chipId : checkedChipIds) {
            if (chipId.equals(viewBinding.chipMountain.getId())) {
                checkedExperienceTypes.add(MOUNTAIN);
            }
            if (chipId.equals(viewBinding.chipPanoramicView.getId())) {
                checkedExperienceTypes.add(PANORAMIC_VIEW);
            }
            if (chipId.equals(viewBinding.chipNaturalisticArea.getId())) {
                checkedExperienceTypes.add(NATURALISTIC_AREA);
            }
            if (chipId.equals(viewBinding.chipPointOfHistoricalInterest.getId())) {
                checkedExperienceTypes.add(POINT_OF_HISTORICAL_INTEREST);
            }
            if (chipId.equals(viewBinding.chipRestaurant.getId())) {
                checkedExperienceTypes.add(RESTAURANT);
            }
            if (chipId.equals(viewBinding.chipRiverWaterfall.getId())) {
                checkedExperienceTypes.add(RIVER_WATERFALL);
            }
            if (chipId.equals(viewBinding.chipTypicalFood.getId())) {
                checkedExperienceTypes.add(TYPICAL_FOOD);
            }
        }
        return checkedExperienceTypes;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }
}