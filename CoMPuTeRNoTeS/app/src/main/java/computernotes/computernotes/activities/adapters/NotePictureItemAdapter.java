package computernotes.computernotes.activities.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import computernotes.computernotes.R;

public class NotePictureItemAdapter extends RecyclerView.Adapter<NotePictureItemAdapter.NoteViewHolder> {


    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the notes in a list
    private List<String> images;

    //getting the context and note list with constructor
    public NotePictureItemAdapter(Context mCtx, List<String> images) {
        this.mCtx = mCtx;
        this.images = images;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);


        View view = inflater.inflate(R.layout.note_picture_item, null);
//        change to this and figure out how the scrolling works and how to change to wrap content
//        from match parent for height only.
//        View view = inflater.inflate(R.layout.note_content_item, parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        //getting the note of the specified position
        String url = images.get(position);
        Glide.with(mCtx)
                .load(url)
                .into(holder.imageView);

    }


    @Override
    public int getItemCount() {
        return images.size();
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        public NoteViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}
