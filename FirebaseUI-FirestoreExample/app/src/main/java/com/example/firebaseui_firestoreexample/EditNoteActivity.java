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
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class EditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private NumberPicker numberPickerPriority;

    private DocumentReference documentRef;
    public static ListenerRegistration registration;

    TextWatcher textWatcherTitle;
    TextWatcher textWatcherDescription;


    static boolean isOnline;

    @SuppressWarnings("unused")
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);


        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close); // added Objects.requireNonNull to avoid warning
        setTitle("Edit note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        numberPickerPriority = findViewById(R.id.number_picker_priority);


                documentRef = FirebaseFirestore.getInstance()
                .collection("Notebook")
                .document(Objects.requireNonNull(getIntent().getStringExtra("documentID")));  // added Objects.requireNonNull to avoid warning


        textWatcherTitle = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                documentRef.update("history", FieldValue.arrayUnion(charSequence.toString())).addOnSuccessListener(aVoid -> successfulUpload())
                        .addOnFailureListener(e -> {
                            unsuccessfulUpload();
                        });
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /*if (MyApp.historyTitle.isEmpty())
                    MyApp.historyTitle.add(editable.toString());
                if (!MyApp.historyTitle.getLast().equals(editable.toString()))
                    MyApp.historyTitle.add(editable.toString());*/
                isOnline = isNetworkAvailable();
                if (isOnline && !MyApp.updateFromServer) {
                    documentRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            /*if (!MyApp.historyTitle.getLast().equals(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot).getData()).get("title")))
                                MyApp.historyTitle.add((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));*/
                            if(!Objects.requireNonNull(documentSnapshot).getMetadata().isFromCache())
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

    private void unsuccessfulUpload() {
        makeText(this, "not uploaded - this may be because you are offline", LENGTH_SHORT).show();
    }

    private void successfulUpload() {
        makeText(this, "uploaded successfully", LENGTH_SHORT).show();
    }


    private void chooseBetweenServerDataAndLocalData(String serverData) {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("chooseBetweenServerDataAndLocalData");
        alert.setMessage("Server data: " + serverData + "\n" + "Local data: " + editTextTitle.getText().toString());
// Create TextView
        final TextView input = new TextView(c);
        alert.setView(input);

        alert.setPositiveButton("Server data", (dialog, whichButton) -> editTextTitle.setText(serverData));

        alert.setNegativeButton("Local data", (dialog, whichButton) -> documentRef.update("title",editTextTitle.getText().toString()));
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void titleHistory() {
        String id = getIntent().getStringExtra("documentID");
        Intent intent = new Intent(EditNoteActivity.this, TitleHistoryActivity.class);
        intent.putExtra("documentID",id);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*System.out.println("size of list" + MyApp.historyTitle.size());
        for (String s :
                MyApp.historyTitle) {
            System.out.println(s);
        }*/
        if (registration != null)
            registration.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (MyApp.updateFromServer) {
            MyApp.updateFromServer = false;
            documentRef = FirebaseFirestore.getInstance()
                    .collection("Notebook")
                    .document(Objects.requireNonNull(getIntent().getStringExtra("documentID")));  // added Objects.requireNonNull to avoid warning

            documentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (Objects.requireNonNull(documentSnapshot).exists()) {
                        makeText(this, editTextTitle.getText().toString(), LENGTH_SHORT).show();

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

        if(MyApp.titleOldVersion!=null){
            /*if (!MyApp.historyTitle.isEmpty() && !MyApp.historyTitle.getLast().equals(editTextTitle.getText().toString()))
                MyApp.historyTitle.add(editTextTitle.getText().toString());*/
            editTextTitle.setText(MyApp.titleOldVersion);
            MyApp.titleOldVersion = null;
        }
        isOnline = isNetworkAvailable();
        if (isOnline) {
            documentRef = FirebaseFirestore.getInstance()
                    .collection("Notebook")
                    .document(Objects.requireNonNull(getIntent().getStringExtra("documentID")));  // added Objects.requireNonNull to avoid warning
            registration = documentRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    makeText(EditNoteActivity.this, "Listen failed: " + e, LENGTH_SHORT).show();
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Note note = documentSnapshot.toObject(Note.class);
                    if (note != null) {
                        // check if the data in the server has newer date than the one in the editable and force it to be shown
                        // this line should go off when the internet comes back on.
                        /*if (!MyApp.historyTitle.isEmpty() && !MyApp.historyTitle.getLast().equals(note.getTitle()))
                            MyApp.historyTitle.add(note.getTitle());*/
                        if (!documentSnapshot.getMetadata().hasPendingWrites()) {
                            if (!note.getTitle().equals(editTextTitle.toString())) {
                                editTextTitle.removeTextChangedListener(textWatcherTitle);
                                editTextTitle.setText(note.getTitle());
                                if(documentSnapshot.getMetadata().isFromCache()){
                                    documentRef.get().addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            editTextTitle.setText((String) Objects.requireNonNull(task.getResult()).get("title"));
                                        }
                                    });
                                }
                                editTextTitle.addTextChangedListener(textWatcherTitle);
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
        isOnline = isNetworkAvailable();
//        skip this for now because it does not work. - tried to check if there can be a connection with google established.
//        new Online().run();
        if (isOnline)// && networkWorking)
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
