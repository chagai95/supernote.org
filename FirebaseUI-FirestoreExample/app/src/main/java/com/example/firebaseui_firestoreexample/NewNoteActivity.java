package com.example.firebaseui_firestoreexample;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;

public class NewNoteActivity extends MyActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private NumberPicker numberPickerPriority;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        onCreateCalled = true;


        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close); // added Objects.requireNonNull to avoid warning
        setTitle("Add note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        numberPickerPriority = findViewById(R.id.number_picker_priority);

        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.save_note:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        int priority = numberPickerPriority.getValue();

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(this, "please insert a title AND description", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference notesCollRef = FirebaseFirestore.getInstance()
                .collection("notes");
        notesCollRef.add(new Note(title, description, priority,new Timestamp(new Date()))).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentReference documentReference = task.getResult();
                assert documentReference != null;
                MyApp.allNotesOfflineNoteData.put(documentReference.getId(), new OfflineNoteData(documentReference));
            }
        });
        Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        finish();
    }

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
        MyApp.activityNewNoteResumed();
        if (!onCreateCalled && MyApp.lastTrafficLightState != lastTrafficLightState)
            recreate();
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
