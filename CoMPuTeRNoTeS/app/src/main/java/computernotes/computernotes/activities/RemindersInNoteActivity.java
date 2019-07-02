package computernotes.computernotes.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import java.util.List;

import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.activities.adapters.RemindersInNoteAdapter;
import computernotes.computernotes.utils.ItemClickSupport;
import computernotes.computernotes.reminders.utils.MyDatePickerFragment;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.reminders.Reminder;

public class RemindersInNoteActivity extends AppCompatActivity {
    RecyclerView recyclerViewRemindersInNoteActivity;

    RemindersInNoteAdapter remindersInNoteAdapter;

    //this context we will use to inflate the layout
    Context c = this;

    //we are storing all the reminders in a list
    private List<Reminder> remindersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_in_note);
        recyclerViewRemindersInNoteActivity = findViewById(R.id.recyclerViewRemindersInNote);


        NoteMain noteMain = ServerCommunicator.notes.get(getIntent().getIntExtra("note_index",-1));

        recyclerViewRemindersInNoteActivity.setHasFixedSize(true);
        recyclerViewRemindersInNoteActivity.setLayoutManager(new LinearLayoutManager(c));
        remindersList = (List<Reminder>) noteMain.getReminders();
        //creating recyclerview adapterFirestoreRecyclerViewNoteList
        remindersInNoteAdapter = new RemindersInNoteAdapter(c, remindersList);

        //setting adapterFirestoreRecyclerViewNoteList to recyclerview
        recyclerViewRemindersInNoteActivity.setAdapter(remindersInNoteAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //Remove swiped item from list and notify the RecyclerView
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left delete reminder
                    deleteReminder(position);
                    remindersInNoteAdapter.notifyItemRemoved(position); //item removed from recylcerview
                }
                if (direction == ItemTouchHelper.RIGHT) {    //if swipe right change reminder
                    showDatePicker(position);
                    remindersInNoteAdapter.notifyItemRemoved(position); //item removed from recylcerview
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerViewRemindersInNoteActivity);


        ItemClickSupport.addTo(recyclerViewRemindersInNoteActivity)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        System.out.println("reminder clicked");
//                        adapterFirestoreRecyclerViewNoteList.notifyItemRemoved(position); //item removed from recylcerview
//                        Reminder reminder = remindersList.remove(position);  //then remove item
                        // problem: when canceled the old reminder is not saved
                        // solution: add this reminder to the list when cancel is clicked
                        // differentiate between opening DatePicker from here as edit and from new reminder.
                        showDatePicker(position);
                        remindersInNoteAdapter.notifyDataSetChanged();
                    }
                });

    }

    private void deleteReminder(int position) {
//        Edited by chagai on the 3/13/2019 at 8:51 AM
//        https://stackoverflow.com/questions/14485368/delete-alarm-from-alarmmanager-using-cancel-android/14498387
//        remove the reminder from alarm manager
//        problems: the context used is different every time - we need a fixed context.
//                  the reminder does not know which note it belongs to, might be necessary.
//                  the reminder should perhaps have an firebaseID so we can access it easily.
//                  the alarm manager should be updated every time the remindersList is changed and not only when a reminder is being deleted from this specific place.
        remindersList.remove(position);  //then remove item
    }
//  Edited by chagai on the 3/13/2019 at 8:29 AM
//  simulating change reminder but actually deleting the old reminder and adding a new one.
    public void showDatePicker(int position) {
        NoteMain noteMain = ServerCommunicator.notes.get(getIntent().getIntExtra("note_index",-1));
        DialogFragment newFragment = new MyDatePickerFragment(noteMain,position,c,"","");
        newFragment.show(getFragmentManager(), "date picker");
    }

    @Override
    protected void onResume() {
        remindersInNoteAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
