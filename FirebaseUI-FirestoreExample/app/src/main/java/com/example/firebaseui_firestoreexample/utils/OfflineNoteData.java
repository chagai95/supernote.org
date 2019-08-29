package com.example.firebaseui_firestoreexample.utils;

import com.example.firebaseui_firestoreexample.Note;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

public class OfflineNoteData {
    private Note note;
    private DocumentReference documentReference;
    private ListenerRegistration listenerRegistration;
    private boolean keepOffline;

    public OfflineNoteData(DocumentReference documentReference) {
        this.documentReference = documentReference;
        keepOffline = false;
    }

    @SuppressWarnings("unused")
    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
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

    public boolean isKeepOffline() {
        return keepOffline;
    }

    public void setKeepOffline(boolean keepOffline) {
        this.keepOffline = keepOffline;
    }
}
