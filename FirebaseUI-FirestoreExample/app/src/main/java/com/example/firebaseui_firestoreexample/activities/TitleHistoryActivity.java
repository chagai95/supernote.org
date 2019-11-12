package com.example.firebaseui_firestoreexample.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.adapters.TitleHistoryAdapter;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.firestore_data.NoteData;
import com.example.firebaseui_firestoreexample.utils.RecyclerItemClickListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class TitleHistoryActivity extends MyActivity {

    private ArrayList<String> noteHistoryList;
    private DocumentReference documentRef;

    Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_history);
        setTitle(" Title Note History");
        noteHistoryList = new ArrayList<>();

        NoteData noteData = MyApp.allNotes.get(getIntent().getStringExtra("noteID"));
        assert noteData != null;
        documentRef = noteData.getDocumentReference();

        documentRef.get().addOnSuccessListener(documentSnapshot -> {
            Note note = documentSnapshot.toObject(Note.class);
            if (note != null) {
                noteHistoryList = note.getTitleHistory();
                setUpRecyclerView();
            }
        });


    }

    private void setUpRecyclerView() {

        TitleHistoryAdapter adapter = new TitleHistoryAdapter(this, noteHistoryList);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_note_title_history);
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
                    }
                })
        );


    }
}
