package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.filters.LargeTest;

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
        scenario = FragmentScenario.launchInContainer(
                MapsFragment.class, Bundle.EMPTY,
                R.style.Theme_AdventureMaps,
                new AuthenticatedFragmentsFactory("test@prova.it", "password")
        );
    }

    @Test
    public void test() {
        onView(withId(R.id.map))
                .perform(doubleClick());
        onView(withId(R.id.map))
                .perform(doubleClick());
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
