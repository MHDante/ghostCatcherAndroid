package ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.gcLocationManager;

/**
 * Created by Dante on 07/03/14.
 */
public class gcTrigger {


    public enum Type {
        AUTO,
        TOOL_SUCCESS,
        TOOL_FAILURE,
        LOCATION_ENTER,
        LOCATION_EXIT,
        SCRIPTED
    }

    boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    int id;

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }

    Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        type = value;
    }

    String data;

    List<gcAction> actions;

    public List<gcAction> getActions() {
        return actions;
    }

    private gcTrigger() {
        actions = new ArrayList<>();
    }


    public static gcTrigger parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("trigger"))
            throw new RuntimeException("Tried to parse something that wasn't a trigger");

        gcTrigger result = new gcTrigger();
        result.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        result.type = Type.valueOf(parser.getAttributeValue(null, "type").toUpperCase());
        result.data = parser.getAttributeValue(null, "data");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "action":
                            result.actions.add(gcAction.parse(parser));
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("trigger"))
                        return result;
                    break;
            }
            pEvent = parser.next();
        }
        throw new RuntimeException("trigger Parsing error : " + result.id);
    }


}
