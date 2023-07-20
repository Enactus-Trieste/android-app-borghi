package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.EMAIL;
import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.PASSWORD;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;
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
                new AuthenticatedFragmentsFactory(EMAIL, PASSWORD)
        );
    }

    @Test
    public void testNumberOfNaturalisticViews() {
        // empty
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }

}
