package com.example.firebaseui_firestoreexample.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.firebaseui_firestoreexample.CloudUser;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.LocationReminderData;
import com.example.firebaseui_firestoreexample.utils.MyDatePickerFragment;
import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.firestore_data.UserReminderData;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.example.firebaseui_firestoreexample.reminders.UserReminder;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.firestore_data.OfflineNoteData;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class EditNoteActivity extends MyActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;

    private DocumentReference documentRef;
    public static ListenerRegistration registration;

    TextWatcher textWatcherTitle;
    TextWatcher textWatcherDescription;

    OfflineNoteData offlineNoteData;

    Context c = this;

    boolean onCreateCalled;
    private boolean keepOffline;
    private TrafficLight lastTrafficLightState;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    AlertDialog alertDialogForSharingWithAnotherUser;
    private boolean newNote;
    private LinkedList<AlertDialog> alertList;


    AlertDialog addReminderAlertDialog = null;
    boolean firstOpenSpinner = true;

    // key is the username not user id!
    HashMap<String, CloudUserData> notifyUserSuggestions = new HashMap<>();
    ArrayList<String> sharedUsernames = new ArrayList<>();

    String[] timeType;


//    private int cursor;
//    private boolean changeCursorPositionBack;


    @SuppressLint("ClickableViewAccessibility") // to complicated and only for blind people.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

//        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.pullToRefreshEditNoteActivity);
//        swipeToRefresh(swipeRefreshLayout);


        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close); // added Objects.requireNonNull to avoid warning

        alertList = new LinkedList<>();
        onCreateCalled = true;

        editTextTitle = findViewById(R.id.edit_text_title);

        editTextTitle.setOnClickListener(v -> editTextTitle.setCursorVisible(true));
        editTextTitle.setOnTouchListener((v, event) -> {
            editTextTitle.setCursorVisible(true);
            return false;
        });
        editTextDescription = findViewById(R.id.edit_text_description);


        newNote = getIntent().getBooleanExtra("newNote", false);
        if (newNote) {
            setTitle("Add note");
            editTextDescription.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            CollectionReference notesCollRef = FirebaseFirestore.getInstance()
                    .collection("notes");
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            db.enableNetwork();
            assert firebaseUser != null;
            notesCollRef.add(new Note("", "", new Timestamp(new Date()), firebaseUser.getUid()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            assert documentReference != null;
                            documentRef = documentReference;
                            offlineNoteData = new OfflineNoteData(documentReference);
                            MyApp.allNotes.put(documentReference.getId(), offlineNoteData);
                            if (MyApp.internetDisabledInternally)
                                db.disableNetwork();
                            if (isNetworkAvailable() && !MyApp.internetDisabledInternally)
                                setNoteWithConnection();

                            if (!isNetworkAvailable() || MyApp.internetDisabledInternally)
                                setNoteWithoutConnection();
                        }
                    });
        } else {
            String noteID = Objects.requireNonNull(getIntent().getStringExtra("noteID"));
            offlineNoteData = Objects.requireNonNull(MyApp.allNotes.get(noteID));
            documentRef = offlineNoteData.getDocumentReference();
            setTitle("Edit note");
            editTextTitle.setCursorVisible(false);
        }


        textWatchers();

        if (!MyApp.userSkippedLogin) {
            if (MyApp.myCloudUserData != null)
                notifyUserSuggestions.put(MyApp.myCloudUserData.getCloudUser().getUsername(), MyApp.myCloudUserData);
            loadSharedUsers();
        }

    }

    private void loadSharedUsers() {
        if (!newNote)
            for (String s :
                    offlineNoteData.getNote().getShared()) {
                db.collection("users").document(s).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        assert documentSnapshot != null;
                        CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                        assert cloudUser != null;
                        notifyUserSuggestions.put(cloudUser.getUsername(), new CloudUserData(cloudUser, documentSnapshot.getReference()));
                        if (!sharedUsernames.contains(cloudUser.getUsername()))
                            sharedUsernames.add(cloudUser.getUsername());
                    }
                });
            }
    }

    private void textWatchers() {


        /*cursor = 0;
        changeCursorPositionBack = false;*/
        textWatcherTitle = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                cursor = editTextTitle.getSelectionStart();
//                perhaps send the cursor position to the the server and change the cursor everywhere.
//                registration.remove();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /*if(changeCursorPositionBack){
                    editTextTitle.setSelection(cursor);
                    changeCursorPositionBack = false;
                }*/


