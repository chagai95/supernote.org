package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.reminders.Reminder;
import com.google.firebase.firestore.DocumentReference;

public class ReminderData {
    private DocumentReference documentReference;
    private Reminder reminder;
    private int notificationID;


    public ReminderData(DocumentReference documentReference, Reminder reminder) {
        this.documentReference = documentReference;
        this.reminder = reminder;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }
}
