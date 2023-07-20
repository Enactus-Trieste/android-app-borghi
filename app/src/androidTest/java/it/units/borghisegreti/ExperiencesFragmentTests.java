package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.EMAIL;
import static it.units.borghisegreti.utils.FirebaseEmulatorsConfiguration.PASSWORD;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.units.borghisegreti.fragments.ExperiencesFragment;

@LargeTest
public class ExperiencesFragmentTests {

    private FragmentScenario<ExperiencesFragment> scenario;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(
                ExperiencesFragment.class,
                Bundle.EMPTY,
                R.style.Theme_AdventureMaps,
                new AuthenticatedFragmentsFactory(EMAIL, PASSWORD)
        );
    }

    @Test
    public void testRiverWaterfallExperienceExists() {
        onView(withTagValue(is("ex003")))
                .check(matches(isDisplayed()))
                .check(matches(hasChildCount(4)))
                .check(matches(hasDescendant(withText("Sorgenti del Natisone"))))
                .check(matches(hasDescendant(withText("Le limpide acque di questo fiume demarcano il confine tra Italia e Slovenia fino alle Valli del Natisone."))))
                .check(matches(hasDescendant(allOf(isAssignableFrom(ImageView.class), withId(R.id.completed_experience_logo)))))
                .check(matches(hasDescendant(withText("45"))));
    }

    @Test
    public void testPanoramicViewExperienceExists() {
        onView(withTagValue(is("ex012")))
                .check(matches(isDisplayed()))
                .check(matches(hasChildCount(4)))
                .check(matches(hasDescendant(withText("Vista panoramica di Montemaggiore"))))
                .check(matches(hasDescendant(withText("Vista sul paese di Montemaggiore di Taipana"))))
                .check(matches(hasDescendant(allOf(isAssignableFrom(ImageView.class), withId(R.id.completed_experience_logo)))))
                .check(matches(hasDescendant(withText("20"))));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
