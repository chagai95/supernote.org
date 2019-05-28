package computernotes.computernotes.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import computernotes.computernotes.NoteInfo;
import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.Tag;
import computernotes.computernotes.activities.adapters.NoteContentItemAdapter;
import computernotes.computernotes.activities.adapters.NotePictureItemAdapter;
import computernotes.computernotes.reminders.TimeReminder;
import computernotes.computernotes.reminders.UserReminder;
import computernotes.computernotes.reminders.utils.MyDatePickerFragment;
import computernotes.computernotes.note.Note;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.notecontent.NoteContent;
import computernotes.computernotes.notecontent.Paragraph;
import computernotes.computernotes.notecontent.Picture;
import computernotes.computernotes.reminders.LocationReminder;
import computernotes.computernotes.reminders.Reminder;
import computernotes.computernotes.users.CloudUser;
import computernotes.computernotes.utils.notifications.NotificationHelper;
//
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
import android.support.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class NoteActivity extends AppCompatActivity {

    Context c = this;
    NoteMain noteMain;
    NoteMain noteMainNewOrNot;
    EditText titleEditText;
    boolean newNote;
    //a list to store all the products
    ArrayList<NoteContent> noteContentList;
    FirebaseFirestore db;
    //the recyclerview
    RecyclerView recyclerViewNoteContentList;
    RecyclerView recyclerViewImageList;
    NoteContentItemAdapter adapterRecyclerViewNoteContentList;
    NotePictureItemAdapter adapterRecyclerViewNoteImageList;
    Uri downloadUrl;


    CursorPositionHelper cursorPositionHelper = new CursorPositionHelper();
    private static final int Result_Load_Image = 1;
    private boolean isDeleted = false;
    private boolean isSavedLocal;
    private boolean isSavedInCloud;
    Map<String, Object> data = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        if (savedInstanceState != null)
            if (!savedInstanceState.getBoolean("isSavedLocal", true))
                Toast.makeText(c, "when onSaveInstanceState was called the note was not yet saved, did onStop manage to save it? ", Toast.LENGTH_LONG).show();

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
// Access a Cloud Firestore instance from your Activity
        db = ServerCommunicator.db;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        newNote = getIntent().getIntExtra("note_index", -1) == -1;
        if (newNote) {
            // uses empty string to change titleEditText to "Enter Title!"
            noteMainNewOrNot = new NoteMain("");
            noteMainNewOrNot.getContentSections().add(new Paragraph(""));
            // grant location permission when entering the app -> change to only show the first time or when location is necessary or used.
            // TODO: for some reason gets skipped.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient fusedLocationClient;
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(c);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null)
                                noteMainNewOrNot.setCreated(new NoteInfo(location, new Date()));
                            else
                                noteMainNewOrNot.setCreated(new NoteInfo(null, new Date()));

                        }
                    });
                } else
                    noteMainNewOrNot.setCreated(new NoteInfo(null, new Date()));
            } else
                noteMainNewOrNot.setCreated(new NoteInfo(null, new Date()));

        } else
            noteMainNewOrNot = ServerCommunicator.notes.get(getIntent().getIntExtra("note_index", -1));

        noteMain = noteMainNewOrNot;

        titleEditText = findViewById(R.id.title);
        titleEditText.setTextSize(40);


        titleEditText.setText(noteMain.getTitle());
        if (noteMain.getTitle().equals("")) {
            titleEditText.setHint("Enter Title!");
        }

        noteContentList = noteMain.getContentSections();

        //getting the recyclerview from xml
        recyclerViewNoteContentList = findViewById(R.id.recyclerViewNote);
        recyclerViewNoteContentList.setHasFixedSize(true);

        //        problem: find a good ManagerLayout which can scroll vertically but list the items horizontally
        //        and break to the new line when there is no more room horizontally
        recyclerViewNoteContentList.setLayoutManager(new LinearLayoutManager(c));


        //creating recyclerview adapterRecyclerViewNoteList
        adapterRecyclerViewNoteContentList = new NoteContentItemAdapter(c, noteMain);

        //setting adapterRecyclerViewNoteList to recyclerview
        recyclerViewNoteContentList.setAdapter(adapterRecyclerViewNoteContentList);


        //swipe noteContent
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
                    adapterRecyclerViewNoteContentList.notifyItemRemoved(position); //item removed from recylcerview
                    noteContentList.remove(position);  //then remove item
                }
                if (direction == ItemTouchHelper.RIGHT) {    //if swipe left
                    adapterRecyclerViewNoteContentList.notifyItemRemoved(position); //item removed from recylcerview
                    noteContentList.remove(position);  //then remove item
                }

            }
        };

        //click on noteContent
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recyclerViewNoteContentList);

        // when noteContent clicked check which type it is for example picture - open a menu to enlarge or delete picture.
        recyclerViewNoteContentList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                try {
                    final int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
                    if (noteContentList.get(position) instanceof Paragraph) {
                        EditText paraEditText = rv.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.para);
                        cursorPositionHelper.setNoteContentPosition(position);
                        cursorPositionHelper.setEditTextCursorPosition(paraEditText);
                        cursorPositionHelper.setCursorPositionOnGalleryClick(paraEditText.getSelectionEnd());
                        // the position comes out wrong for some reason
                        System.out.println(paraEditText.getSelectionStart());
                        paraEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                               /* String text = s.toString();
                                //here is your code
                                ((Paragraph) noteContentList.get(position)).setParaString(text);
                                // go through all titles
                                String title = "#test Note 1";
                                if (text.contains(title)) {
                                    // copied from picture

                                    int indexFirst = text.indexOf('#');
                                    int indexLast = text.indexOf('#') + "#test Note 1".length();
//                                    String internalLink = text.substring(indexFirst, indexLast);

                                    ((Paragraph) noteContentList.get(position)).setParaString(text.subSequence(0, indexFirst).toString());


                                    // take the note we are currently at.
                                    if (position + 1 < noteContentList.size())
                                        noteContentList.add(new HyperlinkNote(ServerCommunicator.notes.get(0)));
                                    else
                                        noteContentList.add(position + 1, new HyperlinkNote(ServerCommunicator.notes.get(0)));
                                    if (position + 2 < noteContentList.size())
                                        noteContentList.add(new Paragraph(text.subSequence(indexLast + 1, text.length()).toString()));
                                    else
                                        noteContentList.add(position + 2, new Paragraph(text.subSequence(indexLast + 1, text.length()).toString()));

                                }
                                System.out.println(
                                        (
                                                (Paragraph) (
                                                        (List<NoteContent>) ServerCommunicator.notes.get(
                                                                getIntent().getIntExtra("note_index", -1)
                                                        ).getContentSections()
                                                ).get(position)
                                        ).getParaString()
                                );*/

                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count,
                                                          int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                    }
