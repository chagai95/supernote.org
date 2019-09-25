package com.example.firebaseui_firestoreexample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
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
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class EditNoteActivity extends MyActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private NumberPicker numberPickerPriority;

    String noteID;
    private DocumentReference documentRef;
    public static ListenerRegistration registration;

    TextWatcher textWatcherTitle;
    TextWatcher textWatcherDescription;

    OfflineNoteData offlineNoteData;

    Context c = this;

    boolean onCreateCalled;
    boolean onCreateCalledForTextWatcher;
    private boolean keepOffline;
    private TrafficLight lastTrafficLightState;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    AlertDialog alertDialogForSharingWithAnotherUser;

//    private int cursor;
//    private boolean changeCursorPositionBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        onCreateCalled = true;

        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close); // added Objects.requireNonNull to avoid warning
        setTitle("Edit note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        numberPickerPriority = findViewById(R.id.number_picker_priority);

        noteID = Objects.requireNonNull(getIntent().getStringExtra("noteID"));
        offlineNoteData = Objects.requireNonNull(MyApp.allNotesOfflineNoteData.get(noteID));
        documentRef = offlineNoteData.getDocumentReference();

        textWatchers();


        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

    }

    private void textWatchers() {
        onCreateCalledForTextWatcher = true;


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
                if (!onCreateCalledForTextWatcher) {
                    if (isNetworkAvailable() && !MyApp.internetDisabledInternally) {
//                        when online uploading directly to the main note history list.
                        documentRef.update("history", FieldValue.arrayUnion(editable.toString())).addOnSuccessListener(aVoid -> successfulUpload())
                                .addOnFailureListener(e -> unsuccessfulUpload(e));
//                        because the listener is on we have to check the text we want to upload is not already online otherwise we end up in an endless loop.
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
                                documentRef.collection(MyApp.getDeviceID(c) + MyApp.uid).add(note.newNoteVersion());
                            }
                        });

                    }
                } else onCreateCalledForTextWatcher = false;
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
            recreate();
        });

        alert.setNegativeButton("Local data", (dialog, whichButton) -> {
            documentRef.update("title", editTextTitle.getText().toString());
            recreate();
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, menu);
        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle_in_note_activity);
        if (MyApp.internetDisabledInternally)
            appInternInternetOffToggleMenuItem.setTitle("activate internet in App");
        else
            appInternInternetOffToggleMenuItem.setTitle("deactivate internet in App");
        return super.onCreateOptionsMenu(menu);
    }

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
                showDatePicker("", "");
                return true;
            case R.id.share_with_another_user:
                shareWithAnotherUser();
                return true;
            case R.id.location_reminder:
                createAlertForLocationReminder();
                return true;
            case R.id.action_add_whatsappreminder:
                addWhatsappReminder();
                return true;
            case R.id.save_for_use_offline:
                saveForUseOffline();
                return true;
            case R.id.save_for_load_to_cache:
                saveForLoadToCache();
                return true;
            case R.id.refreshTrafficLight:
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareWithAnotherUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("Share note");
        if (!isNetworkAvailable() || MyApp.internetDisabledInternally)
            alert.setMessage("no internet - possibly showing only users from friends list");

        final AutoCompleteTextView addUser = new AutoCompleteTextView(c);
        addUser.setHint("type a username to share with a user");
        addUser.setCompletionHint("select a username");
        alert.setView(addUser);

        CollectionReference userCollRef;
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
                    usernameSuggestions.add(cloudUser.getUsername());
                }
                usernameSuggestions.remove(MyApp.username);
            }
        });

        MyApp.userDocumentRef.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               DocumentSnapshot documentSnapshot = task.getResult();
               assert documentSnapshot != null;
               CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
               assert cloudUser != null;
               for (String friend :
                       cloudUser.getFriends()) {
                   if(!usernameSuggestions.contains(friend))
                       usernameSuggestions.add(friend);
               }
           }
        });










        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernameSuggestions);
        addUser.setAdapter(adapter);
        addUser.setOnItemClickListener((parent, view, position, id) -> {
            String newUser = (String) parent.getItemAtPosition(position);
            documentRef.update(
                    "shared", FieldValue.arrayUnion(newUser))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "note shared with " + newUser, Toast.LENGTH_LONG).show();
                        addUser.setText("");
                        alertDialogForSharingWithAnotherUser.cancel();
                    });
        });

        alertDialogForSharingWithAnotherUser = alert.show();
    }


    public void showDatePicker(String number, String message) {
        DialogFragment newFragment = new MyDatePickerFragment(documentRef, this, number, message);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    private void createAlertForLocationReminder() {
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

            documentRef.collection("Reminders")
                    .add(new LocationReminder(new GeoPoint(location.getLatitude(), location.getLongitude()), radiusDouble))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            MyApp.locationReminders.put(Objects.requireNonNull(task.getResult()).getId(), task.getResult());
//                    MyApp.addReminderToLocationManager(task.getResult().get);
                        }
                    });

        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        alert.show();

    }

    private void addWhatsappReminder() {
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
            Log.i("AlertDialog", "TextEntry 1 Entered " + numberEditText.getText().toString());
            Log.i("AlertDialog", "TextEntry 2 Entered " + messageEditText.getText().toString());
            showDatePicker(numberEditText.getText().toString(), messageEditText.getText().toString());
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        alert.show();
    }

    private void saveForLoadToCache() {
        documentRef.update("loadToCache", true);
    }

    private void saveForUseOffline() {
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
        if (!onCreateCalled && MyApp.lastTrafficLightState != lastTrafficLightState)
            recreate();

        editTextTitle.removeTextChangedListener(textWatcherTitle);
        editTextTitle.setTextColor(Color.parseColor("#DF3B0D"));
        editTextTitle.setText(offlineNoteData.getNote().getTitle());
        editTextTitle.addTextChangedListener(textWatcherTitle);

        editTextDescription.removeTextChangedListener(textWatcherDescription);
        editTextDescription.setTextColor(Color.parseColor("#DF3B0D"));
        editTextDescription.setText(offlineNoteData.getNote().getDescription());
        editTextDescription.addTextChangedListener(textWatcherDescription);


        if (isNetworkAvailable() && !MyApp.internetDisabledInternally) {

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
                            documentRef.collection(MyApp.getDeviceID(c) + MyApp.uid).orderBy("created", Query.Direction.ASCENDING).get(Source.CACHE).addOnCompleteListener(taskOfflineHistory -> {
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

        if (!isNetworkAvailable() || MyApp.internetDisabledInternally) {
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
                    if (!documentSnapshot.getMetadata().hasPendingWrites()) {
                        editTextTitle.setTextColor(Color.BLACK);
                        if (!note.getTitle().equals(editTextTitle.getText().toString())) {
                            editTextTitle.removeTextChangedListener(textWatcherTitle);
                            editTextTitle.setText(note.getTitle());
                            editTextTitle.addTextChangedListener(textWatcherTitle);
                        }
                        editTextDescription.setTextColor(Color.BLACK);
                        if (!note.getDescription().equals(editTextDescription.toString())) {
                            editTextDescription.removeTextChangedListener(textWatcherDescription);
                            editTextDescription.setText(note.getDescription());
                            editTextDescription.addTextChangedListener(textWatcherDescription);
                        }
                        numberPickerPriority.setValue(note.getPriority());
                    }
                }
            }


        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        int priority = numberPickerPriority.getValue();

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            makeText(this, "please insert a title AND description", LENGTH_SHORT).show();
            return;
        }


        documentRef.update(
                "title", title,
                "description", description,
                "priority", priority
        );
        makeText(this, "Note edited", LENGTH_SHORT).show();
        finish();
    }

    @Override
    public Resources.Theme getTheme() {
        super.setLastTrafficLightState(lastTrafficLightState);
        Resources.Theme theme = super.getTrafficLightTheme();
        lastTrafficLightState = super.getLastTrafficLightState();
        return theme;
    }

    boolean isNetworkAvailable() {
        return super.isNetworkAvailable();
    }
}
