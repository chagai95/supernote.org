package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.google.firebase.firestore.DocumentReference;

public class TimeReminderData extends ReminderData{
    private DocumentReference documentReference;
    private TimeReminder timeReminder;
    private int alarmID;

    public TimeReminderData(DocumentReference documentReference, TimeReminder timeReminder, int alarmID) {
        super(documentReference,timeReminder);
        this.documentReference = documentReference;
        this.timeReminder = timeReminder;
        this.alarmID = alarmID;
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

    public int getAlarmID() {
        return alarmID;
    }

    public void setAlarmID(int alarmID) {
        this.alarmID = alarmID;
    }
}
