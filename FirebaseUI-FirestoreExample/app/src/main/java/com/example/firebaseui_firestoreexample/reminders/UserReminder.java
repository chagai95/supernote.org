package com.example.firebaseui_firestoreexample.reminders;

import com.example.firebaseui_firestoreexample.CloudUser;

// make this a sub class of location reminder and change the location every time the other user
// moves with a listener.
public class UserReminder extends LocationReminder {
    private String cloudUserID;
    private double radius;
    private boolean arrive;
    private boolean leave;

    public UserReminder(String cloudUserID, double radius) {
        super(radius);
        setType("user");
        this.cloudUserID = cloudUserID;
        this.radius = radius;
    }

    @SuppressWarnings("unused")
    public UserReminder(){
        // empty constructor needed for firestore
    }

    public String getCloudUserID() {
        return cloudUserID;
    }

    public void setCloudUserID(String cloudUserID) {
        this.cloudUserID = cloudUserID;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isArrive() {
        return arrive;
    }

    public void setArrive(boolean arrive) {
        this.arrive = arrive;
    }

    public boolean isLeave() {
        return leave;
    }

    public void setLeave(boolean leave) {
        this.leave = leave;
    }
}
