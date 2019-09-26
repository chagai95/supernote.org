package com.example.firebaseui_firestoreexample;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends MyActivity {

    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    Button login;
    Button skipLogin;

    int RC_SIGN_IN = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // added for the traffic light
        onCreateCalled = true;

        swipeToRefresh();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        login = findViewById(R.id.loginButton);
        skipLogin = findViewById(R.id.skipLoginButton);

        MyApp.internetDisabledInternally = false;
        db.enableNetwork();

        TextView textView = findViewById(R.id.goOnlineToLoginExplainingText);
        if (!isNetworkAvailable()) {
            login.setVisibility(View.GONE);
            skipLogin.setVisibility(View.GONE);
        }

        if (isNetworkAvailable())
            textView.setVisibility(View.GONE);

        login.setOnClickListener(v -> {
// Choose authentication providers
            List<AuthUI.IdpConfig> providers = Collections.singletonList(
                    new AuthUI.IdpConfig.EmailBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(), RC_SIGN_IN
            );
        });

        skipLogin.setOnClickListener(v -> mAuth.signInAnonymously()
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in firebaseUser's information
                        db.disableNetwork();
                        MyApp.userSkippedLogin = true;
                        MyApp.internetDisabledInternally = true;
                        DocumentReference documentRef
                                = FirebaseFirestore.getInstance().collection("utils")
                                .document("startAppOffline");
                        documentRef.update("startAppOffline", true);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }));

    }

    // called after login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

//            if the sign in fails in order to check the error
//            IdpResponse response = IdpResponse.fromResultIntent(data);

//            Successfully signed in
            if (resultCode == RESULT_OK) {
                MyApp.userSkippedLogin = false;
                MyApp.internetDisabledInternally = false;

                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                db.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        assert documentSnapshot != null;
                        if (!documentSnapshot.exists())
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(new CloudUser(firebaseUser.getDisplayName() + firebaseUser.getUid()));
                    }

                });

                DocumentReference documentRef
                        = FirebaseFirestore.getInstance().collection("utils")
                        .document("startAppOffline");
                documentRef.update("startAppOffline", false);

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    // added for the traffic light

    boolean onCreateCalled;
    private TrafficLight lastTrafficLightState;

    @Override
    protected void onPause() {
        super.onPause();
        onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // add in MyApp and in NetworkChangeReciever for the traffic light
        MyApp.activityLoginResumed();
        if (!onCreateCalled && MyApp.lastTrafficLightState != lastTrafficLightState)
            recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.activityLoginStopped();
    }

    @Override
    public Resources.Theme getTheme() {
        super.setLastTrafficLightState(lastTrafficLightState);
        Resources.Theme theme = super.getTrafficLightTheme();
        lastTrafficLightState = super.getLastTrafficLightState();
        return theme;
    }

    // until here for the traffic light

    private void swipeToRefresh() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshForLogin);
        pullToRefresh.setOnRefreshListener(() -> {
            recreate();
            pullToRefresh.setRefreshing(false);
        });
    }

}
