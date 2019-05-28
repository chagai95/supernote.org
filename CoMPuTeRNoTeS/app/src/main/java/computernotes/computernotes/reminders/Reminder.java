package computernotes.computernotes.reminders;

import computernotes.computernotes.exceptions.NotImplementedException;

public abstract class Reminder {
    int repeat;
    String notificationText;
    String fireBaseID;

    private void deleteReminder()throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void editReminder()throws NotImplementedException
    {
        throw new NotImplementedException();
    }
}
