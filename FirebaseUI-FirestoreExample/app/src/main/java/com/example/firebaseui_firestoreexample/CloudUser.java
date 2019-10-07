package com.example.firebaseui_firestoreexample;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class CloudUser {
    private String username;
    private String uid;
    private GeoPoint geoPoint;
    private ArrayList<String> friends;

    public CloudUser(String username, String uid) {
        this.username = username;
        this.uid = uid;
        friends = new ArrayList<>();
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
