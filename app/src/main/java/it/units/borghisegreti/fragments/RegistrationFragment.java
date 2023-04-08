package it.units.borghisegreti.fragments;

import static it.units.borghisegreti.fragments.LoginFragment.AUTH_TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentRegistrationBinding;
import it.units.borghisegreti.directions.RegistrationFragmentDirections;


public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding viewBinding;
    private FirebaseAuth authentication;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentRegistrationBinding.inflate(inflater, container, false);

        viewBinding.registrationButton.setOnClickListener(new View.OnClickListener() {

            private final AtomicBoolean alreadySentOnce = new AtomicBoolean(false);

            @Override
            public void onClick(View registrationButtonView) {
                hideKeyboard();
                if (!RegistrationFragment.this.validateForm()) {
                    return;
                }
                if (!alreadySentOnce.get()) {
                    alreadySentOnce.set(true);
                    authentication.createUserWithEmailAndPassword(Objects.requireNonNull(viewBinding.registrationEmail.getText()).toString(), viewBinding.registrationPassword.getText().toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(AUTH_TAG, "User " + authentication.getUid() + " created successfully");
                                    NavHostFragment.findNavController(RegistrationFragment.this)
                                            .navigate(RegistrationFragmentDirections.actionRegistrationFragmentToMapsFragment());
                                } else {
                                    Log.w(AUTH_TAG, "Failed to create new user", task.getException());
                                    // always non-null, checked if task has failed
                                    Snackbar.make(RegistrationFragment.this.requireView(), task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
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

    private boolean validateForm() {
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
}