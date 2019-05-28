package computernotes.computernotes.reminders;

import android.location.Location;

public class LocationReminder extends Reminder{
    private Location location;
    private double radius;
    private boolean arrive;
    private boolean leave;

    public LocationReminder(Location location, double radius, boolean arrive, boolean leave) {
        this.location = location;
        this.radius = radius;
        this.arrive = arrive;
        this.leave = leave;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

    @Override
    public String toString() {
        return "location:" + location.getLatitude() + ", " + location.getLongitude() + " :: " + "radius: " + radius + " :: " + "arrive: " + arrive + " :: " + "leave: " + leave;
    }
}
