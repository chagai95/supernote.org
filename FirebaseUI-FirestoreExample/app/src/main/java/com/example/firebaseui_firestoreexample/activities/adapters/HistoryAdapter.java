package com.example.firebaseui_firestoreexample.activities.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseui_firestoreexample.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {


    //this activity we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the Strings in a list
    private List<String> noteHistoryList;

    //getting the activity and String list with constructor
    public HistoryAdapter(Context cOtherActivity, List<String> noteHistoryList) {
        this.cOtherActivity = cOtherActivity;
        this.noteHistoryList = noteHistoryList;
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.note_history_item, null);
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        holder.titleTextView.setText(noteHistoryList.get(position));
    }


    @Override
    public int getItemCount() {
        return noteHistoryList.size();
    }


    class HistoryHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;

        HistoryHolder(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.note_history_text_view_description);
        }
    }
}

