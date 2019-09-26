package com.example.firebaseui_firestoreexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebaseui_firestoreexample.utils.MyApp;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends MyActivity {

//    Intent mServiceIntent;
//    private SensorService mSensorService;


    FirebaseFirestore db;
    private NoteAdapter adapter;
    FirebaseUser firebaseUser;

    boolean onCreateCalled;
    InternetThread internetThread;
    private TrafficLight lastTrafficLightState;

    private Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApp.getFirstInstance().registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        db = FirebaseFirestore.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            login();
        } else {

            saveUserToMyApp();

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

            createCacheLoaderTimerTask();

            new NotificationHelper(this).initNotificationChannels();

//            for testing internet speed.
//            mDecimalFormatter = new DecimalFormat("##.##");

            reception();

            locationPermission();

        }
    }

    private void locationPermission() {

//        perhaps better to call in login activity! trying to call at the end of onCreate to avoid unexpected changes whilst waiting for user to accept.
//        first check for permissions
//        overriding onRequestPermissionsResult and calling the creating telephony listener there if permission is given.
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION}
                        , 10);
            }
        }
//        else MyApp.setTelephonyListener(c);

    }

    private void newNote() {
        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, NewNoteActivity.class)));
    }

    private void startAppOffline() {
        //                probably better to use 2 different booleans one only for the anonymous user a bit repetitive but probably better.
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
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            recreate();
            pullToRefresh.setRefreshing(false);
        });
    }

    private void disablingInternetIfAnonymousUser() {
//            recreating here because the internet state has changed. using MyApp.userSkippedLogin so it only does this once.
        if (firebaseUser.isAnonymous() && !MyApp.userSkippedLogin) {
            MyApp.userSkippedLogin = true;
            MyApp.internetDisabledInternally = true;
            db.disableNetwork();
            recreate();
        }
    }

    private void saveUserToMyApp() {
        MyApp.uid = firebaseUser.getUid();
        MyApp.userDocumentRef = db.collection("users").document(MyApp.uid);
        MyApp.userDocumentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                assert cloudUser != null;
                MyApp.username = cloudUser.getUsername();
            }
        });
    }

    private void login() {
        db.enableNetwork();
        MyApp.internetDisabledInternally = false;
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if (requestCode == 10) {               MyApp.setTelephonyListener(c);
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                }
        //      }
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
        switch (MyApp.recyclerViewMode) {
            case "trash":
                recyclerViewTrash();
                break;
            case "shared":
                recyclerViewShared();
                break;
            case "search":
                recyclerViewSearch();
                break;
            default:
                recyclerViewAllNotes();

        }
    }

    private void setUpRecyclerViewWithQuery(Query query) {
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options, this, getIntent().getBooleanExtra("startAppAndCloseMainActivity", false));
        getIntent().putExtra("startAppAndCloseMainActivity", false);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,

                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (MyApp.recyclerViewMode.equals("trash"))
                    adapter.untrashItem(viewHolder.getAdapterPosition());
                else
                    adapter.trashItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            String id = documentSnapshot.getId();
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("noteID", id);
            MainActivity.this.startActivity(intent);

        });
    }

    private void recyclerViewSearch() {
        Query query = db.collection("notes").whereEqualTo("creator", MyApp.uid);// change this to have the search query.

        setUpRecyclerViewWithQuery(query);
    }

    private void recyclerViewShared() {
        Query query = db.collection("notes").whereArrayContains("shared", MyApp.username);
        setUpRecyclerViewWithQuery(query);

    }

    private void recyclerViewTrash() {
        Query query = db.collection("notes").whereEqualTo("creator", MyApp.uid).whereEqualTo("trash", true);
        setUpRecyclerViewWithQuery(query);
    }

    private void recyclerViewAllNotes() {
        Query query = db.collection("notes").whereEqualTo("creator", MyApp.uid).whereEqualTo("trash", false);
        setUpRecyclerViewWithQuery(query);
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
        if (!onCreateCalled && MyApp.lastTrafficLightState != lastTrafficLightState)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        MenuItem appInternInternetOffToggleMenuItem = menu.findItem(R.id.app_intern_internet_toggle);
        appInternInternetOffToggleMenuItem.setVisible(!MyApp.userSkippedLogin);

        MenuItem loginMenuItem = menu.findItem(R.id.login);
        loginMenuItem.setVisible(MyApp.userSkippedLogin);

        MenuItem logoutMenuItem = menu.findItem(R.id.logout);
        logoutMenuItem.setVisible(!MyApp.userSkippedLogin);


        if (MyApp.internetDisabledInternally)
            appInternInternetOffToggleMenuItem.setTitle("activate internet in App");
        else
            appInternInternetOffToggleMenuItem.setTitle("deactivate internet in App");

//        consider creating a method for all internet changes aside from the traffic light to handle all of the cases
//        which are not connected to the data but still require internet even if the internet is off internally.
        MenuItem reportBug = menu.findItem(R.id.report_bug);
        if (isNetworkAvailable())
            reportBug.setTitle("report bug");
        else
            reportBug.setTitle("no internet - report bug via whatsappReminder");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.speed_test:
                Toast.makeText(this, "last speed recorded: " + MyApp.totalTime / 1000, Toast.LENGTH_SHORT).show();
                reception();
                return true;
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
                recreate();
                return true;
            case R.id.shared:
                MyApp.recyclerViewMode = "shared";
                recreate();
                return true;
            case R.id.allNotes:
                MyApp.recyclerViewMode = "allNotes";
                recreate();
                return true;
            case R.id.report_bug:
                reportBug();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        db.enableNetwork();
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> login());
    }

    private void reportBug() {
        if (isNetworkAvailable()) {
            Uri uriUrl = Uri.parse("https://api.whatsapp.com/send?phone=4915905872952&text=my%20name%20is%20_writeyournamehere_%20.%20nice%20to%20meet%20you%20chagai%20&source=&data=");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else addWhatsappReminder();
    }

    private void addWhatsappReminder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);


        // Add another TextView here for the message label
        final EditText messageEditText = new EditText(c);
        messageEditText.setHint("write your message here");
        layout.addView(messageEditText); // Another add method

        alert.setTitle("report bug");
        alert.setMessage("press ok to set time");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            Log.i("AlertDialog", "TextEntry 2 Entered " + messageEditText.getText().toString());
            showDatePicker("", messageEditText.getText().toString());
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        alert.show();
    }

    public void showDatePicker(String number, String message) {
        DialogFragment newFragment = new MyDatePickerFragment(null, c, number, message);
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
