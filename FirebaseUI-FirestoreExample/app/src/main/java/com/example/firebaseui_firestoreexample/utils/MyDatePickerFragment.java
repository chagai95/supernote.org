package com.example.firebaseui_firestoreexample.utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.EditNoteActivity;
import com.example.firebaseui_firestoreexample.activities.MainActivity;
import com.example.firebaseui_firestoreexample.activities.OpenFragmentActivity;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.firebaseui_firestoreexample.MyApp.dialogNotShowing;
import static com.example.firebaseui_firestoreexample.MyApp.dialogShowing;
import static com.example.firebaseui_firestoreexample.MyApp.newDialogShowing;

public class MyDatePickerFragment extends DialogFragment {

    private Context c;
    private DocumentReference noteDocumentReference;
    private TimeReminder timeReminder;

    public MyDatePickerFragment(DocumentReference noteDocumentReference, Context c,TimeReminder timeReminder) {
        this.noteDocumentReference = noteDocumentReference;
        this.c = c;
        this.timeReminder = timeReminder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        dialogShowing();
        MyApp.newDialogShowing = true;

        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), dateSetListener, year, month, day);
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "cancel", (dialog, which) -> {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialogNotShowing();
            }
        });
        /*
        TODO does not find the buttons for some reason even though it extends the AlertDialog class.
        Button btnPositive = datePickerDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = datePickerDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);*/
        setDialogView(datePickerDialog);
        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.setCancelable(false); //TODO this did not work and will put the variable showingDialog out of sync!
        // when the back button is pressed when the datePickerDialog is open
        //TODO get this to work - it does not get called when dismissed
        datePickerDialog.setOnDismissListener(dialog -> {
            if(newDialogShowing)
                newDialogShowing = false;
            else
                dialogNotShowing();
        });
        return datePickerDialog;
    }

    private void setDialogView(AlertDialog alertDialog) {
        if(MyApp.getActivity(c)instanceof EditNoteActivity) {
            ((EditNoteActivity) MyApp.getActivity(c)).dialogView = alertDialog.findViewById(android.R.id.content);
        }
        if(MyApp.getActivity(c)instanceof MainActivity) {
            ((MainActivity) MyApp.getActivity(c)).dialogView = alertDialog.findViewById(android.R.id.content);
        }
        if(MyApp.getActivity(c)instanceof OpenFragmentActivity) {
            ((OpenFragmentActivity) MyApp.getActivity(c)).dialogView = alertDialog.findViewById(android.R.id.content);
        }
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

        dialogShowing();
        MyApp.newDialogShowing = true;


        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (view, hourOfDay, minute) -> {
                    try {
                        String timeString = hourOfDay + ":" + minute + ":00";

                        //difference between now and the reminder
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY);
                        String dateReminderString = dateString + " " + timeString;
                        Date dateReminder = sdf.parse(dateReminderString);

                        timeReminder.setTimestamp(new Timestamp(dateReminder));

//                    adding a whatsapp reminder to report a bug - these reminders are saved in a note.
                        if (noteDocumentReference == null) {
                            timeReminder.setWhatsappNumber("4915905872952");
                            noteDocumentReference = FirebaseFirestore.getInstance()
                                    .collection("notes").document("bugReports");
                        }

                        FirebaseFirestore.getInstance().enableNetwork();
                            noteDocumentReference.collection("Reminders")
                                    .add(timeReminder).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentReference documentReference = task.getResult();
                                    assert documentReference != null;
//                            disabling network here because there is no reminder listener.
                                    if (MyApp.userSkippedLogin) {
                                        MyApp.timeReminders.put(Objects.requireNonNull(task.getResult()).getId(),
                                                new TimeReminderData(task.getResult(), timeReminder, -1));
                                        // TODO add reminder to alarm manager!
                                        FirebaseFirestore.getInstance().disableNetwork();
                                    }
                                }

                            });

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }, mHour, mMinute, false);
        /*Button btnPositive = timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);*/

        setDialogView(timePickerDialog);
        timePickerDialog.setOnDismissListener(dialog ->{
            MyApp.dialogNotShowing();
        } );
        timePickerDialog.show();

    }




}