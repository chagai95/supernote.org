package com.example.firebaseui_firestoreexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.RecyclerItemClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class TitleHistoryActivity extends AppCompatActivity {

    private ArrayList<String> noteHistoryList;
    private DocumentReference documentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_history);

        noteHistoryList = new ArrayList<>();

        documentRef = FirebaseFirestore.getInstance()
                .collection("Notebook")
                .document(Objects.requireNonNull(getIntent().getStringExtra("noteID")));  // added Objects.requireNonNull to avoid warning

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
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        documentRef.update("title", noteHistoryList.get(position));
                        MyApp.titleOldVersion = noteHistoryList.get(position);
                        finish();
                    }
                })
        );




    }
}
