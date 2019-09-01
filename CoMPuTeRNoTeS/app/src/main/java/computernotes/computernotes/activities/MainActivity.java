package computernotes.computernotes.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.Tag;
import computernotes.computernotes.activities.adapters.NoteItemAdapter;
import computernotes.computernotes.activities.adapters.NoteItemFirestoreAdapter;
import computernotes.computernotes.note.Note;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.notecontent.Paragraph;
import computernotes.computernotes.reminders.LocationReminder;
import computernotes.computernotes.reminders.Reminder;
import computernotes.computernotes.reminders.TimeReminder;
import computernotes.computernotes.reminders.UserReminder;
import computernotes.computernotes.users.CloudUser;
import computernotes.computernotes.users.LocalUser;
import computernotes.computernotes.utils.FireBaseNote;
import computernotes.computernotes.utils.MyApp;
import computernotes.computernotes.utils.notifications.NotificationHelper;


public class MainActivity extends AppCompatActivity {



    //all context in this project have the variable c.
    Context c = this;

    //a list to store all the products
    List<NoteMain> noteList;

    private LocationManager locationManager;
    private LocationListener listener;

    //the recyclerview
    RecyclerView recyclerViewNoteList;
    NoteItemFirestoreAdapter adapterFirestoreRecyclerViewNoteList;
//    NoteItemFirestoreAdapter adapterFirestoreRecyclerViewNoteList;

    //drawer menu
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    int RC_SIGN_IN = 100;
    Toolbar tb;
    Boolean drawerActive;
    Boolean botTBactive;

    Button noLogin;
    Button login;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    static boolean isOnline;
    public static boolean networkWorking;
    private boolean loggedIn;

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        isOnline = isNetworkAvailable();
//        skip this for now because it does not work. - tried to check if there can be a connection with google established.
//        new Online().run();
        if(isOnline )// && networkWorking)
            theme.applyStyle(R.style.Online, true);
        else
            theme.applyStyle(R.style.AppTheme, true);
        return theme;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noLogin = findViewById(R.id.no_login);
        login = findViewById(R.id.login);

        MyApp.getFirstInstance().registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        //  Setting ItemClickListener for Bottom Toolbar
        // Edited by Robert on the 10.03.2019 at 13:11 .
        tb = findViewById(R.id.bottom_toolbar);
        tb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bot_newnote:
                        newNote();
                        return true;
                    case R.id.bot_settings:
                        settings();
                        return true;
                    case R.id.bot_contact:
                        contactUs();
                        return true;
                    case R.id.bot_getLocation:
                        location();
                        return true;
                    case R.id.bot_signout:
                        signout();
                        return true;
                }

                return true;
            }
        });

        // making the list of notes -> should be called from the storage!
        ServerCommunicator.getInstance();
        noteList = ServerCommunicator.notes;
        mAuth = FirebaseAuth.getInstance();
        db = ServerCommunicator.db;
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            login();
        }
//        else loadNotes(firebaseUser.getUid());
        //Make Home button visible
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        drawerActive = ServerCommunicator.settingsTest.getDrawerMenuActive();
        botTBactive = ServerCommunicator.settingsTest.getBottomToolbarActive();

        if (drawerActive) {
            createDrawerMenu();
        }

        setUpRecyclerView();

        /*//getting the recyclerview from xml
        recyclerViewNoteList = findViewById(R.id.recyclerViewMain);
        recyclerViewNoteList.setHasFixedSize(true); // is probably wrong because it changes when notes get deleted.
        recyclerViewNoteList.setLayoutManager(new LinearLayoutManager(c));


        //creating recyclerview adapterFirestoreRecyclerViewNoteList
        adapterFirestoreRecyclerViewNoteList = new NoteItemAdapter(c, noteList);

        //setting adapterFirestoreRecyclerViewNoteList to recyclerview
        recyclerViewNoteList.setAdapter(adapterFirestoreRecyclerViewNoteList);*/
