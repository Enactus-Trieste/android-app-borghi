package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.viewmodels.MapViewModel.DB_URL;
import static it.units.borghisegreti.viewmodels.UserDataViewModel.USER_DATA_REFERENCE;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;


import it.units.borghisegreti.models.User;

public class RegistrationViewModel extends ViewModel {

    @NonNull
    private final FirebaseDatabase database;
    public RegistrationViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
    }

    public Task<Void> uploadNewUser(@NonNull User newUser) {
        return database.getReference(USER_DATA_REFERENCE)
                .child(newUser.getId())
                .setValue(newUser);
    }
}
