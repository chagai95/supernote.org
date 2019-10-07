package com.example.firebaseui_firestoreexample.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.reminders.WhatsappTimeReminder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MyDatePickerFragment extends DialogFragment {

    private Context c;
    private DocumentReference documentReference;
    private String whatsappNumber, whatsappMessage;
    private ArrayList<String> usernames;
    private HashMap<String, CloudUserData> userSuggestions;

    public MyDatePickerFragment(DocumentReference documentReference, Context c, String whatsappNumber, String whatsappMessage, ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        this.documentReference = documentReference;
        this.c = c;
        this.whatsappNumber = whatsappNumber;
        this.whatsappMessage = whatsappMessage;
        if(usernames==null){
            this.usernames = new ArrayList<>();
            this.userSuggestions = new HashMap<>();
        }
        else{
            this.userSuggestions = userSuggestions;
            this.usernames = usernames;
        }
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
                    try {
                        String timeString = hourOfDay + ":" + minute + ":00";

                        //difference between now and the reminder
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY);
                        String dateReminderString = dateString + " " + timeString;
                        Date dateReminder = sdf.parse(dateReminderString);



//                    adding a whatsapp reminder to report a bug - these are saved in a note.
                        if (documentReference == null) {
                            whatsappNumber = "4915905872952";
                            documentReference = FirebaseFirestore.getInstance()
                                    .collection("notes").document("bugReports");

                        }

//                        if the whatsappNumber is an empty string it is a time reminder
//                        otherwise it is a whatsapp reminder.
                        if (whatsappNumber.equals("")) {
                            TimeReminder timeReminder = new TimeReminder(new Timestamp(Objects.requireNonNull(dateReminder)));
                            timeReminder.setNotifyUsers(getUserIDs(usernames, userSuggestions));
                            documentReference.collection("Reminders")
                                    .add(timeReminder);
                        } else {
                            String whatsappNumberModified = whatsappNumberModified();
                            WhatsappTimeReminder whatsappTimeReminder = new WhatsappTimeReminder(
                                    new Timestamp(Objects.requireNonNull(dateReminder)), whatsappNumberModified, whatsappMessage);
                            whatsappTimeReminder.setNotifyUsers(getUserIDs(usernames, userSuggestions));
                            documentReference.collection("Reminders")
                                    .add(whatsappTimeReminder);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();

    }

    private String whatsappNumberModified() {
        String whatsappNumberModified;
        if (whatsappNumber.startsWith("00"))
            whatsappNumberModified = whatsappNumber.substring(2);
        else if (whatsappNumber.startsWith("0"))
            whatsappNumberModified = "49" + whatsappNumber.substring(1);
        else if (whatsappNumber.startsWith("+"))
            whatsappNumberModified = whatsappNumber.substring(1);
        else whatsappNumberModified = whatsappNumber;
        return whatsappNumberModified;
    }

    private ArrayList<String> getUserIDs(ArrayList<String> usernames, HashMap<String, CloudUserData> userSuggestions) {
        ArrayList<String> userIDs;
        userIDs = new ArrayList<>();
        for (String s :
                usernames) {
            CloudUserData cloudUserData = userSuggestions.get(s);
            if (cloudUserData != null)
                userIDs.add(cloudUserData.getCloudUser().getUid());
        }
        return userIDs;
    }

}