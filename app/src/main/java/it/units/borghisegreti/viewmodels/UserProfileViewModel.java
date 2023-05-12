package it.units.borghisegreti.viewmodels;

import static it.units.borghisegreti.utils.Database.DB_TAG;
import static it.units.borghisegreti.utils.Database.USERNAME_REFERENCE;
import static it.units.borghisegreti.utils.Database.USER_DATA_REFERENCE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Native;
import java.util.Objects;

public class UserProfileViewModel extends ViewModel {

    @NotNull
    private final FirebaseDatabase database;
    @NotNull
    private final SavedStateHandle savedStateHandle;
    @NotNull
    private final String userId;
    @NotNull
    private final MutableLiveData<String> databaseUsername;
    @NotNull
    private final ValueEventListener usernameListener;

    public UserProfileViewModel(@NotNull FirebaseDatabase database, @NotNull SavedStateHandle savedStateHandle) {
        this.database = database;
        this.savedStateHandle = savedStateHandle;
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid(), "User should be already authenticated");
        databaseUsername = new MutableLiveData<>();
        usernameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                databaseUsername.setValue(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DB_TAG, "Error: " + error.getMessage(), error.toException());
            }
        };
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(USERNAME_REFERENCE)
                .addValueEventListener(usernameListener);
    }

    /**
     *
     * @return The username of the current user
     */
    @NonNull
    public LiveData<String> getUsername() {
        return databaseUsername;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(USER_DATA_REFERENCE)
                .child(userId)
                .child(USERNAME_REFERENCE)
                .removeEventListener(usernameListener);
    }

    public static class Factory extends AbstractSavedStateViewModelFactory {

        @NonNull
        private final FirebaseDatabase database;

        public Factory(@NonNull FirebaseDatabase database) {
            this.database = database;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
            return (T) new UserProfileViewModel(database, handle);
        }
    }

}
