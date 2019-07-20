package com.example.firebaseui_firestoreexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.LinkedList;

import javax.annotation.Nullable;

public class EditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private NumberPicker numberPickerPriority;

    DocumentReference documentRef;
    ListenerRegistration registration;

    LinkedList<String> historyTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);


        historyTitle = new LinkedList<String>();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Edit note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        numberPickerPriority = findViewById(R.id.number_picker_priority);

        documentRef = FirebaseFirestore.getInstance()
                .collection("Notebook")
                .document(getIntent().getStringExtra("documentID"));

        registration = documentRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(EditNoteActivity.this, "Listen failed: " + e, Toast.LENGTH_SHORT).show();
                System.err.println("Listen failed: " + e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Note note = documentSnapshot.toObject(Note.class);
                if (note != null) {
                    if (!documentSnapshot.getMetadata().hasPendingWrites()) {
                        editTextTitle.setText(note.getTitle());
                        editTextDescription.setText(note.getDescription());
                        numberPickerPriority.setValue(note.getPriority());
                    }
                }
            }


        });

        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                    documentRef.update("title", editable.toString());
            }
        });
        editTextDescription.addTextChangedListener(new TextWatcher() {
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
        });

        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (String s :
                historyTitle) {
            System.out.println(s);
        }

        registration.remove();
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        int priority = numberPickerPriority.getValue();

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(this, "please insert a title AND description", Toast.LENGTH_SHORT).show();
            return;
        }


        documentRef.update(
                "title", title,
                "description", description,
                "priority", priority
        );
        Toast.makeText(this, "Note edited", Toast.LENGTH_SHORT).show();
        finish();


    }
}
