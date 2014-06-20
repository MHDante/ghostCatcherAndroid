package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 07/03/14.
 */
public class gcTrigger {

    public Type getType() {
        return type;
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

    private boolean enabled;

    int id;
    Type type;
    String data;

    List<gcAction> actions;

    private gcTrigger() {
        actions = new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
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

    public void activate(gcActionManager actionManager) {
        for(gcAction a : actions){
            switch(a.getType()){
                case DIALOG:
                    actionManager.startDialog(a.getData());
                            return;
                case ENABLE_TOOL:
                    actionManager.enableTool(a.getData());
                            return;
                case DISABLE_TOOL:
                    actionManager.disableTool(a.getData());
                            return;
                case END_SQPT:
                    actionManager.endSqPt(a.getData());
                            return;
                case ENABLE_TRIGGER:
                    actionManager.enableTrigger(a.getData());
                            return;
                case COMPLETE_TASK:
                    actionManager.completeTask(a.getData());
                            return;
                case CHECK_TASK:
                    actionManager.checkTask(a.getData());
                            return;
                case ACHIEVEMENT:
                    actionManager.achievement(a.getData());
                            return;
                case CONSUME_TRIGGER:
                    actionManager.consumeTrigger(a.getData());
                            return;
            }
        }
    }

}
