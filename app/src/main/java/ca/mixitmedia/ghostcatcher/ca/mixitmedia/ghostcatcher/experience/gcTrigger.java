package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Dante on 07/03/14.
 */
public class gcTrigger {

    public enum Type {AUTO, TOOLFINISH, LOCATION, SCRIPTED}

    private boolean enabled;

    int id;
    Type type;
    String data;

    List<gcAction> actions;


    public boolean isEnabled() {
        return enabled;
    }

    public static gcTrigger parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("trigger"))
            throw new RuntimeException("Tried to parse something that wasn't a trigger");

        gcTrigger result = new gcTrigger();
        result.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        result.type = Type.valueOf(parser.getAttributeValue(null, "name").toUpperCase());
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
