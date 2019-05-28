package computernotes.computernotes.users;

import java.util.Collection;

import computernotes.computernotes.exceptions.NotImplementedException;
import computernotes.computernotes.note.NoteMain;

public class CloudUser extends User {
    String email;
    String name;
    String username;
    String password;
    Collection<NoteMain> sharedNotes;
    Collection<CloudUser> friends;


    public CloudUser(String uid) {
        super(uid);
    }

    private void login() throws NotImplementedException {
        throw new NotImplementedException();
    }

    private void logout() throws NotImplementedException {
        throw new NotImplementedException();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<NoteMain> getSharedNotes() {
        return sharedNotes;
    }

    public void setSharedNotes(Collection<NoteMain> sharedNotes) {
        this.sharedNotes = sharedNotes;
    }

    public Collection<CloudUser> getFriends() {
        return friends;
    }

    public void setFriends(Collection<CloudUser> friends) {
        this.friends = friends;
    }
}
