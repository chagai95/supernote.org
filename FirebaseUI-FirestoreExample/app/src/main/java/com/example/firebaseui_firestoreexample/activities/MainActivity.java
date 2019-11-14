package com.example.firebaseui_firestoreexample.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.Note;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.activities.adapters.NoteAdapter;
import com.example.firebaseui_firestoreexample.activities.adapters.SearchAdapter;
import com.example.firebaseui_firestoreexample.firestore_data.NoteData;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.utils.InternetThread;
import com.example.firebaseui_firestoreexample.utils.MyActivityLifecycleCallbacks;
import com.example.firebaseui_firestoreexample.utils.MyDatePickerFragment;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.NotificationHelper;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.example.firebaseui_firestoreexample.MyApp.*;

public class MainActivity extends MyActivity {

//    Intent mServiceIntent;
//    private SensorService mSensorService;

    SearchView searchView;
    CoordinatorLayout coordinatorLayout;

    RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NoteAdapter adapterAllNotes;
    private NoteAdapter adapterShared;
    private NoteAdapter adapterTrash;
    FirebaseUser firebaseUser;

    boolean onCreateCalled;
    InternetThread internetThread;
    private TrafficLight lastTrafficLightState;

    private Context c = this;
    private String searchText = "";
    String[] filterOptions;
    boolean[] checkedItemsFilterOptions;
    String[] sortOptions;
    private int sort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApp.getFirstInstance().registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayoutMainActivity);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            login();
        } else {

            disablingInternetIfAnonymousUser();

            swipeToRefresh();

//        code from the article:
//        mSensorService = new SensorService(this);
//        changed the code a bit should work fine
//        mServiceIntent = new Intent(this, SensorService.class);
//        if (!isMyServiceRunning()) {
//            startService(mServiceIntent);
//        }


            startAppOffline();

            newNote();

            setUpRecyclerView();

            onCreateCalled = true;

//            createCacheLoaderTimerTask();

            new NotificationHelper(this).initNotificationChannels();

//            for testing internet speed.
//            mDecimalFormatter = new DecimalFormat("##.##");

            reception();

            setFilterAndSortOptions();

            locationPermission();

        }
    }

    private void setFilterAndSortOptions() {
        sortOptions = new String[]{"Note Title", "Date Created"};
        sort = 0; // note title
        filterOptions = new String[]{"note title", "note body", "shared users"};
        checkedItemsFilterOptions = new boolean[filterOptions.length];
        for (int i = 0; i < checkedItemsFilterOptions.length; i++) {
            checkedItemsFilterOptions[i] = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpSearchView(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.action_search_all_notes).getActionView();

        searchView.setOnSearchClickListener(v -> showSortAndFilterToolbar());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String s) {
                recyclerViewSearch(s);
                searchText = s;
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            setUpRecyclerView();
            removeSortAndFilterToolbar();
            recreate();// do not understand why this is necessary

            return false;
        });
    }


    private void locationPermission() {

//        perhaps better to call in login activity! trying to call at the end of onCreate to avoid unexpected changes whilst waiting for user to accept.
//        first check for permissions
//        overriding onRequestPermissionsResult and calling the creating telephony listener there if permission is given.
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_NETWORK_STATE}
                        , 10);
            }
        } else
            setTelephonyListener();

    }

    //    tried in on create
    public void setTelephonyListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        assert telephonyManager != null;
        telephonyManager.listen(new PhoneStateListener() {

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                if (NetworkUtil.getConnectivityStatusString(c) == NetworkUtil.NETWORK_STATUS_MOBILE &&
                        NetworkUtil.connectionIsFast != NetworkUtil.fastConnection(telephonyManager.getNetworkType())) {
                    recreate();
                    NetworkUtil.connectionIsFast = NetworkUtil.fastConnection(telephonyManager.getNetworkType());
                }
            }

            /*@RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                super.onCellInfoChanged(cellInfo);
                if (c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                Toast.makeText(c, "cellInfo changed", Toast.LENGTH_SHORT).show();
                cellInfo = telephonyManager.getAllCellInfo();
                boolean oneSimIsAlreadyRegistered = false;
                for (CellInfo ci : cellInfo) {
                    if (ci.isRegistered() && !oneSimIsAlreadyRegistered) {
                        oneSimIsAlreadyRegistered = true;
                        if (ci instanceof CellInfoGsm && !(NetworkUtil.lastRegisteredCellInfo instanceof CellInfoGsm)) {
                            Toast.makeText(c, "changed to LTE(4G) from something else", Toast.LENGTH_LONG).show();
                            NetworkUtil.lastRegisteredCellInfo = ci;
                            recreate();
                        }
                        if (ci instanceof CellInfoLte && !(NetworkUtil.lastRegisteredCellInfo instanceof CellInfoLte)) {
                            Toast.makeText(c, "changed to Gsm (2G?) from something else", Toast.LENGTH_LONG).show();
                            NetworkUtil.lastRegisteredCellInfo = ci;
                            recreate();
                        }
                        if (ci instanceof CellInfoCdma && !(NetworkUtil.lastRegisteredCellInfo instanceof CellInfoCdma)) {
                            Toast.makeText(c, "changed to Cdma from something else", Toast.LENGTH_LONG).show();
                            NetworkUtil.lastRegisteredCellInfo = ci;
                            recreate();
                        }
                        if (ci instanceof CellInfoWcdma && !(NetworkUtil.lastRegisteredCellInfo instanceof CellInfoWcdma)) {
                            Toast.makeText(c, "changed to wcdma from something else", Toast.LENGTH_LONG).show();
                            NetworkUtil.lastRegisteredCellInfo = ci;
                            recreate();
                        }
                    }
                }

            }
*/

        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//        PhoneStateListener.LISTEN_DATA_ACTIVITY
    }


    private void newNote() {
        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, EditNoteActivity.class).putExtra("newNote", true)));
    }

    private void startAppOffline() {
        //                probably better to use 2 different booleans one only for the anonymous user a bit repetitive but probably better.
        // I deleted some stuff accidentally so this might not be working properly if at all
        // move this method to MyApp
        if (!MyApp.appStarted)
            db.collection("utils").document("startAppOffline")
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData()).get("startAppOffline")) {
                        db.disableNetwork();
                        MyApp.internetDisabledInternally = true;
                        MyApp.appStarted = true;
                        recreate();
                    }
                }
            });
    }

    private void swipeToRefresh() {
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
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


    private void disablingInternetIfAnonymousUser() {
//            recreating here because the internet state has changed. using MyApp.userSkippedLogin so it only does this once.
        if (firebaseUser.isAnonymous() && !MyApp.userSkippedLogin) {
            MyApp.userSkippedLogin = true;
            disableInternalInternet();
            recreate();
        }
    }


    private void login() {
        db.enableNetwork();
        MyApp.internetDisabledInternally = false;
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


    @SuppressWarnings("unused")
    private void createCacheLoaderTimerTask() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 11);
        today.set(Calendar.MINUTE, 12);
        today.set(Calendar.SECOND, 0);

// every night at 2am you run your task
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                if (isNetworkAvailable())
                    MyApp.loadToCache();
                else
                    MyApp.setBackUpFailed(true);
            }
        };
        timer.schedule(timerTask, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // period: 1 day
    }

    //could perhaps be deleted.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setTelephonyListener();
            }
        }
    }

