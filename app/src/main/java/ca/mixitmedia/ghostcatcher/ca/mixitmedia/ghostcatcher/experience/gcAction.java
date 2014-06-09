package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Dante on 07/03/14.
 */
public class gcAction {


    private boolean consumed;

    public Type getType() {
        return type;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public String getData() {
        return data;
    }

    public enum Type {
        DIALOG, //start dialog
        ENABLE_TOOL, //enable tool
        DISABLE_TOOL,//son
        END_SQPT,//i
        ENABLE_TRIGGER,//am
        COMPLETE_TASK,//dissapoint
        CHECK_TASK,//
        ACHIEVEMENT,
        CONSUME_TRIGGER
    }

    Type type;
    String data;
    boolean lock;

    private gcAction() {
    }

    public static gcAction parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("action"))
            throw new RuntimeException("Tried to parse something that wasn't an action");

        gcAction result = new gcAction();
        result.type = Type.valueOf(parser.getAttributeValue(null, "type").toUpperCase());
        result.lock = Boolean.parseBoolean(parser.getAttributeValue(null, "visible"));
        if (parser.next() == XmlPullParser.TEXT) {
            result.data = parser.getText();
        }

        return result;
    }
}
