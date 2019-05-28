package computernotes.computernotes.notecontent;


public class Paragraph extends NoteContent {
    String paraString;

    public String getParaString() {
        return paraString;
    }

    public void setParaString(String paraString) {
        this.paraString = paraString;
    }

    public Paragraph(String paraString) {
        this.paraString = paraString;
    }
}
