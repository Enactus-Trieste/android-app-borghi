package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.fragments.LoginFragment.AUTH_TAG;
import static it.units.borghisegreti.fragments.directions.RegistrationFragmentDirections.actionRegistrationFragmentToMapsFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentRegistrationBinding;
import it.units.borghisegreti.fragments.directions.RegistrationFragmentDirections;
import it.units.borghisegreti.models.User;
import it.units.borghisegreti.viewmodels.RegistrationViewModel;


public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding viewBinding;
    private FirebaseAuth authentication;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        authentication = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentRegistrationBinding.inflate(inflater, container, false);

        viewBinding.registrationButton.setOnClickListener(new View.OnClickListener() {

            // used to prevent triggering the request multiple times
            private final AtomicBoolean alreadySentOnce = new AtomicBoolean(false);

            @Override
            public void onClick(View registrationButtonView) {
                hideKeyboard();
                if (!RegistrationFragment.this.isInputFormValid()) {
                    return;
                }
                if (!alreadySentOnce.get()) {
                    alreadySentOnce.set(true);
                    authentication.createUserWithEmailAndPassword(
                                    Objects.requireNonNull(viewBinding.registrationEmail.getText()).toString(),
                                    Objects.requireNonNull(viewBinding.registrationPassword.getText(), "User input should be validated first").toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(AUTH_TAG, "User " + task.getResult().getUser() + " created successfully");
                                    User newUser = new User(
                                            Objects.requireNonNull(authentication.getUid(), "User should be successfully logged-in"),
                                            viewBinding.registrationEmail.getText().toString());
                                    RegistrationViewModel viewModel = new ViewModelProvider(RegistrationFragment.this).get(RegistrationViewModel.class);
                                    viewModel.uploadNewUser(newUser)
                                            .addOnSuccessListener(newUserTask -> Log.d(AUTH_TAG, "User " + newUser + " added to database"))
                                            .addOnFailureListener(exception -> Log.w(AUTH_TAG, "Unable to add user " + newUser + " to database", exception));
                                    NavHostFragment.findNavController(RegistrationFragment.this)
                                            .navigate(actionRegistrationFragmentToMapsFragment());
                                } else {
                                    Log.w(AUTH_TAG, "Failed to create new user", task.getException());
                                    // always non-null, checked if task has failed
                                    Snackbar.make(RegistrationFragment.this.requireView(),
                                            Objects.requireNonNull(Objects.requireNonNull(task.getException(), "Task completed successfully, no exception available").getLocalizedMessage(), "No localized message available"),
                                            Snackbar.LENGTH_LONG).show();
                                    alreadySentOnce.set(false);
                                }
                            });
                } else {
                    Log.d(AUTH_TAG, "Sign-up request already sent once without failing, waiting for response");
                }
            }
        });
        viewBinding.cancelRegistrationButton.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return viewBinding.getRoot();
    }

    private void hideKeyboard() {
        viewBinding.registrationEmailLayout.setEnabled(false);
        viewBinding.registrationEmailLayout.setEnabled(true);
        viewBinding.registrationEmailConfirmLayout.setEnabled(false);
        viewBinding.registrationEmailConfirmLayout.setEnabled(true);
        viewBinding.registrationPasswordLayout.setEnabled(false);
        viewBinding.registrationPasswordLayout.setEnabled(true);
        viewBinding.registrationPasswordConfirmLayout.setEnabled(false);
        viewBinding.registrationPasswordConfirmLayout.setEnabled(true);
    }

    private boolean isInputFormValid() {
        boolean isFormValid = true;
        Editable email = viewBinding.registrationEmail.getText();
        Editable confirmEmail = viewBinding.registrationEmailConfirm.getText();
        Editable password = viewBinding.registrationPassword.getText();
        Editable confirmPassword = viewBinding.registrationPasswordConfirm.getText();

        
        if (TextUtils.isEmpty(email)) {
            viewBinding.registrationEmailLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            viewBinding.registrationEmailLayout.setError(null);
            if (!TextUtils.equals(email, confirmEmail)) {
                viewBinding.registrationEmailLayout.setError(getString(R.string.email_mismatch));
                viewBinding.registrationEmailConfirmLayout.setError(getString(R.string.email_mismatch));
                isFormValid = false;
            } else {
                viewBinding.registrationEmailLayout.setError(null);
                viewBinding.registrationEmailConfirmLayout.setError(null);
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewBinding.registrationEmailLayout.setError(getString(R.string.email_bad_format));
                    isFormValid = false;
                } else {
                    viewBinding.registrationEmailLayout.setError(null);
                }
            }
        }
        if (TextUtils.isEmpty(password)) {
            viewBinding.registrationPasswordLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            viewBinding.registrationPasswordLayout.setError(null);
            if (!TextUtils.equals(password, confirmPassword)) {
                viewBinding.registrationPasswordLayout.setError(getString(R.string.password_mismatch));
                viewBinding.registrationPasswordConfirmLayout.setError(getString(R.string.password_mismatch));
                isFormValid = false;
            } else {
                viewBinding.registrationPasswordLayout.setError(null);
                viewBinding.registrationPasswordConfirmLayout.setError(null);
                if (password.length() < 6) {
                    viewBinding.registrationPasswordLayout.setError(getString(R.string.password_weak));
                    isFormValid = false;
                } else {
                    viewBinding.registrationPasswordLayout.setError(null);
                }
            }
        }
        return isFormValid;
    }

    @Override
    public void onDestroyView() {
        viewBinding = null;
        super.onDestroyView();
    }
}