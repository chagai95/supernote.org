package computernotes.computernotes.activities.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import computernotes.computernotes.R;
import computernotes.computernotes.activities.MainActivity;
import computernotes.computernotes.note.Note;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.notecontent.NoteContent;
import computernotes.computernotes.notecontent.Paragraph;

public class NoteItemAdapter extends RecyclerView.Adapter<NoteItemAdapter.NoteViewHolder> {


    /**
     * this context we will use to inflate the layout
     * this context comes from a different activity (for example {@link MainActivity})
     */
    private Context cOtherActivity;

    //we are storing all the notes in a list
    private List<NoteMain> noteList;

    //getting the context and note list with constructor
    public NoteItemAdapter(Context cOtherActivity, List<NoteMain> noteList) {
        this.cOtherActivity = cOtherActivity;
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);

        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.note_item, null);
        LinearLayout layout = view.findViewById(R.id.note_item);
        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int notePosition) {
        //getting the note of the specified position
        Note note = noteList.get(notePosition);

        //binding the data with the ViewHolder views
        holder.textViewTitle.setText(note.getTitle());
        if (((Paragraph) note.getContentSections().get(0)).getParaString().equals(""))
            holder.textViewShortDesc.setText(note.getTitle());
        else {

            if (note.getContentSections().get(0) instanceof Paragraph) {
                holder.textViewShortDesc.setText(((Paragraph) note.getContentSections().get(0)).getParaString());
            }
        }
    }


    @Override
    public int getItemCount() {
        return noteList.size();
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        //decide what comes as a short description preview.
        TextView textViewShortDesc;
        //decide how and if other NoteContent in a note should be shown in the preview

        NoteViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
        }
    }
}