//                    if (noteContentList.get(position) instanceof Picture) {
//                        ImageView imageView = (ImageView) rv.findViewHolderForAdapterPosition(position).itemView.findViewById(R.firebaseID.pic);
//                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        recyclerViewImageList = findViewById(R.id.recyclerViewNotePics);
//        set to horizontal
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewImageList.setLayoutManager(layoutManager);
        recyclerViewImageList.setLayoutManager(new LinearLayoutManager(c));
        recyclerViewImageList.setHasFixedSize(true);
        adapterRecyclerViewNoteImageList = new NotePictureItemAdapter(c, noteMain.getImages());
        recyclerViewImageList.setAdapter(adapterRecyclerViewNoteImageList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: //back top button
                backTopButton();
                return true;
            case R.id.action_search_note:
                return true;
            case R.id.action_add_reminder:
                showDatePicker("", "");
                return true;
            case R.id.action_add_whatsappreminder:
                addWhatsappReminder();
                return true;
            case R.id.action_show_reminders:
                showReminders();
                return true;
            case R.id.gallery:
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, Result_Load_Image);
                return true;
            case R.id.generate_internal_link:
                generateInternalLink();
                return true;
            case R.id.location_reminder:
                createLocationReminder();
                return true;
            case R.id.delete:
                delete();
                return true;
            case R.id.action_add_tag:
                addTagsDialog();
                return true;
            case R.id.action_show_note_history:
                showNoteHistory();
                return true;

            case R.id.action_user_reminder:
                addUserReminder();
                return true;

            case R.id.action_move_to_trash:
                moveNoteToTrash();
                return true;

            case R.id.action_share_note_with_other_user:
                shareNoteWithOtherUser();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void shareNoteWithOtherUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add another TextView here for the message label
        final EditText userEditText = new EditText(c);
        userEditText.setHint("write username here");
        layout.addView(userEditText); // Another add method

        alert.setTitle("share note with other user");
        alert.setMessage("");
        alert.setView(layout); // Again this is a set method, not add

        userEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String text = s.toString();
                userEditText.setTextColor(Color.WHITE);
                ServerCommunicator.db.collection("users").whereEqualTo("username", text.toLowerCase())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            if (task.getResult().isEmpty()) {
                                userEditText.setTextColor(Color.RED);
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (ServerCommunicator.user != null)
                                        if (document.getId().equals(ServerCommunicator.user.getUid()))
                                            userEditText.setTextColor(Color.RED);
                                        else
                                            userEditText.setTextColor(Color.rgb(34, 139, 34));
                                    else userEditText.setHint("log in again");
                                }
                            }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //only works once for some reason
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ServerCommunicator.db.collection("users").whereEqualTo("username", userEditText.getText().toString()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String userIDString = document.getId();
                                        noteMain.getSharedWith().add(new CloudUser(userIDString));
                                        db.collection("notes").document(noteMain.getFirebaseID()).update("sharedWith", FieldValue.arrayUnion(userIDString));
                                        db.collection("users").document(userIDString).update("sharedWithMe", FieldValue.arrayUnion(userIDString));
                                    }
                                }
                            }
                        });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    private void moveNoteToTrash() {
        if (!newNote) {
            noteMain.getTags().set(0, new Tag("trash"));
            ServerCommunicator.notes.remove(noteMain);
        }
        onBackPressed();
    }

    // make sure everything is isSavedLocal before destroyed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSavedLocal = false; // new session
    }

    @Override
    protected void onStop() {
        super.onStop();
        // checking if not empty or deleted
        if ((!(noteMain.getTitle().equals("")
                && ((Paragraph) noteMain.getContentSections().get(0)).getParaString().equals("")
                && noteMain.getImages().size() == 0)
                && !isDeleted))
            uploadNote();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // checking if not empty or deleted
        if ((!(titleEditText.getText().toString().equals("")
                && ((Paragraph) noteContentList.get(0)).getParaString().equals(""))
                && !isDeleted))
            saveNoteTemporary();
    }

    //for add picture using cursor.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == Result_Load_Image && resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();

                EditText editTextCursorPosition = cursorPositionHelper.editTextCursorPosition;
                int cursorPositionOnGalleryClick = cursorPositionHelper.cursorPositionOnGalleryClick;
                int noteContentPosition = cursorPositionHelper.noteContentPosition;
                if (!(editTextCursorPosition == null) && !(cursorPositionOnGalleryClick == -1)) {
                    //change first paragraph
                    CharSequence enteredText = editTextCursorPosition.getText().toString();
                    ((Paragraph) noteContentList.get(noteContentPosition)).setParaString(enteredText.subSequence(0, cursorPositionOnGalleryClick).toString());

                    // works only on when added to the end of the list.
                    //possible problems: saves to slow.
                    // when going back to the parent activity and then back to the note it shows an object which is the picture but doesn't show the picture
                    //possible problems: must save the picture first as drawable on the device itself.
                    noteContentList.add(noteContentPosition + 1, new Picture(selectedImage));
                    System.out.println(noteContentPosition);

                    CharSequence cursorToEnd = enteredText.subSequence(cursorPositionOnGalleryClick, enteredText.length());
                    noteContentList.add(noteContentPosition + 2, new Paragraph(cursorToEnd.toString()));
                } else {
//                    noteContentList.add(new Picture(selectedImage));
                    FirebaseApp app;
                    FirebaseStorage storage;
                    StorageReference storageRef;
                    app = FirebaseApp.getInstance();
                    storage = FirebaseStorage.getInstance(app);
                    // Get a reference to the location where we'll store our photos
                    storageRef = storage.getReference("note images");
                    // Get a reference to store file at chat_photos/<FILENAME>
                    final StorageReference photoRef = storageRef.child(selectedImage.getLastPathSegment());
                    // Upload file to Firebase Storage
                    photoRef.putFile(selectedImage)
                            .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // When the image has successfully uploaded, we get its download URL

                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            downloadUrl = task.getResult();
                                            noteMain.getImages().add(downloadUrl.toString());
                                            adapterRecyclerViewNoteImageList.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                }
                System.out.println("picture is returned");
                recyclerViewNoteContentList.getAdapter().notifyDataSetChanged();
            }
        } catch (Exception ex) {
            Toast.makeText(NoteActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    // back top button is at menu home this is back of the android system!
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("CDA", "onBackPressed Called");
//        call the MainActivity
        /*Intent intent = new Intent(NoteActivity.this,MainActivity.class);
        startActivity(intent);*/
    }

    // Edited by chagai on the 4/2/2019 at 11:18 PM
    // gets called before onStop()
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Toast.makeText(c, "onSaveInstanceState called", Toast.LENGTH_LONG).show();
//        outState.putBoolean("isSavedLocal", isSavedLocal);
    }


    //from here downwards help methods/classes.

    // TODO: save and upload note in two different methods and then call save note in BOTH on back pressed methods.
    // we want to actually save the note automatically every few seconds or after anything has been changed.
    // saves a few times should only save once work with wait method, or only save in one place, onStop is preferable the problem is that
    // after implementing firebase adapter it should update on it's own.
    // but for now the adapter in main activity has to be notified after change is done three options: check if the object was saved already
    // online using the last object - check firebase tutorial, pass adapter to note activity or
    // create a listener for when onStop message is done - after the local update(using observer pattern).
    private void saveNoteTemporary() {
        isSavedLocal = true;
        if (newNote) {
            data.put("time", new Timestamp(noteMain.getCreated().getDate()));
            if (noteMain.getCreated().getLocation() != null)
                data.put("location", new GeoPoint(noteMain.getCreated().getLocation().getLatitude(), noteMain.getCreated().getLocation().getLongitude()));
            noteMain.getImages().add("");
            noteMain.getTags().add(new Tag(""));
            noteMain.setOwner(new CloudUser(FirebaseAuth.getInstance().getCurrentUser().getUid()));
            if (!ServerCommunicator.notes.contains(noteMain))
                if (ServerCommunicator.user != null)
                    if (noteMain.getOwner().equals(ServerCommunicator.user))
                        ServerCommunicator.notes.add(noteMain);
                    else Toast.makeText(c, "changes not saved", Toast.LENGTH_LONG);
                else ServerCommunicator.notes.add(noteMain);
        }
        noteMain.setOwner(new CloudUser(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        noteMain.setTitle(titleEditText.getText().toString());
        EditText paraEditText = findViewById(R.id.para);
        noteContentList.set(0, new Paragraph(paraEditText.getText().toString()));
        noteMain.setContentSections(noteContentList);
        data.put("title", noteMain.getTitle());
        ArrayList<String> contents = new ArrayList<>();
        for (NoteContent noteContent :
                noteMain.getContentSections()) {
            contents.add(((Paragraph) noteContent).getParaString());
        }
        ArrayList<String> tags = new ArrayList<>();
        for (Tag tag :
                noteMain.getTags()) {
            tags.add(tag.getTag());
        }

//        probably better to upload individually in each item menu case after the firebaseUser set.
        if (!newNote) {
            String noteID = noteMain.getFirebaseID();
            ArrayList<String> reminders = new ArrayList<>();
            for (Reminder reminder :
                    noteMain.getReminders()) {
                Map<String, Object> dataReminder = new HashMap<>();
                if (reminder instanceof TimeReminder) {
                    dataReminder.put("type", "time");
                    dataReminder.put("time", new Timestamp(((TimeReminder) reminder).getDate()));
                }
                if (reminder instanceof LocationReminder) {
                    dataReminder.put("type", "location");
                    dataReminder.put("location", new GeoPoint(((LocationReminder) reminder).getLocation().getLatitude(), ((LocationReminder) reminder).getLocation().getLongitude()));
                    dataReminder.put("radius", Double.toString(((LocationReminder) reminder).getRadius()));
                }
                if (reminder instanceof UserReminder) {
                    dataReminder.put("type", "firebaseUser");
                    dataReminder.put("firebaseUser", ((UserReminder) reminder).getCloudUser().getUid());
                    dataReminder.put("radius", Double.toString(((UserReminder) reminder).getRadius()));
                }
                dataReminder.put("noteID", noteID);
                db.collection("reminders").add(dataReminder);
            }
        }


        data.put("contents", contents);
        data.put("tags", tags);
        data.put("owner", ((CloudUser) noteMain.getOwner()).getUid());
        data.put("images", noteMain.getImages());
    }

    private void uploadNote() {
        if (newNote) {
            db.collection("notes")
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            noteMain.setFirebaseID(documentReference.getId());
                            isSavedInCloud = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } else {
            db.collection("notes").document(noteMain.getFirebaseID()).update(data);
        }
    }

    // Edited by chagai on the 3/12/2019 at 9:31 AM
    // creating Location Reminder
    private void createLocationReminder() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                for (Note noteMain :
                        ServerCommunicator.notes) {
                    for (Reminder reminder :
                            noteMain.getReminders()) {
                        if (reminder instanceof LocationReminder) {
                            float distanceInMeters = ((LocationReminder) reminder).getLocation().distanceTo(location);
                            if (((LocationReminder) reminder).getRadius() > (double) distanceInMeters) {
                                System.out.println("\n " + location.getLongitude() + " " + location.getLatitude());
                                Toast.makeText(NoteActivity.this, "\n " + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                                createNotificationForLocationReminder();
                            }

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
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

        if (locationManager != null)
            locationManager.requestLocationUpdates("gps", 500, 1, listener);
        createAlertForLocationReminder();

    }

    private void createAlertForLocationReminder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the number label, as noted in the comments
        final EditText radiusEditText = new EditText(c);
        radiusEditText.setHint("write the radius here");
        layout.addView(radiusEditText); // Notice this is an add method

        // Add another TextView here for the message label
        final EditText locationEditText = new EditText(c);
        locationEditText.setHint("write coordinates here");
        layout.addView(locationEditText); // Another add method

        alert.setTitle("Location Reminder");
        alert.setMessage("");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String radiusString = radiusEditText.getText().toString();
                String locationString = locationEditText.getText().toString();
                Log.i("AlertDialog", "TextEntry 1 Entered " + radiusString);
                Log.i("AlertDialog", "TextEntry 2 Entered " + locationString);

                Location location = new Location("");//provider name is unnecessary

                double radiusDouble = Double.parseDouble(radiusString);

                String[] split = locationString.split(",");
                double locationLatitude = Double.parseDouble(split[0]);
                double locationLongitude = Double.parseDouble(split[1]);
                location.setLatitude(locationLatitude);
                location.setLongitude(locationLongitude);

                LocationReminder locationReminder = new LocationReminder(location, radiusDouble, true, false);
                noteMain.addReminder(locationReminder);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();

    }

    //    Edited by chagai on the 3/12/2019 at 6:11 PM
    //    create a notification for a location reminder - change to work with MyBroadcastReceiver
    private void createNotificationForLocationReminder() {

        String title = noteMain.getTitle();
        String content = "insert what should be displayed as content";


        Intent intent = new Intent(c, NoteActivity.class);
        intent.putExtra("note_index", ServerCommunicator.notes.indexOf(noteMain));
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationHelper notificationHelper = new NotificationHelper(c);
        notificationHelper.createNotification("CHANNEL_ID", title, content, pendingIntent, R.mipmap.ic_launcher, null);
        notificationHelper.show(new Random(100).nextInt());
    }

    public void showDatePicker(String number, String message) {
        DialogFragment newFragment = new MyDatePickerFragment(noteMain, -1, this, number, message);
        newFragment.show(getFragmentManager(), "date picker");
    }

    private void backTopButton() {
        /*if(!isSavedLocal&&!isDeleted)
        saveNoteTemporary();*/
        NavUtils.navigateUpFromSameTask(this);
                /*Intent intentBack = new Intent(NoteActivity.this, MainActivity.class);
                startActivity(intentBack);*/
    }

    private void addWhatsappReminder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

// Add a TextView here for the number label, as noted in the comments
        final EditText numberEditText = new EditText(c);
        numberEditText.setHint("write your number here");
        layout.addView(numberEditText); // Notice this is an add method

        // Add another TextView here for the message label
        final EditText messageEditText = new EditText(c);
        messageEditText.setHint("write your message here");
        layout.addView(messageEditText); // Another add method

        alert.setTitle("WhatsApp contact");
        alert.setMessage("press ok to be redirected to WhatsApp");
        alert.setView(layout); // Again this is a set method, not add

        //only works once for some reason
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("AlertDialog", "TextEntry 1 Entered " + numberEditText.getText().toString());
                Log.i("AlertDialog", "TextEntry 2 Entered " + messageEditText.getText().toString());
                showDatePicker(numberEditText.getText().toString(), messageEditText.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    private void showReminders() {
        Intent intent = new Intent(NoteActivity.this, RemindersInNoteActivity.class);
        intent.putExtra("note_index", getIntent().getIntExtra("note_index", -1));
        startActivity(intent);
    }

    private void generateInternalLink() {
        //                Edited by chagai on the 3/13/2019 at 12:09 AM
//                copying internal link to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("title", "#" + titleEditText.getText().toString());
        if (clipboard != null)
            clipboard.setPrimaryClip(clip);
    }

    private void delete() {
        if (!newNote) {
            ServerCommunicator.notes.remove(noteMain);
            db.collection("notes").document(noteMain.getFirebaseID()).delete();
        } else {
            if (isSavedLocal)
                ServerCommunicator.notes.remove(noteMain);
            if (isSavedInCloud)
                db.collection("notes").document(noteMain.getFirebaseID()).delete();
        }
        isDeleted = true;
        onBackPressed();
    }

    public void addTagsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Add Tags");
        //add an EditText Field
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 20);
        input.setLayoutParams(lp);
        builder.setView(input);
        // add a checkbox list with all current available tags
        final String[] tags = getTagsStringArray(ServerCommunicator.getAllTags());
        boolean[] checkedItems = new boolean[tags.length];
        for (int i = 0; i < tags.length; i++) {
            checkedItems[i] = false;
        }
        builder.setMultiChoiceItems(tags, checkTags(tags, getTagsStringArray(noteMain.getTags())), new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    ArrayList<Tag> t = new ArrayList<Tag>();
                    t.add(new Tag(tags[which]));
                    noteMain.addTags(t);
                }
                else noteMain.getTags().remove(new Tag(tags[which]));
            }
        });
        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String[] newTags = input.getText().toString().split(",");
                    ArrayList<Tag> newTagsT = new ArrayList<>();
                    for (String s : newTags) {
                        newTagsT.add(new Tag(s));
                        System.out.println(s);
                    }
                    noteMain.addTags(newTagsT);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNoteHistory() {
        Intent intent = new Intent(NoteActivity.this, NoteHistoryActivity.class);
        if (newNote)
            intent.putExtra("note_index", ServerCommunicator.notes.size() - 1);
        else
            intent.putExtra("note_index", getIntent().getIntExtra("note_index", -1));
        startActivity(intent);
    }

    private void addUserReminder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the number label, as noted in the comments
        final EditText radiusEditText = new EditText(c);
        radiusEditText.setHint("write the radius here");
        layout.addView(radiusEditText); // Notice this is an add method

        // Add another TextView here for the message label
        final EditText userEditText = new EditText(c);
        userEditText.setHint("write username here");
        layout.addView(userEditText); // Another add method

        alert.setTitle("User Reminder");
        alert.setMessage("");
        alert.setView(layout); // Again this is a set method, not add

        userEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String text = s.toString();
                userEditText.setTextColor(Color.WHITE);
                ServerCommunicator.db.collection("users").whereEqualTo("username", text.toLowerCase())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            if (task.getResult().isEmpty()) {
                                userEditText.setTextColor(Color.RED);
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getId().equals(ServerCommunicator.user.getUid()))
                                        userEditText.setTextColor(Color.RED);
                                    else
                                        userEditText.setTextColor(Color.rgb(34, 139, 34));

                                }
                            }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //only works once for some reason
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ServerCommunicator.db.collection("users").whereEqualTo("username", userEditText.getText().toString()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String radiusString = radiusEditText.getText().toString();
                                        double radiusDouble = Double.parseDouble(radiusString);

                                        String userIDString = document.getId();

//              not sure it is good that firebaseUser reminder should inherit from LocationReminder.
                                        UserReminder userReminder = new UserReminder(radiusDouble, true, false, new CloudUser(userIDString), false);
                                        noteMain.addReminder(userReminder);
                                        Log.i("AlertDialog", "TextEntry 1 Entered " + radiusString);
                                    }
                                }
                            }
                        });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }


//    help methods.

    public boolean[] checkTags(String[] allTags, String[] currentTags) {
        ArrayList<String> cuttentTagsList = new ArrayList<String>();
        for (String s : currentTags) {
            cuttentTagsList.add(s);
        }
        boolean[] checked = new boolean[allTags.length];
        for (int i = 0; i < allTags.length; i++) {
            if (cuttentTagsList.contains(allTags[i])) {
                checked[i] = true;
            }
        }
        return checked;
    }

    private String[] getTagsStringArray(ArrayList<Tag> tags) {
        String[] tagsString = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            tagsString[i] = tags.get(i).getTag();
        }
        return tagsString;
    }

    class CursorPositionHelper {
        int noteContentPosition = -1;
        int cursorPositionOnGalleryClick = -1;
        EditText editTextCursorPosition = null;

        void setNoteContentPosition(int noteContentPosition) {
            this.noteContentPosition = noteContentPosition;
        }

        void setCursorPositionOnGalleryClick(int cursorPositionOnGalleryClick) {
            this.cursorPositionOnGalleryClick = cursorPositionOnGalleryClick;
        }

        void setEditTextCursorPosition(EditText editTextCursorPosition) {
            this.editTextCursorPosition = editTextCursorPosition;
        }
    }

}

