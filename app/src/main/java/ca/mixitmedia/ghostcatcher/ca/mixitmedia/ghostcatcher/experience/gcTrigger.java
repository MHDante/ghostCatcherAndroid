package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

/**
 * Created by Dante on 07/03/14.
 */
public class gcTrigger {
    public enum TriggerType {AUTO, TOOLFINISH, ENTERLOCATION, EXITLOCATION, TOOLEVENT}

    int id;
    TriggerType type;
    String args;
    gcLocation location;
    String desc;

    gcAction[] actions;
}
