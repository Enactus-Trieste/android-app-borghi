package it.units.borghisegreti.utils;

import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.AUTH_PORT;
import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.DATABASE_PORT;
import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.HOST;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import it.units.borghisegreti.fragments.ExperiencesFragment;
import it.units.borghisegreti.fragments.MapsFragment;
import it.units.borghisegreti.fragments.UserProfileFragment;

public class AuthenticatedFragmentsFactory extends FragmentFactory {

    private static final String TEST_AUTH_TAG = "TEST_AUTH";

    public AuthenticatedFragmentsFactory(@NonNull String email, @NonNull String password) {
        super();
        FirebaseAuth authentication = FirebaseAuth.getInstance();
        authentication.useEmulator(HOST, AUTH_PORT);
        authentication.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> Log.d(TEST_AUTH_TAG, "User authenticated"))
                .addOnFailureListener(exception -> Log.e(TEST_AUTH_TAG, "Error while authenticating", exception));
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Class<? extends Fragment> fragmentClass = loadFragmentClass(classLoader, className);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.useEmulator(HOST, DATABASE_PORT);
        if (MapsFragment.class.equals(fragmentClass)) {
            return MapsFragment.newInstance(database);
        } else if (UserProfileFragment.class.equals(fragmentClass)) {
            return UserProfileFragment.newInstance(database);
        } else if (ExperiencesFragment.class.equals(fragmentClass)) {
            return ExperiencesFragment.newInstance(database);
        }
        return super.instantiate(classLoader, className);
    }
}
