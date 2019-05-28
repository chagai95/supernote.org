package computernotes.computernotes.notecontent.hyperlink;

import android.provider.MediaStore;

public abstract class HyperlinkMessenger extends HyperlinkContact{
    MediaStore.Images.Thumbnails thumbnails;

    public HyperlinkMessenger(MediaStore.Images.Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }
}
