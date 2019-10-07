package com.example.firebaseui_firestoreexample.activities.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private OnItemClickListener listener;
    private boolean startAppAndCloseMainActivity;
    private Activity activity;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Activity activity, boolean startAppAndCloseMainActivity) {
        super(options);
        this.activity = activity;
        this.startAppAndCloseMainActivity = startAppAndCloseMainActivity;
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
//      since I don't know how to get the position of the newly added note we (fow now) just put all notes
//      every time this method is called in a map which basically means no duplicates.
        if (startAppAndCloseMainActivity) {
            FirebaseFirestore.getInstance().collection("utils").document("NoteAdapter").update(
                    "NoteAdapter", FieldValue.arrayUnion(this.toString()));
            activity.finish();
        }
        return new NoteHolder(v);
    }

    public void deleteItem(int position) {
        DocumentReference documentReference = getSnapshots().getSnapshot(position).getReference();
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

    public void trashItem(int position) {
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
