package computernotes.computernotes.note;

import java.util.LinkedList;
import java.util.List;

import computernotes.computernotes.NoteInfo;

public class NoteMain extends Note{

    private List<NoteVersion> noteHistory;
    private NoteInfo created;
    public NoteMain(String title) {
        super(title);
        noteHistory = new LinkedList<>();
    }

    public NoteMain() {
        super("");
    }

    public List<NoteVersion> getNoteHistory() {
        return noteHistory;
    }

    @Override
    public boolean equals(Object obj) {
        NoteMain noteMain;
        if (obj instanceof NoteMain)
            noteMain = (NoteMain) obj;
        else throw new IllegalArgumentException();
        boolean checkID = false;
        if(noteMain.getFirebaseID()!=null && this.firebaseID!=null)
            checkID = this.firebaseID.equals(noteMain.getFirebaseID());
        // TODO: set an option in settings to allow or disable duplicate notes
        return checkID || this.getTitle().equals(noteMain.getTitle());
    }

    public NoteInfo getCreated() {
        return created;
    }

    public void setCreated(NoteInfo created) {
        this.created = created;
    }
}
