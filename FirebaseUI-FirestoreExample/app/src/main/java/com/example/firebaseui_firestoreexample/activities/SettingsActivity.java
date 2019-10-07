package com.example.firebaseui_firestoreexample.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.firebaseui_firestoreexample.CloudUser;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends MyActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Settings");

        // added for the traffic light
        onCreateCalled = true;

        autoInternInternetOffWhenSlow();

        startAppOffline();

        username();

        addFriends();

        showFriends();

    }

    private void showFriends() {
        Button button = findViewById(R.id.showFriends);
        button.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, FriendsActivity.class));
        });
    }

    private EditText usernameCheckAvailability;
    private TextView currentUsername;

    @SuppressLint("SetTextI18n")
    private void username() {
        currentUsername = findViewById(R.id.currentUsername);

        usernameCheckAvailability = findViewById(R.id.check_username_availability);
        usernameCheckAvailability.setText(MyApp.myCloudUserData.getCloudUser().getUsername());
        if (isNetworkAvailable() && !MyApp.internetDisabledInternally) {
            db.collection("users").document(MyApp.myCloudUserData.getCloudUser().getUid()).addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) System.err.println("Listen failed: " + e);

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    if (cloudUser != null) {
                        MyApp.myCloudUserData.getCloudUser().setUsername(cloudUser.getUsername());
                        currentUsername.setText("current username: " + MyApp.myCloudUserData.getCloudUser().getUsername());
                        currentUsername.setTextColor(Color.BLACK);
                    }
                }

            });
            usernameCheckAvailability.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }


                @SuppressLint("SetTextI18n")
                @Override
                public void afterTextChanged(Editable s) {
                    String username = s.toString().toLowerCase();
                    usernameCheckAvailability.setTextColor(Color.GRAY);
                    db.collection("users").whereEqualTo("username", username)
                            .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            if (!Objects.requireNonNull(task.getResult()).isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    CloudUser cloudUser = document.toObject(CloudUser.class);
                                    if (!cloudUser.getUsername().equals(MyApp.myCloudUserData.getCloudUser().getUsername())) {
                                        usernameCheckAvailability.setTextColor(Color.RED);
                                        currentUsername.setTextColor(Color.RED);
                                        currentUsername.setText("username: " + username + " is already taken");
                                    } else {
                                        usernameCheckAvailability.setTextColor(Color.parseColor("#36832B"));
                                        currentUsername.setText("current username: " + MyApp.myCloudUserData.getCloudUser().getUsername());
                                        currentUsername.setTextColor(Color.BLACK);
                                    }
                                }
                            } else {
                                usernameCheckAvailability.setTextColor(Color.parseColor("#36832B"));
                                db.collection("users").document(MyApp.myCloudUserData.getCloudUser().getUid()).update("username", username);
                            }
                    });
                }
            });
        } else {
            usernameCheckAvailability.setVisibility(View.GONE);
            currentUsername.setText("no internet connection last username registered:\n" + MyApp.myCloudUserData.getCloudUser().getUsername() + "\n go online to change username");
        }
    }

    boolean startAppOffline;
    private DocumentReference startAppOfflineDocumentRef;

    private void startAppOffline() {
        ToggleButton toggleButton2 = findViewById(R.id.simpleToggleButton2);
        startAppOfflineDocumentRef = db.collection("utils").document("startAppOffline");
        startAppOfflineDocumentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startAppOffline = (boolean) Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData()).get("startAppOffline");
                toggleButton2.setChecked(startAppOffline);
                toggleButton2.setOnClickListener(view -> startAppOfflineDocumentRef.update("startAppOffline", !startAppOffline));

            }
        });
    }

    private void autoInternInternetOffWhenSlow() {
        ToggleButton toggleButton = findViewById(R.id.simpleToggleButton);
        toggleButton.setChecked(MyApp.autoInternInternetOffWhenSlow);
        toggleButton.setOnClickListener(view -> {
            MyApp.autoInternInternetOffWhenSlow = !MyApp.autoInternInternetOffWhenSlow;
            MyApp.currentTrafficLightState = TrafficLight.UNKNOWN;
            int status = NetworkUtil.getConnectivityStatusString(this);
            if (NetworkUtil.networkType == TelephonyManager.NETWORK_TYPE_EDGE && status == NetworkUtil.NETWORK_STATUS_MOBILE)
                if (MyApp.autoInternInternetOffWhenSlow) MyApp.internetDisabledInternally = true;
        });
    }

    private ArrayList<String> usernameSuggestions;

    private void addFriends() {
        usernameSuggestions = new ArrayList<>();
        HashMap<String, CloudUserData> userSuggestions;
        userSuggestions = new HashMap<>();
        userCollRef = db.collection("users");
        userCollRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                assert querySnapshot != null;
                for (DocumentSnapshot documentSnapshot :
                        querySnapshot.getDocuments()) {
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    userSuggestions.put(cloudUser.getUsername(), new CloudUserData(cloudUser, documentSnapshot.getReference()));
                    usernameSuggestions.add(cloudUser.getUsername());
                }
                userSuggestions.remove(MyApp.myCloudUserData.getCloudUser().getUsername());
                usernameSuggestions.remove(MyApp.myCloudUserData.getCloudUser().getUsername());
            }
        });

        AutoCompleteTextView addFriends = findViewById(R.id.addFriends);
        Button addFriendButton = findViewById(R.id.addFriendButton);
        if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
            addFriends.setHint("go online to get username suggestions");
            addFriendButton.setText("add username offline");
        }
        addFriendButton.setOnClickListener(v -> {
            db.collection("users").whereEqualTo("username", addFriends.getText().toString()).get(Source.SERVER).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot result = task.getResult();
                    assert result != null;
                    List<DocumentSnapshot> documentSnapshots = result.getDocuments();
                    if (!documentSnapshots.isEmpty()) {

                        DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                        assert documentSnapshot != null;
                        CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                        assert cloudUser != null;
                        MyApp.myCloudUserData.getDocumentReference().update(
                                "friends", FieldValue.arrayUnion(cloudUser.getUid()))
                                .addOnSuccessListener(aVoid -> Toast.makeText(SettingsActivity.this, cloudUser.getUsername() + " added", Toast.LENGTH_LONG).show());
                    } else
                        Toast.makeText(SettingsActivity.this, addFriends.getText().toString() + " does not exist", Toast.LENGTH_LONG).show();


                }
            });
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernameSuggestions);
        addFriends.setAdapter(adapter);
        addFriends.setOnItemClickListener((parent, view, position, id) -> {
            String newFriendUsername = (String) parent.getItemAtPosition(position);
            CloudUserData newFriendCloudUserData = userSuggestions.get(newFriendUsername);
            assert newFriendCloudUserData != null;
            MyApp.friends.put(newFriendCloudUserData.getCloudUser().getUid(), newFriendCloudUserData);
            MyApp.myCloudUserData.getDocumentReference().update(
                    "friends", FieldValue.arrayUnion(newFriendCloudUserData.getCloudUser().getUid()))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SettingsActivity.this, newFriendUsername + " added", Toast.LENGTH_LONG).show();
                        addFriends.setText("");
                    });
        });
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
        MyApp.activitySettingsResumed();
        if (!onCreateCalled && MyApp.currentTrafficLightState != lastTrafficLightState)
            recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.activitySettingsStopped();
    }


    @Override
    public Resources.Theme getTheme() {
        super.setLastTrafficLightState(lastTrafficLightState);
        Resources.Theme theme = super.getTrafficLightTheme();
        lastTrafficLightState = super.getLastTrafficLightState();
        return theme;
    }

    // until here for the traffic light

}
