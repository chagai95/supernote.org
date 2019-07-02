package computernotes.computernotes.activities.adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import computernotes.computernotes.R;
import computernotes.computernotes.note.NoteMain;
import computernotes.computernotes.note.NoteVersion;
import computernotes.computernotes.notecontent.NoteContent;
import computernotes.computernotes.notecontent.Paragraph;
import computernotes.computernotes.notecontent.Picture;
import computernotes.computernotes.notecontent.hyperlink.HyperlinkNote;

public class NoteContentItemAdapter extends RecyclerView.Adapter<NoteContentItemAdapter.NoteViewHolder> {


    //this context we will use to inflate the layout
    private Context mCtx;
    NoteMain noteMain;

    //we are storing all the notes in a list
    private List<NoteContent> noteContentList;

    //getting the context and note list with constructor
    public NoteContentItemAdapter(Context mCtx, NoteMain noteMain) {
        this.mCtx = mCtx;
        this.noteMain = noteMain;
        this.noteContentList = noteMain.getContentSections();
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);


        View view = inflater.inflate(R.layout.note_content_item, null);
//        change to this and figure out how the scrolling works and how to change to wrap content
//        from match parent for height only.
//        View view = inflater.inflate(R.layout.note_content_item, parent,false);
        LinearLayout layout = view.findViewById(R.id.note_content_item);
        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        //getting the note of the specified position
        final NoteContent noteContent = noteContentList.get(position);
        if (noteContent instanceof Paragraph) {
            //testing for paragraph
            Paragraph paragraph = (Paragraph) noteContent;
            //binding the data with the viewholder views
            String text = paragraph.getParaString();

            holder.textPara.setText(text);
            holder.textPara.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    NoteVersion noteVersion = new NoteVersion(noteMain);
                    noteVersion.getContentSections().add(new Paragraph(charSequence.toString()));
                    noteMain.getNoteHistory().add(noteVersion);
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            holder.textHyperlinkNote.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
        }


        if (noteContent instanceof HyperlinkNote) {
            holder.textHyperlinkNote.setMovementMethod(LinkMovementMethod.getInstance());

            holder.imageView.setVisibility(View.GONE);
            holder.textPara.setVisibility(View.GONE);
        }
        if (noteContent instanceof Picture) {
            //testing for paragraph
            Picture picture = (Picture) noteContent;
            //binding the data with the viewholder views
            holder.imageView.setImageURI(picture.getUri());
            holder.textPara.setVisibility(View.GONE);
            holder.textHyperlinkNote.setVisibility(View.GONE);
        }


    }


    @Override
    public int getItemCount() {
        return noteContentList.size();
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textPara, textHyperlinkNote, textViewRating, textViewPrice;
        ImageView imageView;

        public NoteViewHolder(View itemView) {
            super(itemView);
            textPara = itemView.findViewById(R.id.para);
            textHyperlinkNote = itemView.findViewById(R.id.textHyperlinkNote);
            imageView = itemView.findViewById(R.id.pic);


        }
    }
}
