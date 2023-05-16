package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.utils.Database.DB_URL;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentUserProfileBinding;
import it.units.borghisegreti.viewmodels.UserProfileViewModel;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding viewBinding;
    private UserProfileViewModel viewModel;

    public UserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new UserProfileViewModel.Factory(FirebaseDatabase.getInstance(DB_URL))).get(UserProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }
}