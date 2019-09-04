package com.example.firebaseui_firestoreexample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class EditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private NumberPicker numberPickerPriority;

    String documentID;
    private DocumentReference documentRef;
    public static ListenerRegistration registration;

    TextWatcher textWatcherTitle;
    TextWatcher textWatcherDescription;

    OfflineNoteData offlineNoteData;

    Context c = this;

    boolean lastOnlineState;
    boolean onCreateCalled;
    private boolean keepOffline;
//    private int cursor;
//    private boolean changeCursorPositionBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);


        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close); // added Objects.requireNonNull to avoid warning
        setTitle("Edit note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        documentID = Objects.requireNonNull(getIntent().getStringExtra("documentID"));
        offlineNoteData = Objects.requireNonNull(MyApp.allNotesOfflineNoteData.get(documentID));
        documentRef = offlineNoteData.getDocumentReference();
        onCreateCalled = true;


        /*cursor = 0;
        changeCursorPositionBack = false;*/
        textWatcherTitle = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                cursor = editTextTitle.getSelectionStart();
//              perhaps send the cursor position to the the server and change the cursor everywhere.
//                registration.remove();
                documentRef.update("history", FieldValue.arrayUnion(charSequence.toString())).addOnSuccessListener(aVoid -> successfulUpload())
                        .addOnFailureListener(e -> unsuccessfulUpload(e));
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

                /*if (MyApp.historyTitle.isEmpty())
                    MyApp.historyTitle.add(editable.toString());
                if (!MyApp.historyTitle.getLast().equals(editable.toString()))
                    MyApp.historyTitle.add(editable.toString());*/
                if (isNetworkAvailable() && !MyApp.updateFromServer) {
                    documentRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            /*if (!MyApp.historyTitle.getLast().equals(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot).getData()).get("title")))
                                MyApp.historyTitle.add((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));*/
                            if (!Objects.requireNonNull(documentSnapshot).getMetadata().isFromCache())
                                if (!Objects.requireNonNull(documentSnapshot.toObject(Note.class)).getTitle().equals(editable.toString()))
                                    documentRef.update("title", editable.toString());
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
                documentRef.update(
                        "description", editable.toString()
                );
            }
        };
        editTextDescription.addTextChangedListener(textWatcherDescription);

        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

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

        alert.setPositiveButton("Server data", (dialog, whichButton) -> editTextTitle.setText(serverData));

        alert.setNegativeButton("Local data", (dialog, whichButton) -> documentRef.update("title", editTextTitle.getText().toString()));
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_note_menu, menu);
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
            case R.id.action_add_reminder:
                showDatePicker("", "");
                return true;
            case R.id.location_reminder:
//                createLocationReminder();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDatePicker(String number, String message) {
        DialogFragment newFragment = new MyDatePickerFragment(documentRef, this, number, message);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

