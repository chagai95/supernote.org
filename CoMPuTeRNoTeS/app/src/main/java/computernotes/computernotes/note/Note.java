package computernotes.computernotes.note;

import android.graphics.Picture;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import computernotes.computernotes.NoteInfo;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.Tag;
import computernotes.computernotes.exceptions.NotImplementedException;
import computernotes.computernotes.notecontent.NoteContent;
import computernotes.computernotes.notecontent.Paragraph;
import computernotes.computernotes.notecontent.hyperlink.HyperlinkNote;
import computernotes.computernotes.reminders.Reminder;
import computernotes.computernotes.users.CloudUser;
import computernotes.computernotes.users.User;

public abstract class Note {
    String title;
    URL externalLink;
    Picture QRCode;
    NoteInfo noteInfo;
    boolean savedInLocalStorage;
    User owner;
    ArrayList<CloudUser> sharedWith;
    ArrayList<Tag> tags;
    Collection<Reminder> reminders;
    ArrayList<NoteContent> contentSections;
    Collection<HyperlinkNote> hyperlinkedIn;
    String firebaseID;
    ArrayList<String> images;

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void setReminders(Collection<Reminder> reminders) {
        this.reminders = reminders;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public Note(String title) {
        this.title = title;
        tags = new ArrayList<>();
        sharedWith = new ArrayList<>();
        images = new ArrayList<>();
        reminders = new ArrayList<>();
        contentSections = new ArrayList<>();
        }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(URL externalLink) {
        this.externalLink = externalLink;
    }

    public Picture getQRCode() {
        return QRCode;
    }

    public void setQRCode(Picture QRCode) {
        this.QRCode = QRCode;
    }

    public NoteInfo getNoteInfo() {
        return noteInfo;
    }

    public void setNoteInfo(NoteInfo noteInfo) {
        this.noteInfo = noteInfo;
    }

    public boolean isSavedInLocalStorage() {
        return savedInLocalStorage;
    }

    public void setSavedInLocalStorage(boolean savedInLocalStorage) {
        this.savedInLocalStorage = savedInLocalStorage;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ArrayList<CloudUser> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(ArrayList<CloudUser> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public void addTags(Collection<Tag> newTags) {
        for (Tag t:newTags)
            if (!tags.contains(t)) tags.add(t);
        ServerCommunicator.addToAllTags(newTags);
    }


    public Collection<Reminder> getReminders() {
        return reminders;
    }

    public void addReminder(Reminder reminder) {
        this.reminders.add(reminder);
    }

    public ArrayList<NoteContent> getContentSections() {
        return contentSections;
    }

    public void setContentSections(ArrayList<NoteContent> contentSections) {
        this.contentSections = contentSections;
    }

    public Collection<HyperlinkNote> getHyperlinkedIn() {
        return hyperlinkedIn;
    }

    public void setHyperlinkedIn(Collection<HyperlinkNote> hyperlinkedIn) {
        this.hyperlinkedIn = hyperlinkedIn;
    }

    private void generateInternalLink(String internalLink) throws NotImplementedException {
        throw new NotImplementedException();
    }

    private void generateExternalLink(String internalLink) throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void editNote(String editedTitle, ArrayList<NoteContent> contentSections) throws NotImplementedException
    {
        title = editedTitle;
        this.contentSections = contentSections;
    }

    private void deleteNote() throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void addReminders(Collection<Reminder> newReminds) throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void shareWith(Collection<User> users) throws NotImplementedException
    {
        throw new NotImplementedException();
    }

}
