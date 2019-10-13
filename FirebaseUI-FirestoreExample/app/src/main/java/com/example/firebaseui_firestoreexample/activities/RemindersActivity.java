package com.example.firebaseui_firestoreexample.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.adapters.ReminderAdapter;
import com.example.firebaseui_firestoreexample.firestore_data.OfflineNoteData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.example.firebaseui_firestoreexample.reminders.Reminder;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.reminders.UserReminder;
import com.example.firebaseui_firestoreexample.reminders.WhatsappTimeReminder;
import com.example.firebaseui_firestoreexample.utils.RecyclerItemClickListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class RemindersActivity extends MyActivity {
    public static HashMap<String, ReminderData> remindersMap ;
    private LinkedList<ReminderData>            remindersList;
    Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        hideKeyboard();
        loadReminders();
        swipeToRefresh();
    }

    private void loadReminders() {
        remindersMap = new HashMap<>();
        remindersList = new LinkedList<>();
        OfflineNoteData offlineNoteData = MyApp.allNotes.get(getIntent().getStringExtra("noteID"));
        assert offlineNoteData != null;
        db.enableNetwork();
        offlineNoteData.getDocumentReference().collection("Reminders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (MyApp.internetDisabledInternally || MyApp.userSkippedLogin)
                    db.disableNetwork();
                QuerySnapshot result = task.getResult();
                assert result != null;
                for (DocumentSnapshot documentSnapshot :
                        result.getDocuments()) {
                    remindersMap.put(documentSnapshot.getId(),
                            new ReminderData(documentSnapshot.getReference(), getReminder(documentSnapshot)));
                }
                setUpRecyclerView(); // change this to load after getting the reminders!!

            }

        });


    }

    private Reminder getReminder(DocumentSnapshot documentSnapshot) {
        Reminder reminder = null;
        switch ((String) Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("type"))) {
            case "time":
                reminder = documentSnapshot.toObject(TimeReminder.class);
                break;
            case "location":
                reminder = documentSnapshot.toObject(LocationReminder.class);
                break;
            case "user":
                reminder = documentSnapshot.toObject(UserReminder.class);
                break;
            case "whatsapp time":
                reminder = documentSnapshot.toObject(WhatsappTimeReminder.class);
                break;
        }
        return reminder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.reminders_activity_menu, menu);

        MenuItem recyclerViewModeReminderShowDoneMenuItem = menu.findItem(R.id.done);


        if (MyApp.recyclerViewModeReminderShowDone)
            recyclerViewModeReminderShowDoneMenuItem.setTitle("hide done");
        else
            recyclerViewModeReminderShowDoneMenuItem.setTitle("show done");

        MenuItem recyclerViewModeReminderShowOtherUsersMenuItem = menu.findItem(R.id.otherUsers);


        if (MyApp.recyclerViewModeReminderShowOtherUsers)
            recyclerViewModeReminderShowOtherUsersMenuItem.setTitle("show mine");
        else
            recyclerViewModeReminderShowOtherUsersMenuItem.setTitle("show other users");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.time:
                MyApp.recyclerViewModeReminder = "time";
                recreate();
                return true;
            case R.id.location:
                MyApp.recyclerViewModeReminder = "location";
                recreate();
                return true;
            case R.id.user:
                MyApp.recyclerViewModeReminder = "user";
                recreate();
                return true;
            case R.id.whatsapp:
                MyApp.recyclerViewModeReminder = "whatsapp time";
                recreate();
                return true;
            case R.id.otherUsers:
                MyApp.recyclerViewModeReminderShowOtherUsers = !MyApp.recyclerViewModeReminderShowOtherUsers;
                recreate();
                return true;
            case R.id.all:
                MyApp.recyclerViewModeReminder = "all";
                recreate();
                return true;
            case R.id.done:
                MyApp.recyclerViewModeReminderShowDone = !MyApp.recyclerViewModeReminderShowDone;
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView() {
        for (ReminderData reminderData :
                remindersMap.values()) {
            switch (MyApp.recyclerViewModeReminder) {
                case "time":
                case "location":
                case "user":
                case "whatsapp time":
                    if (reminderData.getReminder().getType().equals(MyApp.recyclerViewModeReminder))
                        addReminder(reminderData);
                    break;
                default:
                    addReminder(reminderData);
            }
        }
        ReminderAdapter adapter = new ReminderAdapter(c, remindersList);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_reminders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    remindersList.remove(viewHolder.getAdapterPosition())
                            .getDocumentReference().delete();

                    Toast.makeText(c, "deleted", Toast.LENGTH_SHORT).show();
                }
                if (direction == ItemTouchHelper.RIGHT) {
                    ReminderData reminderData = remindersList.remove(viewHolder.getAdapterPosition());
                    if (reminderData.getReminder().isDone()) {
                        reminderData.getDocumentReference().update("done", false).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(c, "reminder undone", Toast.LENGTH_SHORT).show();
                                // change the array as well so hopefully it updates in real time.
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } else {
                        reminderData.getDocumentReference().update("done", true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // change the array as well so hopefully it updates in real time.
                                Toast.makeText(c, "reminder flagged \"done\"", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                        });
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // do whatever
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                        }));
    }

    private void addReminder(ReminderData reminderData) {
        if (MyApp.recyclerViewModeReminderShowDone) {
            if (MyApp.recyclerViewModeReminderShowOtherUsers) {
                if (!reminderData.getReminder().getNotifyUsers().contains(MyApp.userUid))
                    remindersList.add(reminderData);
            } else if (reminderData.getReminder().getNotifyUsers().contains(MyApp.userUid))
                remindersList.add(reminderData);
        } else if (!reminderData.getReminder().isDone()) {
            if (MyApp.recyclerViewModeReminderShowOtherUsers) {
                if (!reminderData.getReminder().getNotifyUsers().contains(MyApp.userUid))
                    remindersList.add(reminderData);
            } else if (reminderData.getReminder().getNotifyUsers().contains(MyApp.userUid))
                remindersList.add(reminderData);
        }
    }

    private void swipeToRefresh() {
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshRemindersActivity);
        if (MyApp.userSkippedLogin) {
            pullToRefresh.setRefreshing(false);
            pullToRefresh.setEnabled(false);
        } else
            pullToRefresh.setOnRefreshListener(() -> {
                if (MyApp.internetDisabledInternally)
                    askAboutActivatingInternalInternet();
                else
                    recreate();
                pullToRefresh.setRefreshing(false);
            });
    }


}
