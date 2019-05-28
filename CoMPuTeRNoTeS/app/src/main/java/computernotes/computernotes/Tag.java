package computernotes.computernotes;

import computernotes.computernotes.exceptions.NotImplementedException;
import computernotes.computernotes.note.NoteMain;

public class Tag {

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    String tag;

    public Tag(String tag) {
        this.tag = tag;
    }

    private void deleteTag(String tag) throws NotImplementedException {
        throw new NotImplementedException();
    }


    private void editTag(String tag)throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(Object obj) {
        Tag tagObject = null;
        if (obj instanceof Tag)
            tagObject = (Tag) obj;
        else throw new IllegalArgumentException();
        return this.tag.equals(tagObject.tag);
    }

}