/*

        //swipe note
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //Remove swiped item from list and notify the RecyclerView
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left
                    if (ServerCommunicator.settingsTest.getLeftSwipeNoteString().equals("delete")) {
                        adapterFirestoreRecyclerViewNoteList.notifyItemRemoved(position); //item removed from recylcerview
                        noteList.remove(position);  //then remove item
                        db.collection("notes").document(noteList.get(position).getFirebaseID()).delete();
                    }
                    if (ServerCommunicator.settingsTest.getLeftSwipeNoteString().equals("add reminder")) {
                        DialogFragment newFragment = new MyDatePickerFragment(noteList.get(position), -1, c, "", "");
                        newFragment.show(getFragmentManager(), "date picker");
                    }


                }
                if (direction == ItemTouchHelper.RIGHT) {    //if swipe right
                    if (ServerCommunicator.settingsTest.getRightSwipeNoteString().equals("delete")) {
                        adapterFirestoreRecyclerViewNoteList.notifyItemRemoved(position); //item removed from recylcerview
                        noteList.remove(position);  //then remove item
                        db.collection("notes").document(noteList.get(position).getFirebaseID()).delete();
                    }
                    if (ServerCommunicator.settingsTest.getRightSwipeNoteString().equals("add reminder")) {
                        DialogFragment newFragment = new MyDatePickerFragment(noteList.get(position), -1, c, "", "");
                        newFragment.show(getFragmentManager(), "date picker");
                    }
                }

            }
        };

        //click on note
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerViewNoteList);

        ItemClickSupport.addTo(recyclerViewNoteList)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Log.d("note clicked", "at position" + position);
                        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                        intent.putExtra("note_index", position);
                        startActivity(intent);
                    }
                });
*/

        //@Michael was tut das hier? einmal inizieren?
        new NotificationHelper(c).initNotificationChannels();

        //test where we are
        location();

    }


    private void setUpRecyclerView() {
        Query query = db.collection("notes").orderBy("title", Query.Direction.DESCENDING).whereEqualTo("owner", firebaseUser.getUid());

        FirestoreRecyclerOptions<FireBaseNote> options = new FirestoreRecyclerOptions.Builder<FireBaseNote>()
                .setQuery(query, FireBaseNote.class)
                .build();

        adapterFirestoreRecyclerViewNoteList = new NoteItemFirestoreAdapter(options,c);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterFirestoreRecyclerViewNoteList);
    }

    // called after login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                loggedIn = true;
                // Successfully signed in
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Edited by chagai on the 4/5/2019 at 12:08 PM
//                            no need to check because set creates a document if the id doesn't already exist - and if it does we use merge.
//                            if merge proves to be useless we can still use this code or try to assign new users only when
//                            they register/sign in for the first time.
                            /*boolean userExists = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExists = document.getId().equals(firebaseUser.getUid());
                            }*/
//                            if(!userExists)  {
                            Map<String, Object> data = new HashMap<>();
                            db.collection("users").document(firebaseUser.getUid()).update(data);
                            ServerCommunicator.user = new CloudUser(firebaseUser.getUid());
                            ServerCommunicator.user.setNotes(noteList);
                            db.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful())
                                        ((CloudUser) ServerCommunicator.user).setUsername((String) task.getResult().get("username"));
                                }
                            });
//                            loadNotes(ServerCommunicator.user.getUid());

//                            }

                        }
                    }
                });
                // ...
            } else {
                // Sign in failed. If response is null the firebaseUser canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(loggedIn)
        adapterFirestoreRecyclerViewNoteList.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(loggedIn)
            adapterFirestoreRecyclerViewNoteList.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(loggedIn)
//        adapterFirestoreRecyclerViewNoteList.notifyDataSetChanged();
        super.onResume();
    }

    //could perhaps be deleted.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        Toolbar tb = findViewById(R.id.bottom_toolbar);
        Menu botMenu = tb.getMenu();
        getMenuInflater().inflate(R.menu.bot_toolbar, botMenu);
        botTBitems();
        if (botTBactive) {
            tb.setVisibility(View.VISIBLE);
            ScrollView sv = findViewById(R.id.scrollViewMain);
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) sv.getLayoutParams();
            params.setMargins(0, 0, 0, 150);
            sv.setLayoutParams(params);
        }
        if (!botTBactive) {
            tb.setVisibility(View.GONE);
            ScrollView sv = findViewById(R.id.scrollViewMain);
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) sv.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            sv.setLayoutParams(params);
        }
