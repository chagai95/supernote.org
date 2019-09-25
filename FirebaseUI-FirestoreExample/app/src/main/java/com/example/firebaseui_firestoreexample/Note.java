package com.example.firebaseui_firestoreexample;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class Note {
    private String title;
    private String description;
    private int priority;
    private ArrayList<String> history;
    private boolean keepOffline;
    private boolean loadToCache;
    private Timestamp created;


    @SuppressWarnings("unused")
    public Note(){
        // empty constructor needed for firebase
    }
    public Note(String title, String description, int priority,Timestamp created) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.created = created;
        history = new ArrayList<>();
        keepOffline = false;
        loadToCache = false;
    }

    public Note newNoteVersion(){
        return new Note(title,description,priority, new Timestamp(new Date()));
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

    public Timestamp getCreated() {
        return created;
    }

    public boolean isKeepOffline() {
        return keepOffline;
    }

    public boolean isLoadToCache() {
        return loadToCache;
    }

}
