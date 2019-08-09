package com.example.firebaseui_firestoreexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TitleHistoryAdapter extends RecyclerView.Adapter<TitleHistoryAdapter.TitleHistoryHolder> {


    //this context we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the Strings in a list
    private List<String> noteHistoryList;

    //getting the context and String list with constructor
    TitleHistoryAdapter(Context cOtherActivity, List<String> noteHistoryList) {
        this.cOtherActivity = cOtherActivity;
        this.noteHistoryList = noteHistoryList;
    }

    @NonNull
    @Override
    public TitleHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.note_title_item, null);
        return new TitleHistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TitleHistoryHolder holder, int position) {
            holder.titleTextView.setText(noteHistoryList.get(position));
    }


    @Override
    public int getItemCount() {
        return noteHistoryList.size();
    }


    class TitleHistoryHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;

        TitleHistoryHolder(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.note_history_text_view_title);
        }
    }
}

