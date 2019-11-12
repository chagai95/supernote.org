package com.example.firebaseui_firestoreexample.activities.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.reminders.UserReminder;

import java.util.Date;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderHolder> {


    //this activity we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the Strings in a list
    private List<ReminderData> remindersList;

    //getting the activity and String list with constructor
    public ReminderAdapter(Context cOtherActivity, List<ReminderData> remindersList) {
        this.cOtherActivity = cOtherActivity;
        this.remindersList = remindersList;
    }

    @NonNull
    @Override
    public ReminderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.reminder_item, null);
        return new ReminderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderHolder holder, int position) {
        switch (MyApp.recyclerViewModeReminder) {
            case "time":
                holder.reminderItemTitle.setText(((TimeReminder) remindersList.get(position).getReminder()).getTimestamp().toDate().toString());
                if (MyApp.recyclerViewModeReminderShowDone && remindersList.get(position).getReminder().isDone())
                    holder.reminderItemDescription.setText("flagged \"done\"");
                else if (((TimeReminder) remindersList.get(position).getReminder()).getTimestamp().toDate().before(new Date()))
                    holder.reminderItemDescription.setText("date has passed!!");

                break;
            case "location":
                holder.reminderItemTitle.setText(((LocationReminder) remindersList.get(position).getReminder()).getGeoPoint().toString());
                if (MyApp.recyclerViewModeReminderShowDone && remindersList.get(position).getReminder().isDone())
                    holder.reminderItemDescription.setText("flagged \"done\"");
                else
                    holder.reminderItemDescription.setText("Radius: " + ((LocationReminder) remindersList.get(position).getReminder()).getRadius());
                break;
            case "user":
                CloudUserData cloudUserData = MyApp.userReminderUsers.get(((UserReminder) remindersList.get(position).getReminder()).getCloudUserID());
                assert cloudUserData != null;
                String username = cloudUserData.getCloudUser().getUsername();
                holder.reminderItemTitle.setText(username);
                if (MyApp.recyclerViewModeReminderShowDone && remindersList.get(position).getReminder().isDone())
                    holder.reminderItemDescription.setText("flagged \"done\"");
                else
                    holder.reminderItemDescription.setText("Radius: " + ((UserReminder) remindersList.get(position).getReminder()).getRadius());
                break;
            default:
                holder.reminderItemTitle.setText(remindersList.get(position).getReminder().getType());
                if (MyApp.recyclerViewModeReminderShowDone && remindersList.get(position).getReminder().isDone())
                    holder.reminderItemDescription.setText("flagged \"done\"");
                else
                    switch (remindersList.get(position).getReminder().getType()) {
                        case "time":
                        case "whatsapp time":
                            holder.reminderItemDescription.setText(((TimeReminder) remindersList.get(position).getReminder()).getTimestamp().toDate().toString());
                            break;
                        case "location":
                            holder.reminderItemDescription.setText(((LocationReminder) remindersList.get(position).getReminder()).getGeoPoint().toString());
                            break;
                        case "user":
                            CloudUserData cloudUserData2 = MyApp.userReminderUsers.get(((UserReminder) remindersList.get(position).getReminder()).getCloudUserID());
                            assert cloudUserData2 != null;
                            String username2 = cloudUserData2.getCloudUser().getUsername();
                            holder.reminderItemDescription.setText(username2);
                            break;
                    }
        }
    }


    @Override
    public int getItemCount() {
        return remindersList.size();
    }


    class ReminderHolder extends RecyclerView.ViewHolder {

        TextView reminderItemTitle;
        TextView reminderItemDescription;

        ReminderHolder(View itemView) {
            super(itemView);

            reminderItemTitle = itemView.findViewById(R.id.reminderItemTitle);
            reminderItemDescription = itemView.findViewById(R.id.reminderItemDescription);
        }
    }
}

