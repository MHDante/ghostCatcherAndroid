package ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 07/03/14.
 */
public class gcTrigger {


    boolean enabled;
    int id;
    Type type;
    String data;
    List<gcAction> actions;

    gcTrigger() {
        actions = new ArrayList<>();
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

    public List<gcAction> getActions() {
        return actions;
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
