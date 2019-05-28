package computernotes.computernotes.reminders;

import java.util.Date;

public class TimeReminder extends Reminder{
    Date date;

    public TimeReminder(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
