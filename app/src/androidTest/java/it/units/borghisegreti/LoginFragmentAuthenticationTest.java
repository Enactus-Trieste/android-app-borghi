package it.units.borghisegreti;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.units.borghisegreti.fragments.LoginFragment;

@LargeTest
public class LoginFragmentAuthenticationTest {

    private FragmentScenario<LoginFragment> scenario;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(LoginFragment.class, Bundle.EMPTY, R.style.Theme_AdventureMaps);
        scenario.onFragment(loginFragment -> FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099));
    }

    @Test
    public void testAuthentication() {
        onView(withId(R.id.login_username_text))
                .perform(typeText("test@prova.it"));
        onView(withId(R.id.login_username_text))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.login_password_text))
                .perform(typeText("password"));
        onView(withId(R.id.login_password_text))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.login_button))
                .perform(click());
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
