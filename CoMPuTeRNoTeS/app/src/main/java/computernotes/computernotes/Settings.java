package computernotes.computernotes;

import java.util.ArrayList;

public class Settings {

    String leftSwipeNoteString;
    String rightSwipeNoteString;
    Boolean bottomToolbarActive;
    Boolean DrawerMenuActive;
    ArrayList<String> botTBprios = new ArrayList<>(5);
    ArrayList<String> drawerPrios = new ArrayList<>(5);

    public ArrayList<String> getDrawerPrios() {
        return drawerPrios;
    }

    public void setDrawerPrios(ArrayList<String> drawerPrios) {
        this.drawerPrios = drawerPrios;
    }

    public void addDrawerPrio(String item) {
        drawerPrios.add(item);
    }

    public void clearDrawerPrios() {
        drawerPrios.clear();
    }

    public ArrayList<String> getBotTBprios() {
        return botTBprios;
    }

    public void setBotTBprios(ArrayList<String> botTBprios) {
        this.botTBprios = botTBprios;
    }

    public void addBotTBprio(String item) {
        botTBprios.add(item);
    }

    public void clearBotPrios() {
        botTBprios.clear();
    }

    public Boolean getDrawerMenuActive() {
        return DrawerMenuActive;
    }

    public void setDrawerMenuActive(Boolean drawerMenuActive) {
        DrawerMenuActive = drawerMenuActive;
    }

    public Boolean getBottomToolbarActive() {
        return bottomToolbarActive;
    }

    public void setBottomToolbarActive(Boolean bottomToolbarActive) {
        this.bottomToolbarActive = bottomToolbarActive;
    }

    // Edited by chagai on the 3/6/2019 at 5:28 PM
    public static long swipedSnoozeTimeInSeconds = 30;
    public Settings() {
    }

    public String getLeftSwipeNoteString() {
        return leftSwipeNoteString;
    }

    public void setLeftSwipeNoteString(String leftSwipeNoteString) {
        this.leftSwipeNoteString = leftSwipeNoteString;
    }

    public String getRightSwipeNoteString() {
        return rightSwipeNoteString;
    }

    public void setRightSwipeNoteString(String rightSwipeNoteString) {
        this.rightSwipeNoteString = rightSwipeNoteString;
    }

    public static long getSwipedSnoozeTimeInSeconds() {
        return swipedSnoozeTimeInSeconds;
    }

    public static void setSwipedSnoozeTimeInSeconds(long swipedSnoozeTimeInSeconds) {
        Settings.swipedSnoozeTimeInSeconds = swipedSnoozeTimeInSeconds;
    }
}
