package it.units.borghisegreti.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.FirebaseDatabase;

public class MapViewModelFactory extends AbstractSavedStateViewModelFactory {

    @NonNull
    private final FirebaseDatabase database;

    public MapViewModelFactory(@NonNull FirebaseDatabase database) {
        this.database = database;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        return (T) new MapViewModel(database, handle);
    }
}
