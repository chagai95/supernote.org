package computernotes.computernotes.activities.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import computernotes.computernotes.R;
import computernotes.computernotes.reminders.LocationReminder;
import computernotes.computernotes.reminders.Reminder;
import computernotes.computernotes.reminders.TimeReminder;
import computernotes.computernotes.reminders.UserReminder;

public class RemindersInNoteAdapter extends RecyclerView.Adapter<RemindersInNoteAdapter.ReminderViewHolder> {


    //this context we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the reminders in a list
    private List<Reminder> remindersList;

    //getting the context and reminder list with constructor
    public RemindersInNoteAdapter(Context cOtherActivity, List<Reminder> remindersList) {
        this.cOtherActivity = cOtherActivity;
        this.remindersList = remindersList;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.reminders_in_note_item, null);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
//        getting the reminder of the specified position

//        Edited by chagai on the 3/12/2019 at 5:36 PM
//        differentiate between timeReminder and locationReminder
//        binding the data with the viewHolder views
        if (remindersList.get(position) instanceof TimeReminder)
            holder.reminderTextView.setText(((TimeReminder) remindersList.get(position)).getDate().toString());
        if (remindersList.get(position) instanceof LocationReminder)
            holder.reminderTextView.setText(remindersList.get(position).toString());
        if (remindersList.get(position) instanceof UserReminder)
            holder.reminderTextView.setText(remindersList.get(position).toString());


    }


    @Override
    public int getItemCount() {
        return remindersList.size();
    }


    class ReminderViewHolder extends RecyclerView.ViewHolder {

        TextView reminderTextView;

        ReminderViewHolder(View itemView) {
            super(itemView);

            reminderTextView = itemView.findViewById(R.id.reminderDetails);
        }
    }
}
