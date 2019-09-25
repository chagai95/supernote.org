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
    private ArrayList<String> shared;
    private boolean keepOffline;
    private boolean loadToCache;
    private boolean trash;
    private Timestamp created;
    private String creator;


    @SuppressWarnings("unused")
    public Note(){
        // empty constructor needed for firebase
    }
    public Note(String title, String description, int priority, Timestamp created, String creator) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.created = created;
        this.creator = creator;
        history = new ArrayList<>();
        shared = new ArrayList<>();
        keepOffline = false;
        loadToCache = false;
        trash = false;
    }

    public Note newNoteVersion(){
        return new Note(title,description,priority, new Timestamp(new Date()), creator);
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

    public String getCreator() {
        return creator;
    }

    public ArrayList<String> getShared() {
        return shared;
    }

    public boolean isTrash() {
        return trash;
    }
}