//      time Edited by chagai on the 3/30/2019 at 9:16 PM
//      testing search bar
        SearchView view = (SearchView) menu.findItem(R.id.action_search_main).getActionView();
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<NoteMain> searchList = new ArrayList<>();
                for (NoteMain noteMain :
                        noteList) {
//                    if(noteMain.getTitle().startsWith(s)) searchList.add(noteMain);
//                    if(noteMain.getTitle().contains(s)) searchList.add(noteMain);
                    boolean wordExists = false;
                    for (String w : noteMain.getTitle().split(" ")
                    ) {
//                        if (w.equals(s)) wordExists = true;
                        if (w.startsWith(s)) wordExists = true;
                    }
                    if (wordExists) searchList.add(noteMain);
                    boolean tagExists = false;
                    for (Tag t :
                            noteMain.getTags()) {
//                        if (t.getTag().startsWith(s)) tagExists = true;
                        if (t.getTag().equals(s)) tagExists = true;
                    }
                    if (tagExists) searchList.add(noteMain);
                }

                NoteItemAdapter adapterRecyclerViewNoteListSearch = new NoteItemAdapter(c, searchList);
                recyclerViewNoteList.setAdapter(adapterRecyclerViewNoteListSearch);
                return false;
            }
        });
        view.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerViewNoteList.setAdapter(adapterFirestoreRecyclerViewNoteList);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Drawer Toggle
        if (drawerActive) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }

        switch (item.getItemId()) {

            case R.id.home: //back top button
                NavUtils.navigateUpFromSameTask(this);//android.R.firebaseID.home might be needed
                return true;
            case R.id.action_search_main:
                return true;
            case R.id.action_add_user_notification:
                addUserNotification();
                return true;
            case R.id.action_settings:
                settings();
                return true;
            case R.id.action_new_note:
                newNote();
                return true;
            case R.id.action_contact:
                contactUs();
                return true;
            case R.id.signout:
                signout();
                return true;
            case R.id.offline:
                switchBetweenOnlineOffline();
                return true;
            case R.id.online:
                switchBetweenOnlineOffline();
                return true;
            case R.id.trash:
                showTrash();
                return true;
            case R.id.shared_with_me:
                showSharedWithMe();
                return true;
            case R.id.all_notes:
                getTheme();
                recreate();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //sync drawer button and state of drawer
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerActive) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerActive) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    //from here downwards help methods.

    private void login() {
        noLogin.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        noLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in firebaseUser's information
                                    ServerCommunicator.user = new LocalUser("test");
                                    ServerCommunicator.user.setNotes(noteList);
                                    db.disableNetwork();
                                }
                            }
                        });
                login.setVisibility(View.GONE);
                noLogin.setVisibility(View.GONE);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login.setVisibility(View.GONE);
                noLogin.setVisibility(View.GONE);
                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(), RC_SIGN_IN
                );
            }
        });

    }

    private void switchBetweenOnlineOffline() {
        if (ServerCommunicator.online)
            db.disableNetwork();//disable offline button and enable online button
        else
            db.enableNetwork();//disable online button and enable offline button
    }

    private void signout() {
        db.enableNetwork();
        ServerCommunicator.notes.clear();
        ServerCommunicator.getAllTags().clear();
        adapterFirestoreRecyclerViewNoteList.notifyDataSetChanged();
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        login();
                    }
                });
        loggedIn = false;
        noLogin.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
    }

    private void loadNotes(final String uid) {

        // TODO: differentiate between CloudUser and LocalUser then add initialising user here.(either with a method or with a field in user(be careful because the internet is off for anonymous user ))
        List<? extends UserInfo> providerData = firebaseUser.getProviderData();
        boolean isAnonymousUser = true;
        for (int i = 0; i < providerData.size(); i++) {
            UserInfo userInfo = providerData.get(i);
            System.out.println("testing provider " + i + ": " + userInfo.getProviderId());
            if (!userInfo.getProviderId().equals("firebase")) {
                ServerCommunicator.user = new CloudUser(firebaseUser.getUid());
                db.collection("users").document(firebaseUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful())
                                ((CloudUser) ServerCommunicator.user).setUsername((String) task.getResult().get("username"));
                            }
                        });
                isAnonymousUser = false;
            }
        }
        if (isAnonymousUser) ServerCommunicator.user = new LocalUser(firebaseUser.getUid());
        // Access a Cloud Firestore instance from your Activity

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("notes").whereEqualTo("owner", firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final NoteMain noteMain = new NoteMain(document.getString("title"));
                                noteMain.setFirebaseID(document.getId());
                                if (!noteList.contains(noteMain)) {
                                    FireBaseNote note = document.toObject(FireBaseNote.class);
                                    if (note.getOwner().equals(uid)) {
                                        final ArrayList<Tag> tags = new ArrayList<>();
                                        for (String s : note.getTags()
                                        ) {
                                            tags.add(new Tag(s));
                                        }
                                        noteMain.setTags(tags);
                                        noteMain.setImages(note.getImages());
                                        noteMain.getContentSections().add(0, new Paragraph(note.getContents().get(0)));
                                        db.collection("reminders")
                                                .whereEqualTo("noteID", noteMain.getFirebaseID())
                                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                        Map<String, Object> data = queryDocumentSnapshot.getData();
                                                        if (data.get("type").equals("time"))
                                                            noteMain.getReminders().add(new TimeReminder(((Timestamp) data.get("time")).toDate()));
                                                        if (data.get("type").equals("location")) {
                                                            Location location = new Location("");
                                                            location.setLatitude(((GeoPoint) data.get("location")).getLatitude());
                                                            location.setLongitude(((GeoPoint) data.get("location")).getLongitude());
                                                            noteMain.getReminders().add(new LocationReminder(location, Double.parseDouble((String) data.get("radius")), true, false));
                                                        }

                                                        if (data.get("type").equals("firebaseUser"))
                                                            noteMain.getReminders()
                                                                    .add(new UserReminder(Double.parseDouble((String) data.get("radius")), true, false, new CloudUser((String) data.get("firebaseUser")), false));
                                                    }
                                                } else
                                                    Snackbar.make(findViewById(R.id.activity_note), "query failed", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                                        if (noteMain.getTags().get(0).equals(new Tag("trash"))) {
                                            if (ServerCommunicator.user != null)
                                                ServerCommunicator.user.getTrash().add(noteMain);
                                        } else
                                            noteList.add(noteMain);
                                    }
                                }
                            }
                            adapterFirestoreRecyclerViewNoteList.notifyDataSetChanged();
