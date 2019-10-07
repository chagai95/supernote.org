package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.google.firebase.firestore.DocumentReference;

public class LocationReminderData extends ReminderData{
    private DocumentReference documentReference;
    private LocationReminder locationReminder;

    public LocationReminderData(DocumentReference documentReference, LocationReminder locationReminder) {
        super(documentReference,locationReminder);
        this.documentReference = documentReference;
        this.locationReminder = locationReminder;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public LocationReminder getLocationReminder() {
        return locationReminder;
    }

    public void setLocationReminder(LocationReminder locationReminder) {
        this.locationReminder = locationReminder;
    }
}
