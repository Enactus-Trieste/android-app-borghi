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

import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.units.borghisegreti.adapters.ExperiencesAdapter;
import it.units.borghisegreti.databinding.FragmentExperiencesBinding;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.viewmodels.ExperiencesViewModel;

public class ExperiencesFragment extends Fragment {

    private FirebaseDatabase database;
    private FragmentExperiencesBinding viewBinding;
    private ExperiencesViewModel viewModel;
    @NonNull
    private List<Experience> experiences = Collections.emptyList();

    public ExperiencesFragment() {
        // Required empty public constructor
    }

    private ExperiencesFragment(@NonNull FirebaseDatabase database) {
        this.database = database;
    }

    @NonNull
    public static ExperiencesFragment newInstance(@NonNull FirebaseDatabase database) {
        return new ExperiencesFragment(database);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ExperiencesViewModel.Factory(database == null ? FirebaseDatabase.getInstance(DB_URL) : database)).get(ExperiencesViewModel.class);
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
            experiencesAdapter.submitExperiences(filterExperiencesByCheckedChipIds(checkedChipIds));
        });
        viewBinding.experiencesCategories.setOnCheckedStateChangeListener((group, checkedIds) -> experiencesAdapter.submitExperiences(filterExperiencesByCheckedChipIds(checkedIds)));
        return viewBinding.getRoot();
    }

    @NonNull
    private List<Experience> filterExperiencesByCheckedChipIds(@NonNull List<Integer> checkedChipIds) {
        Set<Experience.Type> checkedExperienceTypes = getCheckedExperienceTypesFromCheckedChipIds(checkedChipIds);
        return experiences.stream()
                .filter(experience -> checkedExperienceTypes.contains(experience.getEnumType()))
                .collect(Collectors.toList());
    }

    @NonNull
    private Set<Experience.Type> getCheckedExperienceTypesFromCheckedChipIds(@NonNull List<Integer> checkedChipIds) {
        return checkedChipIds.stream()
                .map(chipId -> {
                    if (chipId.equals(viewBinding.chipMountain.getId())) {
                        return MOUNTAIN;
                    } else if (chipId.equals(viewBinding.chipPanoramicView.getId())) {
                        return PANORAMIC_VIEW;
                    } else if (chipId.equals(viewBinding.chipNaturalisticArea.getId())) {
                        return NATURALISTIC_AREA;
                    } else if (chipId.equals(viewBinding.chipPointOfHistoricalInterest.getId())) {
                        return POINT_OF_HISTORICAL_INTEREST;
                    } else if (chipId.equals(viewBinding.chipRestaurant.getId())) {
                        return RESTAURANT;
                    } else if (chipId.equals(viewBinding.chipRiverWaterfall.getId())) {
                        return RIVER_WATERFALL;
                    } else if (chipId.equals(viewBinding.chipTypicalFood.getId())) {
                        return TYPICAL_FOOD;
                    }
                    throw new IllegalStateException("No corresponding experience type found for the checked chip");
                })
                .collect(Collectors.toSet());
    }

    @Override
    public void onDestroyView() {
        viewBinding = null;
        super.onDestroyView();
    }
}