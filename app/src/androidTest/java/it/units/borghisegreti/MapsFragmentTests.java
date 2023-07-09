package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static it.units.borghisegreti.utils.Database.DB_URL;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.filters.LargeTest;

import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import it.units.borghisegreti.fragments.MapsFragment;

@LargeTest
public class MapsFragmentTests {
    private FragmentScenario<MapsFragment> scenario;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(MapsFragment.class, Bundle.EMPTY, R.style.Theme_AdventureMaps, new AuthenticatedFragmentsFactory("test@prova.it", "password"));
        scenario.onFragment(mapsFragment -> FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000));
    }

    @Test
    public void test() {
        IntStream.range(0, 100)
                        .forEach(index -> {
                            onView(withId(R.id.maps_search_button))
                                    .perform(click());
                        });
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }

}
