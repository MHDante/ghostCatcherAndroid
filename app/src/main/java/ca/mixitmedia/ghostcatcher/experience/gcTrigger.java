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

    public void activate(gcActionManager actionManager) {
        int hitLock = 0;
        Iterate:
        for (gcAction a : actions) {
            hitLock++;
            switch (a.getType()) {
                case DIALOG:
                    actionManager.startDialog(a.getData());
                    if (a.lock) break Iterate;
                    continue;
                case ENABLE_TOOL:
                    actionManager.enableTool(a.getData());
                    continue;
                case DISABLE_TOOL:
                    actionManager.disableTool(a.getData());
                    continue;
                case END_SQPT:
                    actionManager.endSqPt(a.getData());
                    continue;
                case ENABLE_TRIGGER:
                    actionManager.enableTrigger(a.getData());
                    continue;
                case COMPLETE_TASK:
                    actionManager.completeTask(a.getData());
                    continue;
                case CHECK_TASK:
                    actionManager.checkTask(a.getData());
                    continue;
                case ACHIEVEMENT:
                    actionManager.achievement(a.getData());
                    continue;
                case CONSUME_TRIGGER:
                    actionManager.consumeTrigger(a.getData());
                    continue;
            }
        }
        if (hitLock < actions.size()) this.setType(Type.AUTO);
        gcEngine.Access().getCurrentSeqPt().triggers.remove(this);
    }

}
