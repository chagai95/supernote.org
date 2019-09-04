package com.example.firebaseui_firestoreexample;

abstract class Reminder {
    String type;

    Reminder(String type) {
        this.type = type;
    }

    @SuppressWarnings("unused")
    Reminder(){
        // empty constructor needed for firebase
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
