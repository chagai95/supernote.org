package com.example.firebaseui_firestoreexample;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.OfflineNoteData;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;


public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private OnItemClickListener listener;
    private boolean startAppAndCloseMainActivity;
    Activity activity;

    NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Activity activity, boolean startAppAndCloseMainActivity) {
        super(options);
        this.activity = activity;
        this.startAppAndCloseMainActivity = startAppAndCloseMainActivity;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int i, @NonNull Note model) {
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewPriority.setText(String.valueOf(model.getPriority()));
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
//      since I don't know how to get the position of the newly added note we (fow now) just put all notes
//      every time this method is called in a map which basically means no duplicates.
        for (int i = 0; i < getItemCount(); i++) {
            DocumentSnapshot docSnapshot = getSnapshots().getSnapshot(i);
            Note note = docSnapshot.toObject(Note.class);
            DocumentReference documentReference = docSnapshot.getReference();
            MyApp.allNotesOfflineNoteData.put(documentReference.getId(), new OfflineNoteData(documentReference));
            OfflineNoteData offlineNoteData = MyApp.allNotesOfflineNoteData.get(documentReference.getId());
            if(note.isKeepOffline()){
                ListenerRegistration listenerRegistration = documentReference.addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                    }
                });
                offlineNoteData.setListenerRegistration(listenerRegistration);
            }
            if(note.isLoadToCache())
                MyApp.loadToCacheMap.put(documentReference.getId(),documentReference);
        }
        if(startAppAndCloseMainActivity)
            activity.finish();
        return new NoteHolder(v);
    }

    void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPriority;

        NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);

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

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
