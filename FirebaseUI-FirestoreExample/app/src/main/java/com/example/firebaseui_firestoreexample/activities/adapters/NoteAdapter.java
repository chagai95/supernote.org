package com.example.firebaseui_firestoreexample.activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;


public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private OnItemClickListener listener;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int i, @NonNull Note model) {
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewDescription.setText(model.getDescription());
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);

        return new NoteHolder(v);
    }

    public void deleteItem(int position) {
        DocumentReference documentReference = getSnapshots().getSnapshot(position).getReference();
        MyApp.allNotes.remove(documentReference.getId());
        documentReference.collection("Reminders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot :
                        Objects.requireNonNull(task.getResult()).getDocuments()) {
                    documentSnapshot.getReference().delete();
                }
            }

        });
        documentReference.delete();
    }

    public DocumentReference trashItem(int position) {
        DocumentReference documentReference = getSnapshots().getSnapshot(position).getReference();
        documentReference.collection("Reminders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot :
                        Objects.requireNonNull(task.getResult()).getDocuments()) {
                    documentSnapshot.getReference().update("trash", true);
                }
            }
        });

        documentReference.update("trash", true);
        return documentReference;
    }

    public void untrashItem(int position) {
        DocumentReference documentReference = getSnapshots().getSnapshot(position).getReference();
        documentReference.collection("Reminders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot :
                        Objects.requireNonNull(task.getResult()).getDocuments()) {
                    documentSnapshot.getReference().update("trash", false);
                }
            }
        });

        documentReference.update("trash", false);
    }


    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;

        NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
