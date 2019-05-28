package computernotes.computernotes.users;




import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import computernotes.computernotes.Settings;
import computernotes.computernotes.note.Note;
import computernotes.computernotes.Tag;
import computernotes.computernotes.exceptions.NotImplementedException;
import computernotes.computernotes.note.NoteMain;

public abstract class User {

    Collection<NoteMain> notes;
    ArrayList<NoteMain> trash;
    Collection<Settings> settingsCollection;
    String uid;

    public User(String uid) {
        this.uid = uid;
        trash = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private Collection<Note> search(String search, Date begin, Date end, Collection<Tag> filterTags)throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void createNote()throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    public void addAcount()throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    public Collection<NoteMain> getNotes() {
        return notes;
    }

    public void setNotes(Collection<NoteMain> notes) {
        this.notes = notes;
    }

    public ArrayList<NoteMain> getTrash() {
        return trash;
    }

    public void setTrash(ArrayList<NoteMain> trash) {
        this.trash = trash;
    }

    public Collection<Settings> getSettingsCollection() {
        return settingsCollection;
    }

    public void setSettingsCollection(Collection<Settings> settingsCollection) {
        this.settingsCollection = settingsCollection;
    }
    @Override
    public boolean equals(Object obj) {
        User user;
        if (obj instanceof User)
            user = (User) obj;
        else throw new IllegalArgumentException();
        return this.uid.equals(user.uid);
    }
}
