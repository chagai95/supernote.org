package com.example.firebaseui_firestoreexample;

import com.google.firebase.Timestamp;

public class TimeReminder extends Reminder {
    private Timestamp timestamp;
    TimeReminder(Timestamp timestamp) {
        super("time");
        this.timestamp = timestamp;
    }

    @SuppressWarnings("unused")
    public TimeReminder(){
        // empty constructor needed for firestore
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
