package com.example.firebaseui_firestoreexample;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class Note {
    private String title;
    private String description;
    private int priority;
    private ArrayList<String> history;
    private boolean keepOffline;

    @SuppressWarnings("unused")
    public Note(){
        // empty constructor needed for firebase
    }
    public Note(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        history = new ArrayList<>();
        keepOffline = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public boolean isKeepOffline() {
        return keepOffline;
    }
}
