package com.example.firebaseui_firestoreexample.reminders;


import com.google.firebase.Timestamp;

public class WhatsappTimeReminder extends TimeReminder {
    private String number;
    private String message;

    public WhatsappTimeReminder(Timestamp timestamp, String number, String message) {
        super(timestamp);
        setType("whatsapp time");
        this.number = number;
        this.message = message;
    }

    public WhatsappTimeReminder() {
        // empty constructor needed for firestore
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
