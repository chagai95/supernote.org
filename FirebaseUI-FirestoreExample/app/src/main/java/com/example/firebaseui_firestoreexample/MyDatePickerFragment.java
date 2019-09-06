package com.example.firebaseui_firestoreexample;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MyDatePickerFragment extends DialogFragment {

    private Context c;
    private DocumentReference documentReference;
    private String whatsappNumber, whatsappMessage;

    MyDatePickerFragment(DocumentReference documentReference, Context c, String whatsappNumber, String whatsappMessage) {
        this.documentReference = documentReference;
        this.c = c;
        this.whatsappNumber = whatsappNumber;
        this.whatsappMessage = whatsappMessage;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(Objects.requireNonNull(getActivity()), dateSetListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener =
            (view, year, month, day) -> {
                Toast.makeText(getActivity(), "selected date is " + view.getYear() +
                        " / " + (view.getMonth() + 1) +
                        " / " + view.getDayOfMonth(), Toast.LENGTH_SHORT).show();
                String monthString;
                if (view.getDayOfMonth() + 1 < 10)
                    monthString = "0" + (view.getMonth() + 1);
                else monthString = "" + (view.getMonth() + 1);
                String dayString;
                if (view.getDayOfMonth() < 10)
                    dayString = "0" + view.getDayOfMonth();
                else dayString = "" + view.getDayOfMonth();

                String dateString = view.getYear() + "/" + monthString + "/" + dayString;
                timePicker(dateString);
            };

    private void timePicker(final String dateString) {
        // Get Current Time
        final Calendar cal = Calendar.getInstance();
        final int mHour = cal.get(Calendar.HOUR_OF_DAY);
        final int mMinute = cal.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (view, hourOfDay, minute) -> {
                    String timeString = hourOfDay + ":" + minute + ":00";

                    AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                    Intent myIntent = new Intent(c, MyBroadcastReceiver.class);

                    myIntent.putExtra("whatsapp", true);
                    myIntent.putExtra("whatsappNumber", whatsappNumber);
                    myIntent.putExtra("whatsappMessage", whatsappMessage);

//                      is this for the reminder id?
                    myIntent.putExtra("noteID", documentReference.getId());


                    myIntent.setAction("TimeReminder");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, myIntent, 0);

                        Date dateReminder = null;
                    try {
                        //difference between now and the reminder
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY);
                        String dateReminderString = dateString + " " + timeString;
                        dateReminder = sdf.parse(dateReminderString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                        /*Reminder reminder = new TimeReminder("time",new Timestamp(Objects.requireNonNull(dateReminder)));
                        MyApp.remindersRef.add(reminder);*/

                        documentReference.collection("Reminders")
                                .add(new TimeReminder("time", new Timestamp(Objects.requireNonNull(dateReminder))))
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) myIntent.putExtra("reminderID",Objects.requireNonNull(task.getResult()).getId());
                                });


                        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, dateReminder.getTime(), pendingIntent);





                }, mHour, mMinute, false);
        timePickerDialog.show();

    }


}