//    private boolean isMyServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
//            if (SensorService.class.getName().equals(service.service.getClassName())) {
//                Log.i ("isMyServiceRunning?", true+"");
//                return true;
//            }
//        }
//        Log.i ("isMyServiceRunning?", false+"");
//        return false;
//    }

    @Override
    protected void onDestroy() {
//        if(getIntent().getBooleanExtra("networkChangeReciever",false))
//            stopService(mServiceIntent);
//        getIntent().putExtra("networkChangeReciever",false);
//        Log.i("MAINACT", "onDestroy!");
//        FirebaseFirestore.getInstance().collection("utils").document("mainActivityDestroyed").update(
//                "mainActivityDestroyed", FieldValue.arrayUnion(((Activity) this).toString().substring(52)));
        super.onDestroy();
    }


    private void setUpRecyclerView() {
        hideKeyboard();

        switch (MyApp.recyclerViewMode) {
            case "trash":
                setTitle("Trash");
                setUpRecyclerViewWithQuery(queryTrashedNotes);
                break;
            case "shared":
                setTitle("Share");
                setUpRecyclerViewWithQuery(querySharedNotes);
                break;
            default:
                setTitle("All Notes");
                // on the first run of the app this is quicker than our initialisation in MyApp so we initialise here.
                queryNotTrashedNotes = db.collection("notes").whereEqualTo("creator", userUid).whereEqualTo("trash", false);
                setUpRecyclerViewWithQuery(queryNotTrashedNotes);
        }
    }

    private void setUpRecyclerViewWithQuery(Query query) {

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        adapter = new NoteAdapter(options);
        getIntent().putExtra("startAppAndCloseMainActivity", false);
        if (recyclerView == null) {
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                setSwiping(viewHolder, direction);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            String id = documentSnapshot.getId();
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("noteID", id);
            MainActivity.this.startActivity(intent);

        });
    }

    private void setSwiping(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            if (MyApp.recyclerViewMode.equals("trash")) {
                Toast.makeText(c, "note restored from trash", Toast.LENGTH_SHORT).show();
                adapter.untrashItem(viewHolder.getAdapterPosition());
            } else {
                createUndoSnackbar(adapter.trashItem(viewHolder.getAdapterPosition()));
            }
        }
        if (direction == ItemTouchHelper.LEFT) {
            if (swipeLeftToPermanentlyDelete) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
                Toast.makeText(c, "deleted", Toast.LENGTH_SHORT).show();
            } else if (MyApp.recyclerViewMode.equals("trash")) {
                Toast.makeText(c, "note restored from trash", Toast.LENGTH_SHORT).show();
                adapter.untrashItem(viewHolder.getAdapterPosition());
            } else {
                createUndoSnackbar(adapter.trashItem(viewHolder.getAdapterPosition()));
            }
        }
    }

    private void createUndoSnackbar(DocumentReference documentReference) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Note is trashed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    undoTrashing(documentReference);
                    Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Note is restored!", Snackbar.LENGTH_SHORT);
                    snackbar1.show();
                });
        snackbar.show();
    }

    private void undoTrashing(DocumentReference documentReference) {
        documentReference.collection("Reminders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot :
                        Objects.requireNonNull(task.getResult()).getDocuments()) {
                    documentSnapshot.getReference().update("trash", false);
                }
            }
        });
        documentReference.update("trash", false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recyclerViewSearch(String searchText) {
//        Query query = db.collection("notes").whereEqualTo("creator", firebaseUser.getUid()).whereEqualTo("trash", true);  change this to have the search query.
//        setUpRecyclerViewWithQuery(query);
//        work around:

        List<NoteData> noteList = new ArrayList<>();

//        note title
        if (checkedItemsFilterOptions[0])
            for (NoteData noteData :
                    MyApp.allNotes.values()) {
                if (noteData.getNote().getTitle().contains(searchText))
                    if (!noteList.contains(noteData))
                        noteList.add(noteData);
            }
//        note body
        if (checkedItemsFilterOptions[1])
            for (NoteData noteData :
                    MyApp.allNotes.values()) {
                if (noteData.getNote().getDescription().contains(searchText))
                    if (!noteList.contains(noteData))
                        noteList.add(noteData);
            }

        if (searchText.equals("missed reminders")) {
            noteList.clear();
            for (TimeReminderData timeReminderData :
                    timeReminders.values()) {
                if (timeReminderData.getTimeReminder().getTimestamp().toDate().before(new Date())
                        && !timeReminderData.getTimeReminder().isDone()) {
                    CollectionReference coll = timeReminderData.getDocumentReference().getParent();
                    DocumentReference documentReferenceNote = coll.getParent();
                    assert documentReferenceNote != null;
                    NoteData noteData = allNotes.get(documentReferenceNote.getId());
                    if (!noteList.contains(noteData))
                        noteList.add(noteData);
                }

            }
        }

        sortSearch(noteList);

        SearchAdapter searchAdapter = new SearchAdapter(c, noteList);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                setSwiping(viewHolder, direction);
            }
        }).attachToRecyclerView(recyclerView);

        searchAdapter.setOnItemClickListener((documentSnapshot, position) -> {
            String id = documentSnapshot.getId();
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("noteID", id);
            MainActivity.this.startActivity(intent);

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortSearch(List<NoteData> noteList) {
        switch (sortOptions[sort]) {
            case "Date Created":
                noteList.sort((o1, o2) ->
                        o1.getNote().getCreated().toDate().compareTo(o2.getNote().getCreated().toDate()));
                break;
            case "Note Title":
                noteList.sort((o1, o2) ->
                        o1.getNote().getTitle().toLowerCase().compareTo(o2.getNote().getTitle().toLowerCase()));
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            MyApp.activityMainStopped();
            adapter.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.activityMainResumed();
        if (!onCreateCalled && MyApp.currentTrafficLightState != lastTrafficLightState)
            recreate();
    }

    @Override
    public Resources.Theme getTheme() {
        super.setLastTrafficLightState(lastTrafficLightState);
        Resources.Theme theme = super.getTrafficLightTheme();
        lastTrafficLightState = super.getLastTrafficLightState();
        return theme;
    }

    boolean isNetworkAvailable() {
        return super.isNetworkAvailable();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        setUpSearchView(menu);

        if (MyApp.userSkippedLogin)
            setMenuForUserSkippedLogin(menu);

        MenuItem logoutMenuItem = menu.findItem(R.id.logout);
        logoutMenuItem.setVisible(!MyApp.userSkippedLogin);

        MenuItem loginMenuItem = menu.findItem(R.id.login);
        loginMenuItem.setVisible(MyApp.userSkippedLogin);

        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle);


        if (MyApp.internetDisabledInternally)
            appInternInternetOffToggleMenuItem.setTitle("go online");
        else
            appInternInternetOffToggleMenuItem.setTitle("go offline");

//        consider creating a method for all internet changes aside from the traffic light to handle all of the cases
//        which are not connected to the data but still require internet even if the internet is off internally.
        return super.onCreateOptionsMenu(menu);
    }

    private void setMenuForUserSkippedLogin(Menu menu) {
        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle);
        appInternInternetOffToggleMenuItem.setVisible(false);
        MenuItem sharedMenuItem = menu.findItem(R.id.shared_notes);
        sharedMenuItem.setVisible(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.speed_test:
                Toast.makeText(this, "last speed recorded: " + MyApp.totalTime / 1000, Toast.LENGTH_SHORT).show();
                reception();
                return true;*/
            case R.id.app_intern_internet_toggle:
                if (MyApp.internetDisabledInternally)
                    db.enableNetwork();
                else db.disableNetwork();
                MyApp.internetDisabledInternally = !MyApp.internetDisabledInternally;
                recreate();
                return true;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.login:
//                deal here with the anonymous user merge with the new user
                login();
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.trash:
                MyApp.recyclerViewMode = "trash";
                setTitle("Trash");
                recreate();
                return true;
            case R.id.shared_notes:
                MyApp.recyclerViewMode = "shared";
                setTitle("Share");
                recreate();
                return true;
            case R.id.allNotes:
                MyApp.recyclerViewMode = "allNotes";
                setTitle("All Notes");
                recreate();
                return true;
            case R.id.missed_reminders:
                showNotesWithMissedReminders();
                return true;
            case R.id.report_bug:
                reportBug();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showNotesWithMissedReminders() {
        recyclerViewSearch("missed reminders");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showSortAndFilterToolbar() {
        Toolbar toolbar = findViewById(R.id.sort_and_filter_toolbar);
        toolbar.setVisibility(View.VISIBLE);
        getMenuInflater().inflate(R.menu.bot_toolbar, toolbar.getMenu());
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.filter:
                    createFilterDialog();
                    return true;
                case R.id.sort:
                    createSortDialog();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        });

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createSortDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setTitle("Sort Search");

        alert.setSingleChoiceItems(sortOptions, sort,
                (dialog, which) -> sort = which);


        alert.setPositiveButton("search", (dialog, whichButton) -> recyclerViewSearch(searchText));

        alert.setNegativeButton("cancel", (dialog, whichButton) -> {
            //cancel
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createFilterDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setTitle("Filter Search");


        alert.setMultiChoiceItems(filterOptions, checkedItemsFilterOptions,
                (dialog, which, isChecked) -> checkedItemsFilterOptions[which] = isChecked);


        alert.setPositiveButton("search", (dialog, whichButton) -> recyclerViewSearch(searchText));

        alert.setNegativeButton("cancel", (dialog, whichButton) -> {
            //cancel
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private void removeSortAndFilterToolbar() {
        Toolbar view = findViewById(R.id.sort_and_filter_toolbar);
        view.setVisibility(View.GONE);
        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setVisibility(View.VISIBLE);
    }

    private void logout() {
        db.enableNetwork();
        MyApp.logout();
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> login());
    }

    private void reportBug() {
        if (isNetworkAvailable()) {
            Uri uriUrl = Uri.parse("https://api.whatsapp.com/send?phone=4915905872952&text=my%20name%20is%20_writeyournamehere_%20.%20nice%20to%20meet%20you%20chagai.&source=&data=");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else addWhatsappReminderToReportBug();
    }

    private void addWhatsappReminderToReportBug() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);


        // Add another TextView here for the message label
        final EditText messageEditText = new EditText(c);
        messageEditText.setHint("describe your problem here");
        layout.addView(messageEditText); // Another add method

        alert.setTitle("report bug via whatsapp reminder");
        alert.setMessage("no internet - set the time to when you will have internet connection");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("set time", (dialog, whichButton) -> {
            Log.i("AlertDialog", "TextEntry 2 Entered " + messageEditText.getText().toString());
            showDatePicker(messageEditText.getText().toString());
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        AlertDialog alertDialog = alert.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void showDatePicker(String message) {
        TimeReminder timeReminder = new TimeReminder();
        timeReminder.setWhatsappMessage(message);
        DialogFragment newFragment = new MyDatePickerFragment(null, c, timeReminder);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }


    private void reception() {
        if (isNetworkAvailable()) {
            internetThread = new InternetThread(this);
            internetThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            internetThread.start();
        }
    }
/*
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_STATUS:
                    final SpeedInfo info1 = (SpeedInfo) msg.obj;
                    mTxtSpeed.setText(String.format("0x7f040004", mDecimalFormatter.format(info1.kilobits)));
                    // Title progress is in range 0..10000
                    setProgress(100 * msg.arg1);
                    mTxtProgress.setText(String.format("0x7f040005", msg.arg2, EXPECTED_SIZE_IN_BYTES));
                    break;
                case MSG_UPDATE_CONNECTION_TIME:
                    mTxtConnectionSpeed.setText(String.format("0x7f040006", msg.arg1));
                    break;
                case MSG_COMPLETE_STATUS:
                    final SpeedInfo info2 = (SpeedInfo) msg.obj;
                    mTxtSpeed.setText(String.format("0x7f040007", msg.arg1, info2.kilobits));

                    mTxtProgress.setText(String.format("0x7f040005", msg.arg1, EXPECTED_SIZE_IN_BYTES));

                    if (networkType(info2.kilobits) == 1) {
                        mTxtNetwork.setText("0x7f040002");
                    } else {
                        mTxtNetwork.setText("0x7f040001");
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    *//*
      Our Slave worker that does actually all the work
     *//*
    private final Runnable mWorker = new Runnable() {

        @Override
        public void run() {
            InputStream stream = null;
            try {
                int bytesIn = 0;
                String downloadFileUrl = "http://www.gregbugaj.com/wp-content/uploads/2009/03/dummy.txt";
                long startCon = System.currentTimeMillis();
                URL url = new URL(downloadFileUrl);
                URLConnection con = url.openConnection();
                con.setUseCaches(false);
                long connectionLatency = System.currentTimeMillis() - startCon;
                stream = con.getInputStream();

                Message msgUpdateConnection = Message.obtain(mHandler, MSG_UPDATE_CONNECTION_TIME);
                msgUpdateConnection.arg1 = (int) connectionLatency;
                mHandler.sendMessage(msgUpdateConnection);

                long start = System.currentTimeMillis();
                int currentByte = 0;
                long updateStart = System.currentTimeMillis();
                long updateDelta = 0;
                int bytesInThreshold = 0;

                while ((currentByte = stream.read()) != -1) {
                    bytesIn++;
                    bytesInThreshold++;
                    if (updateDelta >= UPDATE_THRESHOLD) {
                        int progress = (int) ((bytesIn / (double) EXPECTED_SIZE_IN_BYTES) * 100);
                        Message msg = Message.obtain(mHandler, MSG_UPDATE_STATUS, calculate(updateDelta, bytesInThreshold));
                        msg.arg1 = progress;
                        msg.arg2 = bytesIn;
                        mHandler.sendMessage(msg);
                        //Reset
                        updateStart = System.currentTimeMillis();
                        bytesInThreshold = 0;
                    }
                    updateDelta = System.currentTimeMillis() - updateStart;
                }

                long downloadTime = (System.currentTimeMillis() - start);
                //Prevent ArithmeticException
                if (downloadTime == 0) {
                    downloadTime = 1;
                }

                Message msg = Message.obtain(mHandler, MSG_COMPLETE_STATUS, calculate(downloadTime, bytesIn));
                msg.arg1 = bytesIn;
                mHandler.sendMessage(msg);
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    //Suppressed
                }
            }

        }
    };

    *//*
      Get Network type from download rate

      @return 0 for Edge and 1 for 3G
     *//*
    private int networkType(final double kbps) {
        int type = 1;//3G
        //Check if its EDGE
        if (kbps < EDGE_THRESHOLD) {
            type = 0;
        }
        return type;
    }

    *//*
      1 byte = 0.0078125 kilobits
      1 kilobits = 0.0009765625 megabit

      @param downloadTime in miliseconds
     * @param bytesIn      number of bytes downloaded
     * @return SpeedInfo containing current speed
     *//*
    private SpeedInfo calculate(final long downloadTime, final long bytesIn) {
        SpeedInfo info = new SpeedInfo();
        //from mil to sec
        long bytespersecond = (bytesIn / downloadTime) * 1000;
        double kilobits = bytespersecond * BYTE_TO_KILOBIT;
        double megabits = kilobits * KILOBIT_TO_MEGABIT;
        info.downspeed = bytespersecond;
        info.kilobits = kilobits;
        info.megabits = megabits;

        return info;
    }

    *//*
      Transfer Object

      @author devil
     *//*
    private static class SpeedInfo {
        public double kilobits = 0;
        public double megabits = 0;
        public double downspeed = 0;
    }


    //Private fields
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int EXPECTED_SIZE_IN_BYTES = 1048576;//1MB 1024*1024

    private static final double EDGE_THRESHOLD = 176.0;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private Button mBtnStart;
    private TextView mTxtSpeed;
    private TextView mTxtConnectionSpeed;
    private TextView mTxtProgress;
    private TextView mTxtNetwork;

    private final int MSG_UPDATE_STATUS = 0;
    private final int MSG_UPDATE_CONNECTION_TIME = 1;
    private final int MSG_COMPLETE_STATUS = 2;

    private final static int UPDATE_THRESHOLD = 300;


    private DecimalFormat mDecimalFormatter;*/
}
