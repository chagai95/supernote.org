package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

public class TimeReminderData extends ReminderData{
    private DocumentReference documentReference;
    private TimeReminder timeReminder;

    public TimeReminderData(DocumentReference documentReference, TimeReminder timeReminder) {
        super(documentReference,timeReminder);
        this.documentReference = documentReference;
        this.timeReminder = timeReminder;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public TimeReminder getTimeReminder() {
        return timeReminder;
    }

    public void setTimeReminder(TimeReminder timeReminder) {
        this.timeReminder = timeReminder;
    }

}
