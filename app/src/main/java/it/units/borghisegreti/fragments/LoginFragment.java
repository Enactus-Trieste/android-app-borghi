package it.units.borghisegreti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.borghisegreti.R;
import it.units.borghisegreti.databinding.FragmentLoginBinding;
import it.units.borghisegreti.directions.LoginFragmentDirections;

public class LoginFragment extends Fragment {

    private FirebaseAuth authentication;
    private FragmentLoginBinding viewBinding;
    public static final String AUTH_TAG = "AUTH";

    public LoginFragment() {
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
        viewBinding = FragmentLoginBinding.inflate(inflater, container, false);

        viewBinding.signUpButton.setOnClickListener(registrationButtonView ->
                NavHostFragment.findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()));
        viewBinding.loginButton.setOnClickListener(new View.OnClickListener() {
            private final AtomicBoolean alreadySentOnce = new AtomicBoolean(false);

            @Override
            public void onClick(View loginButtonView) {
                hideKeyboard();
                if (!LoginFragment.this.inputValidation()) {
                    return;
                }
                if (!alreadySentOnce.get()) {
                    alreadySentOnce.set(true);
                    // always non-null on user input, checked with inputValidation
                    authentication.signInWithEmailAndPassword(Objects.requireNonNull(viewBinding.loginUsernameText.getText()).toString(), Objects.requireNonNull(viewBinding.loginPasswordText.getText()).toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(AUTH_TAG, "Sign-in successful");
                                    NavHostFragment.findNavController(LoginFragment.this).navigate(LoginFragmentDirections.actionLoginFragmentToMapsFragment());
                                } else {
                                    Log.w(AUTH_TAG, "Sign-in failed", task.getException());
                                    Snackbar.make(LoginFragment.this.requireView(), Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
                                    alreadySentOnce.set(false);
                                }
                            });
                } else {
                    Log.d(AUTH_TAG, "Sign-in request already sent once without failing, waiting for response");
                }
            }
        });

        return viewBinding.getRoot();
    }

    private void hideKeyboard() {
        viewBinding.loginUsernameLayout.setEnabled(false);
        viewBinding.loginUsernameLayout.setEnabled(true);
        viewBinding.loginPasswordLayout.setEnabled(false);
        viewBinding.loginPasswordLayout.setEnabled(true);
    }

    private boolean inputValidation() {
        boolean isInputValid = true;
        Editable email = viewBinding.loginUsernameText.getText();
        Editable password = viewBinding.loginPasswordText.getText();

        if (TextUtils.isEmpty(email)) {
            viewBinding.loginUsernameLayout.setError(getString(R.string.field_required));
            isInputValid = false;
        } else {
            viewBinding.loginUsernameLayout.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            viewBinding.loginPasswordLayout.setError(getString(R.string.field_required));
            isInputValid = false;
        } else {
            viewBinding.loginPasswordLayout.setError(null);
        }
        return isInputValid;
    }
}