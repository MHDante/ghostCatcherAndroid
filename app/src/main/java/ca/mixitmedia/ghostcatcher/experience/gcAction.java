package ca.mixitmedia.ghostcatcher.experience;

/**
 * Created by Dante on 07/03/14
 */
public class gcAction {

    Type type;
    String data;
    gcTrigger trigger;
    boolean locked;

    public boolean isLocked() {
        return locked;
    }

    gcAction() {}

    public Type getType() {
        return type;
    }
    public String getData() {
        return data;
    }
    public gcTrigger getTrigger() {
        return trigger;
    }

    public enum Type {
        DIALOG,
        ENABLE_TOOL,
        DISABLE_TOOL,
        END_SQPT,
        ENABLE_TRIGGER,
        COMPLETE_TASK,
        CHECK_TASK,
        ACHIEVEMENT,
        CONSUME_TRIGGER,
        OUTOFSCREEN
    }
}
