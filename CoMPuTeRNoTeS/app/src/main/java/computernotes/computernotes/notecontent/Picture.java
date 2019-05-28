package computernotes.computernotes.notecontent;


import android.net.Uri;


public class Picture extends NoteContent {
    Uri uri;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Picture(Uri uri) {
        this.uri = uri;

    }
}
