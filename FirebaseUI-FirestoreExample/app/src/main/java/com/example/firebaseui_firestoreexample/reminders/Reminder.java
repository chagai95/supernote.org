package com.example.firebaseui_firestoreexample.reminders;

import com.example.firebaseui_firestoreexample.MyApp;

import java.util.ArrayList;

public abstract class Reminder {
    private String type;
    private boolean done;
    private boolean trash;
//    refactor to "creator" including the index and all other uses
    private String uid;
    private ArrayList<String> notifyUsers;

    Reminder(String type) {
        this.type = type;
        this.uid = MyApp.myCloudUserData.getCloudUser().getUid();
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

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(ArrayList<String> notifyUsers) {
        this.notifyUsers = notifyUsers;
    }
}
