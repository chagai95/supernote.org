package com.example.firebaseui_firestoreexample.reminders;

import com.example.firebaseui_firestoreexample.MyApp;

import java.util.ArrayList;

public abstract class Reminder {
    private String type;
    private boolean done;
    private boolean trash;
    private String preview;
    private String whatsappMessage;
    private String whatsappNumber;
//    refactor to "creator" including the index and all other uses
    private String uid;
    private ArrayList<String> notifyUsers;
    private int amountOfRepeats;

    Reminder(String type) {
        this.type = type;
        this.uid = MyApp.userUid;
        if(MyApp.userSkippedLogin){
            notifyUsers = new ArrayList<>();
            notifyUsers.add(uid);
        }
        amountOfRepeats = 1;
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

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getWhatsappMessage() {
        return whatsappMessage;
    }

    public void setWhatsappMessage(String whatsappMessage) {
        this.whatsappMessage = whatsappMessage;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public int getAmountOfRepeats() {
        return amountOfRepeats;
    }

    public void setAmountOfRepeats(int amountOfRepeats) {
        this.amountOfRepeats = amountOfRepeats;
    }

    public void decrementAmountOfRepeats(){
        amountOfRepeats--;
    }
}
