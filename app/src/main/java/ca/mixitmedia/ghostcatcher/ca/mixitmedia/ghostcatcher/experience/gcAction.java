package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

/**
 * Created by Dante on 07/03/14.
 */
public class gcAction {
    public enum ActionType {DIALOG, ENABLETOOL, DISABLETOOL, END_SQPT, ADD_PERIPHERAL_CONTENT, GRANT_BADGE}

    int id;
    ActionType type;
    String args;
    String description;
}
