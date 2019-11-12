package com.example.firebaseui_firestoreexample.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.firestore_data.NoteData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Objects;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {
    private SearchAdapter.OnItemClickListener listener;

    //this activity we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the Strings in a list
    private List<NoteData> noteList;

    //getting the activity and String list with constructor
    public SearchAdapter(Context cOtherActivity, List<NoteData> noteList) {
        this.cOtherActivity = cOtherActivity;
        this.noteList = noteList;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchHolder holder, int i) {
        holder.textViewTitle.setText(noteList.get(i).getNote().getTitle());
        holder.textViewDescription.setText(noteList.get(i).getNote().getDescription());
    }

    @NonNull
    @Override
    public SearchAdapter.SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);

        return new SearchAdapter.SearchHolder(v);
    }

    public void deleteItem(int position) {
        DocumentReference documentReference = noteList.get(position).getDocumentReference();
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

    public void trashItem(int position) {
        DocumentReference documentReference = noteList.get(position).getDocumentReference();
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
        DocumentReference documentReference = noteList.get(position).getDocumentReference();
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


    class SearchHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;

        SearchHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(noteList.get(position).getDocumentReference(), position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentReference documentReference, int position);
    }

    public void setOnItemClickListener(SearchAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
