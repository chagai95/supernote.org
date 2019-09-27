package com.example.firebaseui_firestoreexample.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.adapters.TitleHistoryAdapter;
import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.example.firebaseui_firestoreexample.utils.RecyclerItemClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class TitleHistoryActivity extends MyActivity {

    private ArrayList<String> noteHistoryList;
    private DocumentReference documentRef;

    Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_history);

        noteHistoryList = new ArrayList<>();

        OfflineNoteData offlineNoteData = MyApp.allNotesOfflineNoteData.get(getIntent().getStringExtra("noteID"));
        assert offlineNoteData != null;
        documentRef = offlineNoteData.getDocumentReference();

        documentRef.get().addOnSuccessListener(documentSnapshot -> {
            Note note = documentSnapshot.toObject(Note.class);
            if (note != null) {
                noteHistoryList = note.getHistory();
                setUpRecyclerView();
            }
        });


    }

    private void setUpRecyclerView() {

        TitleHistoryAdapter adapter = new TitleHistoryAdapter(this, noteHistoryList);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_note_history);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        }).attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if (isNetworkAvailable()) {
                            documentRef.get(Source.SERVER).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    Note noteServer = Objects.requireNonNull(documentSnapshot).toObject(Note.class);
                                    if (Objects.requireNonNull(documentSnapshot).exists()) {
                                        MyApp.titleOldVersion = noteHistoryList.get(position);
                                        if (!getIntent().getStringExtra("title").equals(Objects.requireNonNull(Objects.requireNonNull(noteServer).getTitle()))) {
                                            chooseBetweenServerDataAndLocalData((String) Objects.requireNonNull(documentSnapshot.getData()).get("title"));
                                        } else finish();
                                    }
                                } else
                                    makeText(c, "didn't get data from server trying again!", LENGTH_SHORT).show();
                            });

                        } else{
                            MyApp.titleOldVersion = noteHistoryList.get(position);
                            finish();
                        }
                    }
                })
        );


    }

    private void chooseBetweenServerDataAndLocalData(String serverData) {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("chooseBetweenServerDataAndLocalData");
        alert.setMessage("Server data: " + serverData + "\n" + "Local data: " + MyApp.titleOldVersion);
// Create TextView
        final TextView input = new TextView(c);
        alert.setView(input);

        alert.setPositiveButton("changed Server data", (dialog, whichButton) -> {
            MyApp.titleOldVersion = null;
            finish();
        });

        alert.setNegativeButton("older version", (dialog, whichButton) -> {
                    documentRef.update("title", MyApp.titleOldVersion);
                    finish();
                }
        );
        alert.show();
    }

}
