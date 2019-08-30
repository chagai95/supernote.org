package com.example.firebaseui_firestoreexample.utils;

import com.example.firebaseui_firestoreexample.Note;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

public class OfflineNoteData {

    private DocumentReference documentReference;
    private ListenerRegistration listenerRegistration;

    public OfflineNoteData(DocumentReference documentReference) {
        this.documentReference = documentReference;
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

}
