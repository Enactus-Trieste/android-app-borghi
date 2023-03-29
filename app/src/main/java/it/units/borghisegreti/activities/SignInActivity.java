package it.units.borghisegreti.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Collections;
import java.util.Objects;

import it.units.borghisegreti.R;


public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SIGN_IN";
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "User authentication: " + user);
            Intent mainActivityIntent = new Intent(this, MapsActivity.class);
            startActivity(mainActivityIntent);
            finish();
        } else {
            if (response == null) {
                Log.d(TAG, "User canceled authentication");
            } else{
                Log.d(TAG, "Error: " +
                        Objects.requireNonNull(response.getError())
                                .getLocalizedMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_sign_in);

        Button logInBtn = findViewById(R.id.logInButton);

        logInBtn.setOnClickListener(logInBtnClickListener);
    }

    private final OnClickListener logInBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                            Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build())
                    )
                    .setIsSmartLockEnabled(false)
                    .build();
            signInLauncher.launch(signInIntent);
        }
    };
}