//              onCreateCalledForTextWatcher is only to stop the saving of data when initializing the textWatcher
//              (I assume it initializes but this might actually be called by another method - perhaps in onResume)
                if (isNetworkAvailable() && !MyApp.internetDisabledInternally) {
//                        when online uploading directly to the main note history list.
                    documentRef.update("history", FieldValue.arrayUnion(editable.toString())).addOnSuccessListener(aVoid -> successfulUpload())
                            .addOnFailureListener(e -> unsuccessfulUpload(e));
//                        because the listener is on we have to check the text we want to upload is not already online otherwise we end up in an endless loop.
//                        the problem is this takes time and happens in a listener and not linear, hopefully method hasPendingWrites
//                        has solved this but it might come back and bite me in the ass.
                    documentRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            Note note = documentSnapshot.toObject(Note.class);
                            assert note != null;
                            offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
                            if (!note.getTitle().equals(editable.toString()))
                                documentRef.update("title", editable.toString());
                        }
                    });
                }
                if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
//                        first we update the title field so when we come back to the note even without internet it will show the new value.
                    documentRef.update("title", editable.toString());
//                        then we push a new note version to the sub collection of the note, which is named the id of the device - this is our unique collection
//                        for the changes on this device.
//                        the sub collection is created if it had not existed before.
//                        calling get() just to to be able to convert the docRef to an object so we can copy the note using method copy note.
                    documentRef.get(Source.CACHE).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                            assert note != null;
                            offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
                            documentRef.collection(MyApp.getDeviceID(c) + MyApp.userUid).add(note.newNoteVersion());
                        }
                    });

                }
            }
        };
        editTextTitle.addTextChangedListener(textWatcherTitle);
        textWatcherDescription = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (documentRef != null)
                    documentRef.update(
                            "description", editable.toString()
                    );
            }
        };
        editTextDescription.addTextChangedListener(textWatcherDescription);
    }

    private void unsuccessfulUpload(Exception e) {
        makeText(this, "failed to upload to note history: " + e.getMessage(), LENGTH_SHORT).show();
    }

    private void successfulUpload() {
//          createFirestoreListener();
//        makeText(this, "uploaded successfully", LENGTH_SHORT).show();
    }

    private void chooseBetweenServerDataAndLocalData(String serverData) {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("chooseBetweenServerDataAndLocalData");
        alert.setMessage("Server data: " + serverData + "\n" + "Local data: " + editTextTitle.getText().toString());
// Create TextView
        final TextView input = new TextView(c);
        alert.setView(input);

        alert.setPositiveButton("Server data", (dialog, whichButton) -> {
            editTextTitle.setText(serverData);
//            changing the history in case the main note history is not yet merged with a different devices note history
            documentRef.update("history", FieldValue.arrayUnion(serverData)).addOnSuccessListener(aVoid -> successfulUpload())
                    .addOnFailureListener(this::unsuccessfulUpload);
            for (AlertDialog alertDialog :
                    alertList) {
                alertDialog.cancel();
            }
            recreate();
        });

        alert.setNegativeButton("Local data", (dialog, whichButton) -> {
            documentRef.update("title", editTextTitle.getText().toString());

            for (AlertDialog alertDialog :
                    alertList) {
                alertDialog.cancel();
            }
            recreate();
        });
        AlertDialog alertDialog = alert.show();
        alertList.add(alertDialog);
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, menu);

        timeType = new String[]{"time", "location", "user", "whatsapp"};
        if (MyApp.userSkippedLogin) {
            setMenuForUserSkippedLogin(menu);
            timeType = new String[]{"time", "location", "whatsapp"};
        }

        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle_in_note_activity);
        if (MyApp.internetDisabledInternally)
            appInternInternetOffToggleMenuItem.setTitle("go online");
        else
            appInternInternetOffToggleMenuItem.setTitle("go offline");

        MenuItem shareWithAnotherUserMenuItem = menu.findItem(R.id.share_with_another_user);
        if (!isNetworkAvailable() || MyApp.internetDisabledInternally)
            shareWithAnotherUserMenuItem.setTitle("share with friends");

        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_note:
                saveNote();
                return true;
            case R.id.title_history:
                titleHistory();
                return true;
            case R.id.app_intern_internet_toggle_in_note_activity:
                if (MyApp.internetDisabledInternally) {
                    if (isNetworkAvailable()) MyApp.updateFromServer = true;
                    db.enableNetwork();
                } else
                    db.disableNetwork();
                MyApp.internetDisabledInternally = !MyApp.internetDisabledInternally;
                recreate();
                return true;
            case R.id.action_add_reminder:
                addReminder();
                return true;
            case R.id.action_show_reminder:
                Intent intent = new Intent(EditNoteActivity.this, RemindersActivity.class);
                intent.putExtra("noteID", documentRef.getId());
                startActivity(intent);
                return true;
            case R.id.share_with_another_user:
                shareWithAnotherUser();
                return true;
            case R.id.keep_listener_on:
                keepListenerOn();
                return true;
            /*case R.id.save_for_load_to_cache:
                saveForLoadToCache();
                return true;*/
            case R.id.refreshTrafficLight:
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //    add a button instead of cancel to go back and choose a different reminder type for every reminder dialog.
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addReminder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setTitle("choose users:");

        final String[] shared = sharedUsernames.toArray(new String[0]);
        final String[] sharedAndMe = new String[shared.length + 1];
        System.arraycopy(shared, 0, sharedAndMe, 1, shared.length);
        boolean[] checkedItems = new boolean[sharedAndMe.length];
        sharedAndMe[0] = "me";
        checkedItems[0] = true;
        if (!MyApp.userSkippedLogin) {
            alert.setMultiChoiceItems(sharedAndMe, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);
        }

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView textView = new TextView(c);
        textView.setText("choose reminder type: ");
        layout.addView(textView);


        Spinner dropdownTimeType = new Spinner(c);