//    private void createLocationReminder() {
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        LocationListener listener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                for (Note noteMain :
//                        ServerCommunicator.notes) {
//                    for (Reminder reminder :
//                            noteMain.getReminders()) {
//                        if (reminder instanceof LocationReminder) {
//                            float distanceInMeters = ((LocationReminder) reminder).getLocation().distanceTo(location);
//                            if (((LocationReminder) reminder).getRadius() > (double) distanceInMeters) {
//                                System.out.println("\n " + location.getLongitude() + " " + location.getLatitude());
//                                Toast.makeText(NoteActivity.this, "\n " + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
//                                createNotificationForLocationReminder();
//                            }
//
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String s) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String s) {
//
//                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(i);
//            }
//        };
//        // first check for permissions
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
//                        , 10);
//            }
//            return;
//        }
//
//        if (locationManager != null)
//            locationManager.requestLocationUpdates("gps", 500, 1, listener);
//        createAlertForLocationReminder();
//
//    }

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
        MyApp.loadToCacheMap.put(documentID, documentRef);
    }

    private void saveForUseOffline() {
        documentRef.update("keepOffline", true);
        keepOffline = true;
//        add a color or a symbol to show this note is kept offline.
//        make save_for_use_offline invisible and add another menu case for deactivating.
//        check in the other app's code how it is done.
    }

    private void titleHistory() {
        String id = getIntent().getStringExtra("documentID");
        Intent intent = new Intent(EditNoteActivity.this, TitleHistoryActivity.class);
        intent.putExtra("documentID", id);
        startActivity(intent);
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
        lastOnlineState = isNetworkAvailable();
        onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.activityEditNoteResumed();
        if (!onCreateCalled && lastOnlineState != isNetworkAvailable())
            recreate();
        if (MyApp.updateFromServer) {
            MyApp.updateFromServer = false;
            documentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (Objects.requireNonNull(documentSnapshot).exists()) {
                        if (!editTextTitle.getText().toString().equals(Objects.requireNonNull(documentSnapshot.getData()).get("title"))) {
                            chooseBetweenServerDataAndLocalData((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));
                        }
//                        if (!MyApp.historyTitle.getLast().equals(Objects.requireNonNull(documentSnapshot.getData()).get("title")))
//                            MyApp.historyTitle.add((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));
                    }
                }
            });
            /*documentRef.get().addOnSuccessListener(documentSnapshot -> {
                Note note = documentSnapshot.toObject(Note.class);
                if (note != null) {
                    if (!editTextTitle.getText().toString().equals(note.getTitle())) {
                        chooseBetweenServerDataAndLocalData(note.getTitle());
                    }
                    if (!MyApp.historyTitle.getLast().equals(note.getTitle()))
                        MyApp.historyTitle.add(note.getTitle());
                }
            });*/
        }

        if (MyApp.titleOldVersion != null) {
            /*if (!MyApp.historyTitle.isEmpty() && !MyApp.historyTitle.getLast().equals(editTextTitle.getText().toString()))
                MyApp.historyTitle.add(editTextTitle.getText().toString());*/
            editTextTitle.setText(MyApp.titleOldVersion);
            MyApp.titleOldVersion = null;
        }
        if (isNetworkAvailable()) {
            if (offlineNoteData.getListenerRegistration() != null)
                offlineNoteData.getListenerRegistration().remove();
            createFirestoreListener();
        } else
            documentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (Objects.requireNonNull(documentSnapshot).exists()) {
                        editTextTitle.setText((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));
                        editTextDescription.setText((String) Objects.requireNonNull(documentSnapshot.getData()).get("description"));
                    }
                }
            });
    }

    private void createFirestoreListener() {
        registration = documentRef.addSnapshotListener((documentSnapshot, e) -> {
//            changeCursorPositionBack = true;
            if (e != null) System.err.println("Listen failed: " + e);

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Note note = documentSnapshot.toObject(Note.class);
                if (note != null) {
                    // check if the data in the server has newer date than the one in the editable and force it to be shown
                    // this line should go off when the internet comes back on.
                        /*if (!MyApp.historyTitle.isEmpty() && !MyApp.historyTitle.getLast().equals(note.getTitle()))
                            MyApp.historyTitle.add(note.getTitle());*/
                    if (!documentSnapshot.getMetadata().hasPendingWrites()) {
                        if (!note.getTitle().equals(editTextTitle.getText().toString())) {
//                            editTextTitle.removeTextChangedListener(textWatcherTitle);
                            editTextTitle.setText(note.getTitle());
                                /*if (documentSnapshot.getMetadata().isFromCache()) {
                                    documentRef.get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            editTextTitle.setText((String) Objects.requireNonNull(task.getResult()).get("title"));
                                        }
                                    });
                                }*/
//                            editTextTitle.addTextChangedListener(textWatcherTitle);
                        }
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
        Resources.Theme theme = super.getTheme();
//        skip this for now because it does not work. - tried to check if there can be a connection with google established.
//        new Online().run();
        if (isNetworkAvailable())// && networkWorking)
            theme.applyStyle(R.style.Online, true);
        else
            theme.applyStyle(R.style.Offline, true);
        return theme;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null; //added to avoid warning
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
