package computernotes.computernotes.reminders;

import android.location.Location;

import computernotes.computernotes.users.CloudUser;

public class UserReminder extends Reminder {
    private CloudUser cloudUser;
    private boolean notifyUser;
    private double radius;
    private boolean arrive;
    private boolean leave;

    public UserReminder(double radius, boolean arrive, boolean leave, CloudUser cloudUser, boolean notifyUser) {
        this.cloudUser = cloudUser;
        this.notifyUser = notifyUser;
        this.radius = radius;
        this.arrive = arrive;
        this.leave = leave;
    }

    public CloudUser getCloudUser() {
        return cloudUser;
    }

    public void setCloudUser(CloudUser cloudUser) {
        this.cloudUser = cloudUser;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
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