//      create a list of items for the spinner.


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeType);
        dropdownTimeType.setAdapter(adapter);

        layout.addView(dropdownTimeType);

        firstOpenSpinner = true;
        dropdownTimeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstOpenSpinner)
                    firstOpenSpinner = false;
                else {
                    if (MyApp.userSkippedLogin)
                        showReminderDialog(timeType[position], new ArrayList<>(), notifyUserSuggestions);
                    else
                        showReminderDialog(timeType[position], getUsernames(sharedAndMe, checkedItems), notifyUserSuggestions);
                    if (addReminderAlertDialog != null)
                        addReminderAlertDialog.cancel();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        alert.setView(layout);


        alert.setPositiveButton("continue", (dialog, whichButton) -> {
            if (MyApp.userSkippedLogin)
                showReminderDialog(dropdownTimeType.getSelectedItem().toString(), new ArrayList<>(), notifyUserSuggestions);
            else
                showReminderDialog(dropdownTimeType.getSelectedItem().toString(), getUsernames(sharedAndMe, checkedItems), notifyUserSuggestions);
        });

        alert.setNegativeButton("cancel", (dialog, whichButton) -> {
            //cancel
        });

        addReminderAlertDialog = alert.create();
        addReminderAlertDialog.show();
        Button btnPositive = addReminderAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = addReminderAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showReminderDialog(String type, ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {

        switch (type) {
            case "location":
                createAlertForLocationReminder(usernames, userSuggestions);
                break;
            case "user":
                createAlertForUserReminder(usernames, userSuggestions);
                break;
            case "whatsapp":
                addWhatsappReminder(usernames, userSuggestions);
                break;
            default:
                showDatePicker("", "", usernames, userSuggestions);
                break;
        }
    }

    private ArrayList<String> getUsernames(String[] sharedAndMe, boolean[] checkedItems) {
        ArrayList<String> usernames = new ArrayList<>();
        for (int i = 1; i < sharedAndMe.length; i++) {
            if (checkedItems[i])
                usernames.add(sharedAndMe[i]);
        }
        if (checkedItems[0])
            usernames.add(MyApp.myCloudUserData.getCloudUser().getUsername());
        return usernames;
    }


    private void shareWithAnotherUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("Share note");


        final String[] shared = sharedUsernames.toArray(new String[0]);
        boolean[] checkedItems = new boolean[shared.length];
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = true;
        }
        alert.setMultiChoiceItems(shared, checkedItems, (dialog, which, isChecked) -> {
            if (!isChecked) {
                CloudUserData cloudUserData = notifyUserSuggestions.get(shared[which]);
                sharedUsernames.remove(shared[which]);

                assert cloudUserData != null;
                documentRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        assert documentSnapshot != null;
                        Note note = documentSnapshot.toObject(Note.class);
                        assert note != null;
                        ArrayList<String> sharedIDs = note.getShared();
                        sharedIDs.remove(cloudUserData.getCloudUser().getUid());
                        documentRef.update("shared", sharedIDs);
                    }

                });
            } else {
                CloudUserData newUserCloudUserData = notifyUserSuggestions.get(shared[which]);
                assert newUserCloudUserData != null;
                documentRef.update(
                        "shared", FieldValue.arrayUnion(newUserCloudUserData.getCloudUser().getUid()))
                        .addOnSuccessListener(aVoid -> {
                            sharedUsernames.add(shared[which]);
                            notifyUserSuggestions.put(shared[which], newUserCloudUserData);
                            Toast.makeText(this, "note shared with " + shared[which], Toast.LENGTH_LONG).show();
                        });
            }
        });

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
//      add layout
        if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
            final TextView noInternetMessageTextView = new TextView(c);

            noInternetMessageTextView.setText("no internet - possibly suggesting\n" +
                    "only users from friends list");
            layout.addView(noInternetMessageTextView);
        }

        if (!MyApp.userUid.equals(offlineNoteData.getNote().getCreator())) {
            final TextView creatorTextView = new TextView(c);
            CloudUserData cloudUserData = MyApp.friends.get(offlineNoteData.getNote().getCreator());
            assert cloudUserData != null;
            CloudUser cloudUser = cloudUserData.getCloudUser();
            String username = cloudUser.getUsername();
            creatorTextView.setText("note creator: " + username);
            layout.addView(creatorTextView);
        }

        final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(c);
        autoCompleteTextView.setHint("type a username to share with a user");
        autoCompleteTextView.setCompletionHint("select a username");
        layout.addView(autoCompleteTextView);
        alert.setView(layout);

        CollectionReference userCollRef;
        HashMap<String, CloudUserData> userSuggestions;
        userSuggestions = new HashMap<>();
        ArrayList<String> usernameSuggestions;
        usernameSuggestions = new ArrayList<>();
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
                    if (!usernameSuggestions.contains(cloudUser.getUsername()))
                        usernameSuggestions.add(cloudUser.getUsername());
                }
                userSuggestions.remove(MyApp.myCloudUserData.getCloudUser().getUsername());
                usernameSuggestions.remove(MyApp.myCloudUserData.getCloudUser().getUsername());
            }
        });

        for (CloudUserData cloudUserData :
                MyApp.friends.values()) {
            if (!usernameSuggestions.contains(cloudUserData.getCloudUser().getUsername())) {
                userSuggestions.put(cloudUserData.getCloudUser().getUsername(), cloudUserData);
                if (!usernameSuggestions.contains(cloudUserData.getCloudUser().getUsername()))
                    usernameSuggestions.add(cloudUserData.getCloudUser().getUsername());
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernameSuggestions);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String newUserUsername = (String) parent.getItemAtPosition(position);
            CloudUserData newUserCloudUserData = userSuggestions.get(newUserUsername);
            assert newUserCloudUserData != null;
            documentRef.update(
                    "shared", FieldValue.arrayUnion(newUserCloudUserData.getCloudUser().getUid()))
                    .addOnSuccessListener(aVoid -> {
                        sharedUsernames.add(newUserUsername);
                        notifyUserSuggestions.put(newUserUsername, newUserCloudUserData);
                        MyApp.friends.put(newUserCloudUserData.getCloudUser().getUid(), newUserCloudUserData);
                        Toast.makeText(this, "note shared with " + newUserUsername, Toast.LENGTH_LONG).show();
                        alertDialogForSharingWithAnotherUser.cancel();
                    });
            if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
                Toast.makeText(this, "note will be shared with: " + newUserUsername + " when internet is available", Toast.LENGTH_LONG).show();
                alertDialogForSharingWithAnotherUser.cancel();
            }
        });

        alertDialogForSharingWithAnotherUser = alert.show();
    }


    public void showDatePicker(String number, String message, ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        DialogFragment newFragment = new MyDatePickerFragment(documentRef, this, number, message, usernames, userSuggestions);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createAlertForLocationReminder(ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the number label, as noted in the comments
        final EditText radiusEditText = new EditText(c);
        radiusEditText.setHint("write the radius here");
        layout.addView(radiusEditText); // Notice this is an add method

        // Add another TextView here for the message label
        final EditText locationEditText = new EditText(c);
        locationEditText.setHint("write coordinates here");
        layout.addView(locationEditText); // Another add method

        // Add another TextView here for the message label
        final Button locationHereButton = new Button(c);
        locationHereButton.setText("your location");
        locationHereButton.setOnClickListener(v -> {
            LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation("gps");
            locationEditText.setText(location.getLatitude() + ", " + location.getLongitude());

        });
        layout.addView(locationHereButton); // Another add method

        alert.setTitle("Location Reminder");
        alert.setMessage("");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            String radiusString = radiusEditText.getText().toString();
            String locationString = locationEditText.getText().toString();
            Log.i("AlertDialog", "TextEntry 1 Entered " + radiusString);
            Log.i("AlertDialog", "TextEntry 2 Entered " + locationString);

            Location location = new Location("");//provider name is unnecessary

            double radiusDouble = Double.parseDouble(radiusString);

            String[] split = locationString.split(",");
            double locationLatitude = Double.parseDouble(split[0]);
            double locationLongitude = Double.parseDouble(split[1]);
            location.setLatitude(locationLatitude);
            location.setLongitude(locationLongitude);

            LocationReminder locationReminder = new LocationReminder(radiusDouble);
            locationReminder.setGeoPoint(new GeoPoint(location.getLatitude(), location.getLongitude()));
            locationReminder.setNotifyUsers(getUserIDs(usernames, userSuggestions));
            db.enableNetwork();
            documentRef.collection("Reminders")
                    .add(locationReminder)
//                    this is already done in the listener.
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            MyApp.locationReminders.put(Objects.requireNonNull(task.getResult()).getId(),
                                    new LocationReminderData(task.getResult(), locationReminder));
//                            disabling network here because there is no reminder listener.
                            if (MyApp.userSkippedLogin)
                                db.disableNetwork();
                        }
                    });

        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);

    }

    private ArrayList<String> getUserIDs(ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        ArrayList<String> userIDs;
        userIDs = new ArrayList<>();
        for (String s :
                usernames) {
            CloudUserData cloudUserData = userSuggestions.get(s);
            if (cloudUserData != null)
                userIDs.add(cloudUserData.getCloudUser().getUid());
        }
        return userIDs;
    }

    private void createAlertForUserReminder(ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestionsNotifyUser) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the number label, as noted in the comments
        final EditText radiusEditText = new EditText(c);
        radiusEditText.setHint("write the radius here");
        layout.addView(radiusEditText); // Notice this is an add method

        if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
            final TextView noInternetMessageTextView = new TextView(c);
            noInternetMessageTextView.setText("no internet - possibly suggesting\n" +
                    "only users from friends list");
            layout.addView(noInternetMessageTextView);
        }

        final AutoCompleteTextView addUserAutoCompleteTextView = new AutoCompleteTextView(c);
        addUserAutoCompleteTextView.setHint("type a username to share with a user");
        addUserAutoCompleteTextView.setCompletionHint("select a username");
        layout.addView(addUserAutoCompleteTextView); // Another add method

        CollectionReference userCollRef;
        ArrayList<CloudUser> userSuggestions;
        ArrayList<String> usernameSuggestions;
        usernameSuggestions = new ArrayList<>();
        userSuggestions = new ArrayList<>();
        userCollRef = db.collection("users");
        userCollRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                assert querySnapshot != null;
                for (DocumentSnapshot documentSnapshot :
                        querySnapshot.getDocuments()) {
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    userSuggestions.add(cloudUser);
                    usernameSuggestions.add(cloudUser.getUsername());
                }
                userSuggestions.remove(MyApp.myCloudUserData.getCloudUser());
                usernameSuggestions.remove(MyApp.myCloudUserData.getCloudUser().getUsername());
            }
        });

        MyApp.myCloudUserData.getDocumentReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                assert cloudUser != null;
                for (String friend :
                        cloudUser.getFriends()) {
                    if (!usernameSuggestions.contains(friend))
                        usernameSuggestions.add(friend);
                }
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernameSuggestions);
        addUserAutoCompleteTextView.setAdapter(adapter);


        alert.setTitle("User Reminder");
        alert.setMessage("");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("continue", (dialog, whichButton) -> {
            String radiusString = radiusEditText.getText().toString();

            double radiusDouble = Double.parseDouble(radiusString);
            String username = addUserAutoCompleteTextView.getText().toString();
            int index = usernameSuggestions.indexOf(username);
            String userID = userSuggestions.get(index).getUid();
            UserReminder userReminder = new UserReminder(
                    userID
                    , radiusDouble);
            userReminder.setNotifyUsers(getUserIDs(usernames, userSuggestionsNotifyUser));
            db.enableNetwork();
            documentRef.collection("Reminders")
                    .add(userReminder)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // using the username variable in a listener scope might be problematic - everywhere.
                            // the solution will be to get the object from the document snapshot
                            MyApp.locationReminders.put(Objects.requireNonNull(task.getResult()).getId(),
                                    new UserReminderData(task.getResult(), userReminder));
                            Toast.makeText(this, "user reminder added with user: " + addUserAutoCompleteTextView.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                    });

        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);

    }

    private void addWhatsappReminder(ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

// Add a TextView here for the number label, as noted in the comments
        final EditText numberEditText = new EditText(c);
        numberEditText.setHint("write your number here");
        layout.addView(numberEditText); // Notice this is an add method

        // Add another TextView here for the message label
        final EditText messageEditText = new EditText(c);
        messageEditText.setHint("write your message here");
        layout.addView(messageEditText); // Another add method

        alert.setTitle("WhatsApp contact");
        alert.setMessage("press ok to be redirected to WhatsApp");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            showDatePicker(numberEditText.getText().toString(), messageEditText.getText().toString(), usernames, userSuggestions);
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @SuppressWarnings("unused")
    private void saveForLoadToCache() {
        documentRef.update("loadToCache", true);
    }

    private void keepListenerOn() {
        documentRef.update("keepOffline", true);
        keepOffline = true;
//        add a color or a symbol to show this note is kept offline.
//        make save_for_use_offline invisible and add another menu case for deactivating.
//        check in the other app's code how it is done.
    }

    private void titleHistory() {
        String id = getIntent().getStringExtra("noteID");
        Intent intent = new Intent(EditNoteActivity.this, TitleHistoryActivity.class);
        intent.putExtra("noteID", id);
        intent.putExtra("title", editTextTitle.getText().toString());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.activityEditNoteStopped();
        /*System.out.println("size of list" + MyApp.historyTitle.size());
        for (String s :
                MyApp.historyTitle) {
            System.out.println(s);
        }*/
        if (editTextTitle.getText().toString().trim().equals("")) {
            String description = editTextDescription.getText().toString();
            boolean shortEnough = description.length() < 45;
            int titleLength = shortEnough ? description.length() : 40;
            int checkIfOneBigAssWord = description.substring(0, titleLength).lastIndexOf(" ");
            if (checkIfOneBigAssWord <= 0)
                checkIfOneBigAssWord = 40;
            if (documentRef != null)
                if (shortEnough) {

                    documentRef.update("title", description);
                    documentRef.update("history", FieldValue.arrayUnion(description)).addOnSuccessListener(aVoid -> successfulUpload())
                            .addOnFailureListener(this::unsuccessfulUpload);
                } else {
                    documentRef.update("title", description.substring(0, checkIfOneBigAssWord));
                    documentRef.update("history", FieldValue.arrayUnion(description.substring(0, checkIfOneBigAssWord)))
                            .addOnSuccessListener(aVoid -> successfulUpload())
                            .addOnFailureListener(this::unsuccessfulUpload);
                }


        }
        if (registration != null)
            registration.remove();
        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Note note = Objects.requireNonNull(task.getResult()).toObject(Note.class);
                offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
                if (keepOffline || Objects.requireNonNull(note).isKeepOffline()) {
                    ListenerRegistration listenerRegistration = documentRef.addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            System.err.println("Listen failed: " + e);
                        }
                    });
                    offlineNoteData.setListenerRegistration(listenerRegistration);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.activityEditNoteResumed();
        if (!onCreateCalled && MyApp.currentTrafficLightState != lastTrafficLightState)
            recreate();

        if (!newNote) {

            setPreLoading();

            if (isNetworkAvailable() && !MyApp.internetDisabledInternally)
                setNoteWithConnection();

            if (!isNetworkAvailable() || MyApp.internetDisabledInternally)
                setNoteWithoutConnection();
        }

    }

    private void setNoteWithoutConnection() {

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (Objects.requireNonNull(documentSnapshot).exists()) {
                    runOnUiThread(() -> {
                        Note note = documentSnapshot.toObject(Note.class);
                        assert note != null;
                        offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
//                            setting the variable only the first time we move from online to offline - if it is -1 that means
//                            we were online before.                    offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
                        if (offlineNoteData.getLastKnownNoteHistoryListSize() == -1)
                            offlineNoteData.setLastKnownNoteHistoryListSize(note.getHistory().size());

                        // adding an older version of the title from the historyTitle list
                        if (MyApp.titleOldVersion != null) {
                            editTextTitle.setText(MyApp.titleOldVersion);
                            MyApp.titleOldVersion = null;
                        } else {
                            editTextTitle.removeTextChangedListener(textWatcherTitle);
                            editTextTitle.setTextColor(Color.BLACK);
                            editTextTitle.setText(note.getTitle());
                            editTextTitle.addTextChangedListener(textWatcherTitle);

                            editTextDescription.removeTextChangedListener(textWatcherDescription);
                            editTextDescription.setTextColor(Color.BLACK);
                            editTextDescription.setText(note.getDescription());
                            editTextDescription.addTextChangedListener(textWatcherDescription);

                        }
                    });
                }
            }
        });

    }

    private void setNoteWithConnection() {


        //adding an older version of the title from the historyTitle list
        if (MyApp.titleOldVersion != null) {
            editTextTitle.setText(MyApp.titleOldVersion);
            MyApp.titleOldVersion = null;
        }

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (Objects.requireNonNull(documentSnapshot).exists()) {
                    Note note = documentSnapshot.toObject(Note.class);
                    assert note != null;
                    offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.

//                        if this equals -1 that means we are coming from an online state so there is no need to check for changes
//                        and we can set the listener directly.
                    if (offlineNoteData.getLastKnownNoteHistoryListSize() != -1) {
//                            getting the unique device history list - this has no use online so we can just call it from the cache.
                        documentRef.collection(MyApp.getDeviceID(c) + MyApp.userUid).orderBy("created", Query.Direction.ASCENDING).get(Source.CACHE).addOnCompleteListener(taskOfflineHistory -> {
                            if (taskOfflineHistory.isSuccessful()) {
                                ArrayList<DocumentSnapshot> deviceHistoryList = (ArrayList<DocumentSnapshot>) Objects.requireNonNull(taskOfflineHistory.getResult()).getDocuments();
//                                    if the size of the main note history list has changed we want to check if the title is not coincidentally the same and then
//                                    if not we can call the dialog so the user can choose.
                                if (offlineNoteData.getLastKnownNoteHistoryListSize() != note.getHistory().size())
                                    if (!note.getHistory().get(note.getHistory().size() - 1).equals(Objects.requireNonNull(deviceHistoryList.get(deviceHistoryList.size() - 1).getData()).get("title")))
                                        chooseBetweenServerDataAndLocalData(note.getHistory().get(note.getHistory().size() - 1));
//                                    moving all the elements from the unique device history list to the main note history list and
//                                    then clearing unique device history list.
                                for (DocumentSnapshot documentSnapshotOfflineHistory :
                                        deviceHistoryList) {
                                    documentRef.update("history", FieldValue.arrayUnion(Objects.requireNonNull(documentSnapshotOfflineHistory.getData()).get("title")));
                                }
                                for (DocumentSnapshot documentSnapshotOfflineHistory :
                                        deviceHistoryList) {
                                    documentSnapshotOfflineHistory.getReference().delete();
                                }
//                                    setting back to -1 so this will not be called when the note was not edited offline.
                                offlineNoteData.setLastKnownNoteHistoryListSize(-1);

//                                    this is called here so it does not mess with the main note history list before
//                                    we are done with transferring everything and we also do not want the size to change before checking if it changed.
                                if (offlineNoteData.getListenerRegistration() != null)
                                    offlineNoteData.getListenerRegistration().remove();
                                createFirestoreListener();
                            }
                        });
                    } else {
                        if (offlineNoteData.getListenerRegistration() != null)
                            offlineNoteData.getListenerRegistration().remove();
                        createFirestoreListener();
                    }
                }
            }
        });


    }

    private void setPreLoading() {
        editTextTitle.removeTextChangedListener(textWatcherTitle);
        editTextTitle.setTextColor(Color.parseColor("#DF3B0D"));
        editTextTitle.setText(offlineNoteData.getNote().getTitle());
        editTextTitle.addTextChangedListener(textWatcherTitle);

        editTextDescription.removeTextChangedListener(textWatcherDescription);
        editTextDescription.setTextColor(Color.parseColor("#DF3B0D"));
        editTextDescription.setText(offlineNoteData.getNote().getDescription());
        editTextDescription.addTextChangedListener(textWatcherDescription);
    }

    private void createFirestoreListener() {
        registration = documentRef.addSnapshotListener((documentSnapshot, e) -> {
//            changeCursorPositionBack = true;
            if (e != null) System.err.println("Listen failed: " + e);

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Note note = documentSnapshot.toObject(Note.class);
                if (note != null) {
                    offlineNoteData.setNote(note); // saving to enable quicker loading or pre-loading.
                    // check if the data in the server has newer date than the one in the editable and force it to be shown
                    // this line should go off when the internet comes back on.
                    editTextTitle.setTextColor(Color.BLACK);
                    editTextDescription.setTextColor(Color.BLACK);
//                    hasPendingWrites is important because it keeps the server in it's place.
                    if (!documentSnapshot.getMetadata().hasPendingWrites()) {
//                        if last element in note history is not the title on the server then there has been
//                        a change and we ask if the user wants to see the change or keep on working on his
//                        version. problem:
                        if ((!note.getHistory().isEmpty() &&
                                !note.getHistory().get(note.getHistory().size() - 1).equals(note.getTitle()) &&
                                !note.getTitle().trim().equals(editTextTitle.getText().toString().trim())))
                            chooseBetweenServerDataAndLocalData(note.getTitle());
                        else {
                            if (!note.getTitle().equals(editTextTitle.getText().toString())) {
                                editTextTitle.removeTextChangedListener(textWatcherTitle);
                                editTextTitle.setText(note.getTitle());
                                editTextTitle.addTextChangedListener(textWatcherTitle);
                            }
                            if (!note.getDescription().equals(editTextDescription.toString())) {
                                editTextDescription.removeTextChangedListener(textWatcherDescription);
                                editTextDescription.setText(note.getDescription());
                                editTextDescription.addTextChangedListener(textWatcherDescription);
                            }
                        }
                    }
                }
            }


        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();


        documentRef.update(
                "title", title,
                "description", description
        );
        if (newNote)
            makeText(this, "Note added", LENGTH_SHORT).show();
        else
            makeText(this, "Note edited", LENGTH_SHORT).show();
        finish();
    }

    private void setMenuForUserSkippedLogin(Menu menu) {
        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle_in_note_activity);
        appInternInternetOffToggleMenuItem.setVisible(false);
        MenuItem shareWithAnotherUserMenuItem = menu.findItem(R.id.share_with_another_user);
        shareWithAnotherUserMenuItem.setVisible(false);
        MenuItem refreshTrafficLightMenuItem = menu.findItem(R.id.refreshTrafficLight);
        refreshTrafficLightMenuItem.setVisible(false);
        MenuItem keepSyncedMenuItem = menu.findItem(R.id.keep_listener_on);
        keepSyncedMenuItem.setVisible(false);
    }

    @Override
    public Resources.Theme getTheme() {
        super.setLastTrafficLightState(lastTrafficLightState);
        Resources.Theme theme = super.getTrafficLightTheme();
        lastTrafficLightState = super.getLastTrafficLightState();
        return theme;
    }

}
