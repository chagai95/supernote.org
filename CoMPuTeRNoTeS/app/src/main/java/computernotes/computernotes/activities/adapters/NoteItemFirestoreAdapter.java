package computernotes.computernotes.activities.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import computernotes.computernotes.R;
import computernotes.computernotes.activities.MainActivity;
import computernotes.computernotes.utils.FireBaseNote;

public class NoteItemFirestoreAdapter extends FirestoreRecyclerAdapter<FireBaseNote, NoteItemFirestoreAdapter.NoteViewFirestoreHolder> {


    /**
     * this context we will use to inflate the layout
     * this context comes from a different activity (for example {@link MainActivity})
     */

    //we are storing all the notes in a list

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public NoteItemFirestoreAdapter(@NonNull FirestoreRecyclerOptions<FireBaseNote> options) {
        super(options);
    }




    @Override
    protected void onBindViewHolder(@NonNull NoteViewFirestoreHolder holder, int notePosition, @NonNull FireBaseNote fireBaseNote) {
        //getting the note of the specified position

        //binding the data with the ViewHolder views
        holder.textViewTitle.setText(fireBaseNote.getTitle());
        if ((fireBaseNote.getContents().get(0)).equals(""))
            holder.textViewShortDesc.setText(fireBaseNote.getTitle());
        else {
                holder.textViewShortDesc.setText(( fireBaseNote.getContents().get(0)));

        }
    }


    @NonNull
    @Override
    public NoteViewFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,
                parent, false);
        return new NoteViewFirestoreHolder(v);
    }

        class NoteViewFirestoreHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        //decide what comes as a short description preview.
        TextView textViewShortDesc;
        //decide how and if other NoteContent in a note should be shown in the preview

        NoteViewFirestoreHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
        }
    }
}
