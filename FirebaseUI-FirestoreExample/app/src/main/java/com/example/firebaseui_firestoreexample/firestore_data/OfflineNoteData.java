package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.Note;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class OfflineNoteData {

    private DocumentReference documentReference;
    private ListenerRegistration listenerRegistration;
    private int lastKnownNoteHistoryListSize;
    private Note note;

    public OfflineNoteData(DocumentReference documentReference) {
        this.documentReference = documentReference;
        lastKnownNoteHistoryListSize = -1;
        documentReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                note = documentSnapshot.toObject(Note.class);
            }
        });
    }


    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public ListenerRegistration getListenerRegistration() {
        return listenerRegistration;
    }

    public void setListenerRegistration(ListenerRegistration listenerRegistration) {
        this.listenerRegistration = listenerRegistration;
    }

    public int getLastKnownNoteHistoryListSize() {
        return lastKnownNoteHistoryListSize;
    }

    public void setLastKnownNoteHistoryListSize(int lastKnownNoteHistoryListSize) {
        this.lastKnownNoteHistoryListSize = lastKnownNoteHistoryListSize;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
