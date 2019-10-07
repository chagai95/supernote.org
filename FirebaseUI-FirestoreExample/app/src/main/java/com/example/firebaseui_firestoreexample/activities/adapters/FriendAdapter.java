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
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendHolder> {


    //this activity we will use to inflate the layout
    private Context cOtherActivity;

    //we are storing all the Strings in a list
    private List<CloudUserData> friendsList;

    //getting the activity and String list with constructor
    public FriendAdapter(Context cOtherActivity, List<CloudUserData> friendsList) {
        this.cOtherActivity = cOtherActivity;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(cOtherActivity);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.friend_item, null);
        return new FriendHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendHolder holder, int position) {
        holder.friendUsername.setText(friendsList.get(position).getCloudUser().getUsername());
    }


    @Override
    public int getItemCount() {
        return friendsList.size();
    }


    class FriendHolder extends RecyclerView.ViewHolder {

        TextView friendUsername;

        FriendHolder(View itemView) {
            super(itemView);

            friendUsername = itemView.findViewById(R.id.friendUsername);
        }
    }
}

