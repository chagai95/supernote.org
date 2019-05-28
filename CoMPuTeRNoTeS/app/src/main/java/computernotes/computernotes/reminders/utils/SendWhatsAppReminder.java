package computernotes.computernotes.reminders.utils;

import java.util.Date;

import computernotes.computernotes.reminders.TimeReminder;


public class SendWhatsAppReminder extends TimeReminder {
    String number;
    String message;

    public SendWhatsAppReminder(Date date) {
        super(date);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