//                            adapterFirestoreRecyclerViewNoteList = new NoteItemAdapter(c,notes);
                        } else {
                        }
                    }
                });

    }

    private void createDrawerMenu() {

        drawerLayout = findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(mDrawerToggle);
        //Drawer Menu Item Listener
        NavigationView navigationView = findViewById(R.id.drawerMenu);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {

                            case R.id.draw_location:
                                addUserNotification();
                                return true;
                            case R.id.draw_setting:
                                settings();
                                return true;
                            case R.id.draw_new_note:
                                newNote();
                                return true;
                            case R.id.draw_contact:
                                contactUs();
                                return true;
                            case R.id.draw_signout:
                                signout();
                                return true;
                        }
                        // should be super. something.
                        return true;
                    }
                });
        if (!ServerCommunicator.settingsTest.getDrawerMenuActive()) {
            navigationView.setVisibility(View.GONE);
        }
        // Visibility of Drawer MenuItems
        Menu navMenu = navigationView.getMenu();
        ArrayList<String> prios = ServerCommunicator.settingsTest.getDrawerPrios();
        if (prios.contains("new note")) {
            navMenu.findItem(R.id.draw_new_note).setVisible(true);
        } else {
            navMenu.findItem(R.id.draw_new_note).setVisible(false);
        }
        if (prios.contains("settings")) {
            navMenu.findItem(R.id.draw_setting).setVisible(true);
        } else {
            navMenu.findItem(R.id.draw_setting).setVisible(false);
        }
        if (prios.contains("location")) {
            navMenu.findItem(R.id.draw_location).setVisible(true);
        } else {
            navMenu.findItem(R.id.draw_location).setVisible(false);
        }
        if (prios.contains("contact")) {
            navMenu.findItem(R.id.draw_contact).setVisible(true);
        } else {
            navMenu.findItem(R.id.draw_contact).setVisible(false);
        }
        if (prios.contains("signout")) {
            navMenu.findItem(R.id.draw_signout).setVisible(true);
        } else {
            navMenu.findItem(R.id.draw_signout).setVisible(false);
        }

    }

    private void location() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Map<String, Object> data = new HashMap<>();
                data.put("location", new GeoPoint(location.getLatitude(), location.getLongitude()));
                db.collection("users").document(firebaseUser.getUid()).set(data);
                for (Note noteMain :
                        noteList) {
                    for (Reminder reminder :
                            noteMain.getReminders()) {
                        if (reminder instanceof LocationReminder) {
                            float distanceInMeters = ((LocationReminder) reminder).getLocation().distanceTo(location);
                            if (((LocationReminder) reminder).getRadius() > (double) distanceInMeters) {
                                System.out.println("\n " + location.getLongitude() + " " + location.getLatitude());
                                Toast.makeText(MainActivity.this, "\n " + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                            }

                        }
                        if (reminder instanceof UserReminder) {
                            final Location locationCurrentUser = location;
                            final double radiusCurrentUser = ((UserReminder) reminder).getRadius();
                            db.collection("users").document(((UserReminder) reminder).getCloudUser().getUid())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists()) {
                                            GeoPoint geoPoint = (GeoPoint) documentSnapshot.getData().get("location");
                                            Location userLocation = new Location("");
                                            userLocation.setLatitude(geoPoint.getLatitude());
                                            userLocation.setLongitude(geoPoint.getLongitude());
                                            float distanceInMeters = userLocation.distanceTo(locationCurrentUser);
                                            if (radiusCurrentUser > (double) distanceInMeters)
                                                Toast.makeText(MainActivity.this, "worked", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        // grant location permission when entering the app -> change to only show the first time or when location is necessary or used.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 10);
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the firebaseUser grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
        }
    }

    private void addUserNotification() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 500, 1, listener);

    }

    private void contactUs() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("WhatsApp contact");
        alert.setMessage("press ok to be redirected to WhatsApp");
// Create TextView
        final TextView input = new TextView(c);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Uri uriUrl = Uri.parse("https://api.whatsapp.com/send?phone=491636238088&text=my%20name%20is%20_writeyournamehere_%20.%20nice%20to%20meet%20you%20chagai%20&source=&data=");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    private void newNote() {
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra("note_index", -1);
        startActivity(intent);
    }

    private void settings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void botTBitems() {
        ArrayList<String> prios = ServerCommunicator.settingsTest.getBotTBprios();
        ActionMenuItemView botNewNote = findViewById(R.id.bot_newnote);
        ActionMenuItemView botSettings = findViewById(R.id.bot_settings);
        ActionMenuItemView botContact = findViewById(R.id.bot_contact);
        ActionMenuItemView botGetLocation = findViewById(R.id.bot_getLocation);
        ActionMenuItemView botSignout = findViewById(R.id.bot_signout);


        if (prios.contains("settings")) {
            botSettings.setVisibility(View.VISIBLE);
        } else {
            botSettings.setVisibility(View.GONE);
        }
        if (prios.contains("new note")) {
            botNewNote.setVisibility(View.VISIBLE);
        } else {
            botNewNote.setVisibility(View.GONE);
        }
        if (prios.contains("contact")) {
            botContact.setVisibility(View.VISIBLE);
        } else {
            botContact.setVisibility(View.GONE);
        }
        if (prios.contains("location")) {
            botGetLocation.setVisibility(View.VISIBLE);
        } else {
            botGetLocation.setVisibility(View.GONE);
        }
        if (prios.contains("signout")) {
            botSignout.setVisibility(View.VISIBLE);
        } else {
            botSignout.setVisibility(View.GONE);
        }
    }

    private void setAdapterToAllNotes() {
        recyclerViewNoteList.setAdapter(adapterFirestoreRecyclerViewNoteList);
    }

    private void showSharedWithMe() {
        db.collection("notes").whereArrayContains("sharedWith", firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<NoteMain> sharedWithMe = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        final NoteMain noteMain = new NoteMain(document.getString("title"));
                        noteMain.setFirebaseID(document.getId());
                        if (!sharedWithMe.contains(noteMain)) {
                            FireBaseNote note = document.toObject(FireBaseNote.class);
                            final ArrayList<Tag> tags = new ArrayList<>();
                            for (String s : note.getTags()
                            ) {
                                tags.add(new Tag(s));
                            }
                            noteMain.setTags(tags);
                            noteMain.setImages(note.getImages());
                            noteMain.getContentSections().add(0, new Paragraph(note.getContents().get(0)));
                            db.collection("reminders")
                                    .whereEqualTo("noteID", noteMain.getFirebaseID())
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                            Map<String, Object> data = queryDocumentSnapshot.getData();
                                            if (data.get("type").equals("time"))
                                                noteMain.getReminders().add(new TimeReminder(((Timestamp) data.get("time")).toDate()));
                                            if (data.get("type").equals("location")) {
                                                Location location = new Location("");
                                                location.setLatitude(((GeoPoint) data.get("location")).getLatitude());
                                                location.setLongitude(((GeoPoint) data.get("location")).getLongitude());
                                                noteMain.getReminders().add(new LocationReminder(location, Double.parseDouble((String) data.get("radius")), true, false));
                                            }

                                            if (data.get("type").equals("firebaseUser"))
                                                noteMain.getReminders()
                                                        .add(new UserReminder(Double.parseDouble((String) data.get("radius")), true, false, new CloudUser((String) data.get("firebaseUser")), false));
                                        }
                                    } else
                                        Snackbar.make(findViewById(R.id.activity_note), "query failed", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            sharedWithMe.add(noteMain);
                        }
                    }
                    NoteItemAdapter adapterRecyclerViewNoteListSharedWithMe = new NoteItemAdapter(c, sharedWithMe);
                    recyclerViewNoteList.setAdapter(adapterRecyclerViewNoteListSharedWithMe);
                }
            }
        });
    }

    private void showTrash() {
        NoteItemAdapter adapterRecyclerViewNoteListSearch = new NoteItemAdapter(c, ServerCommunicator.user.getTrash());
        recyclerViewNoteList.setAdapter(adapterRecyclerViewNoteListSearch);
    }

}
