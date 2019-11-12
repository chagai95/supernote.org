package com.example.firebaseui_firestoreexample.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseui_firestoreexample.CloudUser;
import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.adapters.FriendAdapter;
import com.example.firebaseui_firestoreexample.activities.adapters.ReminderAdapter;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.utils.RecyclerItemClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FriendsActivity extends MyActivity {

    List<CloudUserData> friendsList;
    Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setTitle("Friends");
        initializeFriendsList();
        swipeToRefresh();
        setUpRecyclerView();
    }

    private void initializeFriendsList() {
        friendsList = new LinkedList<>();
        friendsList.addAll(MyApp.friends.values());
    }

    private void setUpRecyclerView() {
        FriendAdapter adapter = new FriendAdapter(c, friendsList);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_friends);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    CloudUser cloudUserBeingRemoved = friendsList.remove(viewHolder.getAdapterPosition()).getCloudUser();
                    DocumentReference documentReference = MyApp.myCloudUserData.getDocumentReference();
                    documentReference.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            CloudUser myCloudUser = documentSnapshot.toObject(CloudUser.class);
                            assert myCloudUser != null;
                            ArrayList<String> friends = myCloudUser.getFriends();
                            friends.remove(cloudUserBeingRemoved.getUid());
                            documentReference.update("friends", friends).addOnSuccessListener(command -> Toast.makeText(c, "removed user: " + cloudUserBeingRemoved.getUsername() + " from friends list", Toast.LENGTH_SHORT).show());
                            MyApp.friends.remove(cloudUserBeingRemoved.getUid());
                        }

                    });
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

    private void swipeToRefresh() {
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshFriendsActivity);
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
