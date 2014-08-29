package ca.mixitmedia.ghostcatcher.experience;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Dante on 07/03/14
 */
public class gcTrigger {


    boolean enabled;
    boolean consumed;
    int id;
    Type type;
    String data;
    Queue<gcAction> actions;

    gcTrigger() {
        actions = new LinkedList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean value) {
        enabled = value;
    }

    public int getId() {
        return id;
    }
    public void setId(int value) {
        id = value;
    }

    public Type getType() {
        return type;
    }
    public void setType(Type value) {
        type = value;
    }

    public Queue<gcAction> getActions() {
        return actions;
    }

    public void consume() {
        consumed = true;
    }
    public boolean isConsumed() {
        return consumed;
    }

    public enum Type {
        AUTO,
        TOOL_SUCCESS,
        TOOL_FAILURE,
        LOCATION_ENTER,
        LOCATION_EXIT,
        SCRIPTED
    }
}
