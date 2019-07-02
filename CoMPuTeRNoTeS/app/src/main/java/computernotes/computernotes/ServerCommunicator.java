package computernotes.computernotes;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.users.User;

public class ServerCommunicator {
//    private static ServerCommunicator instance = null;



    public static List<NoteMain> notes;
    public static ArrayList<Tag> allTags;
    public static Settings settingsTest;
    public static boolean online;
    static public User user;
    static public FirebaseFirestore db;


    private static ServerCommunicator firstInstance = null;

    public ServerCommunicator() {


        db = FirebaseFirestore.getInstance();

        online = true;

        //initializing the settingsTest
        settingsTest = new Settings();
        settingsTest.setBottomToolbarActive(true);
        settingsTest.setDrawerMenuActive(true);

        //initializing the notes
        notes = new ArrayList<>();

        allTags = new ArrayList<>();

        allTags.add(new Tag("telegram"));
        allTags.add(new Tag("cool stuff"));
        allTags.add(new Tag("earn money"));

    }

    public static synchronized ServerCommunicator getInstance() {
        if (firstInstance == null) {
            firstInstance = new ServerCommunicator();
            synchronized (ServerCommunicator.class) {
                if (firstInstance == null)
                    firstInstance = new ServerCommunicator();
            }
        }
        return firstInstance;
    }

    static public void addToAllTags(Collection<Tag> newTags) {
        for (Tag t:newTags)
            if (!allTags.contains(t)) allTags.add(t);
    }

    static public ArrayList<Tag> getAllTags () {return allTags;}





}

