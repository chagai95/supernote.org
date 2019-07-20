package com.example.firebaseui_firestoreexample;

public class Note {
    private String title;
    private String description;
    private int priority;

    public Note(){
        // empty constructor needed for firebase
    }
    public Note(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
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
}
