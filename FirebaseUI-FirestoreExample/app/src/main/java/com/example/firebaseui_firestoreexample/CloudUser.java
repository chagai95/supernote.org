package com.example.firebaseui_firestoreexample;

import java.util.ArrayList;

class CloudUser {
    private String username;
    private ArrayList<String> friends;

    public CloudUser(String username) {
        this.username = username;
    }

    public CloudUser(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }
}
