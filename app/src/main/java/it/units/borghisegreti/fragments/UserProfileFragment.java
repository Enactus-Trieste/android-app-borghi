package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.utils.Database.DB_URL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentUserProfileBinding;
import it.units.borghisegreti.viewmodels.UserProfileViewModel;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding viewBinding;
    private UserProfileViewModel viewModel;
    private FirebaseAuth authentication;
    private FirebaseDatabase database;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    private UserProfileFragment(@NonNull FirebaseDatabase database) {
        this.database = database;
    }

    @NonNull
    public static UserProfileFragment newInstance(@NonNull FirebaseDatabase database) {
        return new UserProfileFragment(database);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new UserProfileViewModel.Factory(database == null ? FirebaseDatabase.getInstance(DB_URL) : database)).get(UserProfileViewModel.class);
        authentication = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentUserProfileBinding.inflate(inflater, container, false);
        viewBinding.logOutButton.setOnClickListener(view -> {
            authentication.signOut();
            NavHostFragment.findNavController(this).navigate(new ActionOnlyNavDirections(R.id.action_global_loginFragment));
        });
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        viewBinding = null;
        super.onDestroyView();
    }
}