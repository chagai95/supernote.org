package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.reminders.UserReminder;
import com.google.firebase.firestore.DocumentReference;

public class UserReminderData extends LocationReminderData {
    private DocumentReference documentReference;
    private UserReminder userReminder;

    public UserReminderData(DocumentReference documentReference, UserReminder userReminder) {
        super(documentReference,userReminder);
        this.documentReference = documentReference;
        this.userReminder = userReminder;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public UserReminder getUserReminder() {
        return userReminder;
    }

    public void setUserReminder(UserReminder userReminder) {
        this.userReminder = userReminder;
    }
}
