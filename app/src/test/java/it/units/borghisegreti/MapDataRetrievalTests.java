package it.units.borghisegreti;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import android.os.Bundle;

import androidx.lifecycle.SavedStateHandle;
import androidx.test.filters.SmallTest;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import it.units.borghisegreti.models.Zone;
import it.units.borghisegreti.viewmodels.MapViewModel;

@SmallTest
public class MapDataRetrievalTests {

    MapViewModel viewModel = new MapViewModel(FirebaseDatabase.getInstance(), SavedStateHandle.createHandle(Bundle.EMPTY, Bundle.EMPTY));
    @Before
    public void init() {
        viewModel.getDatabase().useEmulator("localhost", 9000);
    }

    @Test
    public void testGetZones() {
        List<Zone> obtainedZones = viewModel.getZones().getValue();

    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}