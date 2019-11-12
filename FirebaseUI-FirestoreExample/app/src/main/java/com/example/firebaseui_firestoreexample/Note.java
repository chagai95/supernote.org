package com.example.firebaseui_firestoreexample;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class Note {
    private String title;
    private String description;
    private ArrayList<String> history;
    private ArrayList<String> titleHistory;
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
    public Note(String title, String description, Timestamp created, String creator) {
        this.title = title;
        this.description = description;
        this.created = created;
        this.creator = creator;
        history = new ArrayList<>();
        titleHistory = new ArrayList<>();
        shared = new ArrayList<>();
        keepOffline = false;
        loadToCache = false;
        trash = false;
    }

    public Note newNoteVersion(){
        return new Note(title,description, new Timestamp(new Date()), creator);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public ArrayList<String> getTitleHistory() {
        return titleHistory;
    }

    public void setTitleHistory(ArrayList<String> titleHistory) {
        this.titleHistory = titleHistory;
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
