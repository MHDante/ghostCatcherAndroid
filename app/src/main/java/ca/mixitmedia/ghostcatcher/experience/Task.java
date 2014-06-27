package ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Dante on 2014-05-25.
 */
public class Task {
    String id;
    boolean enabled;
    boolean visible;
    boolean completed;
    String description;

    public static Task parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("Task"))
            throw new RuntimeException("Tried to parse something that wasn't a Task");

        Task result = new Task();
        result.id = parser.getAttributeValue(null, "id");
        result.enabled = Boolean.parseBoolean(parser.getAttributeValue(null, "enabled"));
        result.visible = Boolean.parseBoolean(parser.getAttributeValue(null, "visible"));
        result.completed = Boolean.parseBoolean(parser.getAttributeValue(null, "completed"));
        if (parser.next() == XmlPullParser.TEXT) {
            result.description = parser.getText();
        }

        return result;
    }
}