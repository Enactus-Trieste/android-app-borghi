package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.units.borghisegreti.fragments.LoginFragment;

@LargeTest
public class LoginFragmentAuthenticationTest {

    private static final String USER_EMAIL = "test@prova.it";
    private static final String USER_PASSWORD = "password";
    private FragmentScenario<LoginFragment> scenario;
    private TestNavHostController navController;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(LoginFragment.class, Bundle.EMPTY, R.style.Theme_AdventureMaps);
        navController = new TestNavHostController(ApplicationProvider.getApplicationContext());
        scenario.onFragment(loginFragment -> {
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
            navController.setGraph(R.navigation.navigation_graph);
            navController.setCurrentDestination(R.id.loginFragment);
            Navigation.setViewNavController(loginFragment.requireView(), navController);
        });
    }

    @Test
    public void testAuthentication() {
        onView(withId(R.id.login_username_text))
                .perform(typeText(USER_EMAIL));
        onView(withId(R.id.login_username_text))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.login_password_text))
                .perform(typeText(USER_PASSWORD));
        onView(withId(R.id.login_password_text))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.login_button))
                .perform(click());
        assertEquals(navController.findDestination(R.id.mapsFragment), navController.getCurrentDestination());
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
