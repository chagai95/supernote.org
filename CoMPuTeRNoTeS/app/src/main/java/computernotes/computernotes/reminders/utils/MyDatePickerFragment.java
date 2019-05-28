package computernotes.computernotes.reminders.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.utils.notifications.MyBroadcastReceiver;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.reminders.Reminder;
import computernotes.computernotes.reminders.TimeReminder;

@SuppressLint("ValidFragment")
public class MyDatePickerFragment extends DialogFragment {

    Context c;
    NoteMain noteMain;
    int reminderPosition;
    String number,message;

    public MyDatePickerFragment(NoteMain noteMain, int reminderPosition, Context c, String number, String message) {
        this.noteMain = noteMain;
        this.c = c;
        this.reminderPosition = reminderPosition;
        this.number = number;
        this.message = message;
    }

    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), dateSetListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    Toast.makeText(getActivity(), "selected date is " + view.getYear() +
                            " / " + (view.getMonth()+1) +
                            " / " + view.getDayOfMonth(), Toast.LENGTH_SHORT).show();
                    String monthString;
                    if (view.getDayOfMonth()+1<10)
                        monthString = "0"+(view.getMonth()+1) ;
                    else monthString = ""+(view.getMonth()+1) ;
                    String dayString;
                    if (view.getDayOfMonth()<10)
                        dayString = "0"+view.getDayOfMonth() ;
                    else dayString = ""+view.getDayOfMonth() ;

                        String dateString = view.getYear()+ "/"+monthString+"/"+dayString;
                        timePicker(dateString);
                }
            };

    private void timePicker(final String dateString){
        // Get Current Time
        final Calendar cal = Calendar.getInstance();
        final int mHour = cal.get(Calendar.HOUR_OF_DAY);
        final int mMinute = cal.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String timeString = hourOfDay+":"+ minute + ":00"  ;

                        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);
                        myIntent.putExtra("whatsapp",true);
                        Log.d("numberPutExtra", number);
                        myIntent.putExtra("number",number);
                        myIntent.putExtra("message",message);

                        myIntent.putExtra("note_index", ServerCommunicator.notes.indexOf(noteMain));


                        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, myIntent, 0);

                        try {
                            //difference between now and the reminder
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String dateReminderString = dateString+" "+ timeString;
                            Date dateReminder = sdf.parse(dateReminderString);
                            TimeReminder timeReminder = new TimeReminder(dateReminder);
                            noteMain.addReminder(timeReminder);

                            if (alarmManager != null) {
                                alarmManager.set(AlarmManager.RTC_WAKEUP,  dateReminder.getTime(), pendingIntent);
                                if (reminderPosition!=-1) {
                                    ((LinkedList<Reminder>)noteMain.getReminders()).remove(reminderPosition);  // only when a new reminder is in then remove old reminder
                                }
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();

    }


}