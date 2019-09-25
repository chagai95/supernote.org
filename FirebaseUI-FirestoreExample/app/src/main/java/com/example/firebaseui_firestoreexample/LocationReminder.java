package com.example.firebaseui_firestoreexample;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

public class LocationReminder extends Reminder{
    GeoPoint geoPoint;
    private double radius;
    private boolean arrive;
    private boolean leave;

    LocationReminder(GeoPoint geoPoint,double radius) {
        super("location");
        this.radius = radius;
        this.geoPoint = geoPoint;
    }

    @SuppressWarnings("unused")
    public LocationReminder(){
        // empty constructor needed for firestore
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
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

    public Location getLocation() {
        Location location = new Location("");//provider name is unnecessary
        location.setLatitude(geoPoint.getLatitude());
        location.setLongitude(geoPoint.getLongitude());
        return location;
    }
}
