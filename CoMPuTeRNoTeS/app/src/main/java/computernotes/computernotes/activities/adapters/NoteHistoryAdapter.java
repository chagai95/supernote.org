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
import computernotes.computernotes.note.Note;
import computernotes.computernotes.note.NoteVersion;
import computernotes.computernotes.notecontent.Paragraph;

public class NoteHistoryAdapter extends RecyclerView.Adapter<NoteHistoryAdapter.NoteViewHolder> {


    //this context we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the reminders in a list
    private List<NoteVersion> noteHistoryList;

    //getting the context and reminder list with constructor
    public NoteHistoryAdapter(Context cOtherActivity, List<NoteVersion> noteHistoryList) {
        this.cOtherActivity = cOtherActivity;
        this.noteHistoryList = noteHistoryList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.note_history_item, null);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            holder.paraNoteHistory.setText(((Paragraph) noteHistoryList.get(position).getContentSections().get(0)).getParaString());
    }


    @Override
    public int getItemCount() {
        return noteHistoryList.size();
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView paraNoteHistory;

        NoteViewHolder(View itemView) {
            super(itemView);

            paraNoteHistory = itemView.findViewById(R.id.paraNoteHistory);
        }
    }
}
