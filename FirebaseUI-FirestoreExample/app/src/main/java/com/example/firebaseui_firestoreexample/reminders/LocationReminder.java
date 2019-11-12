package com.example.firebaseui_firestoreexample.reminders;

import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.GeoPoint;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationReminder extends Reminder {
    private GeoPoint geoPoint;
    private double radius;
    private boolean arrive;
    private boolean leave;
    // 0 is the beginning of the day
    private int startHourOfDay;
    // 0 is the end of the day
    private int endHourOfDay;
    private ArrayList<String> daysOfWeek;

    public LocationReminder(double radius) {
        super("location");
        this.radius = radius;
        arrive = true;
        daysOfWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            daysOfWeek.add("sunday");
            daysOfWeek.add("monday");
            daysOfWeek.add("tuesday");
            daysOfWeek.add("wednesday");
            daysOfWeek.add("thursday");
            daysOfWeek.add("friday");
            daysOfWeek.add("saturday");
        }
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

    public int getStartHourOfDay() {
        return startHourOfDay;
    }

    public void setStartHourOfDay(int startHourOfDay) {
        this.startHourOfDay = startHourOfDay;
    }

    public int getEndHourOfDay() {
        return endHourOfDay;
    }

    public void setEndHourOfDay(int endHourOfDay) {
        this.endHourOfDay = endHourOfDay;
    }

    public ArrayList<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(ArrayList<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
}
