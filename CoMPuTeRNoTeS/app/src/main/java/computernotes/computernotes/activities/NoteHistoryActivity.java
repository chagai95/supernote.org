package computernotes.computernotes.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.activities.adapters.NoteHistoryAdapter;
import computernotes.computernotes.utils.ItemClickSupport;
import computernotes.computernotes.note.NoteMain;

public class NoteHistoryActivity extends AppCompatActivity {
    RecyclerView recyclerViewNoteHistoryActivity;

    NoteHistoryAdapter noteHistoryAdapter;

    //this context we will use to inflate the layout
    Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_history);
        recyclerViewNoteHistoryActivity = findViewById(R.id.recyclerViewNoteHistory);


        NoteMain noteMain = ServerCommunicator.notes.get(getIntent().getIntExtra("note_index",-1));

        recyclerViewNoteHistoryActivity.setHasFixedSize(true);
        recyclerViewNoteHistoryActivity.setLayoutManager(new LinearLayoutManager(c));
        //creating recyclerview adapterFirestoreRecyclerViewNoteList
        noteHistoryAdapter = new NoteHistoryAdapter(c, noteMain.getNoteHistory());

        //setting adapterFirestoreRecyclerViewNoteList to recyclerview
        recyclerViewNoteHistoryActivity.setAdapter(noteHistoryAdapter);
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
                    noteHistoryAdapter.notifyItemRemoved(position); //item removed from recylcerview
                }
                if (direction == ItemTouchHelper.RIGHT) {    //if swipe right change reminder
                    noteHistoryAdapter.notifyItemRemoved(position); //item removed from recylcerview
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerViewNoteHistoryActivity);


        ItemClickSupport.addTo(recyclerViewNoteHistoryActivity)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        System.out.println("reminder clicked");
//                        adapterFirestoreRecyclerViewNoteList.notifyItemRemoved(position); //item removed from recylcerview
//                        Reminder reminder = noteHistoryList.remove(position);  //then remove item
                        // problem: when canceled the old reminder is not saved
                        // solution: add this reminder to the list when cancel is clicked
                        // differentiate between opening DatePicker from here as edit and from new reminder.
                        noteHistoryAdapter.notifyDataSetChanged();
                    }
                });

    }

    @Override
    protected void onResume() {
        noteHistoryAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
