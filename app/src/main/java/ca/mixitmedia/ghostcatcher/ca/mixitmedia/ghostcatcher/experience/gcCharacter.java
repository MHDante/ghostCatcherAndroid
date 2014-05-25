package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dante on 07/03/14.
 */
public class gcCharacter {

    String id;

    public String getId() {
        return id;
    }

    String name;

    public String getName() {
        return name;
    }

    String bio;

    public String getBio() {
        return bio;
    }

    Map<String, String> poses = new HashMap<>();

    public String getPose(String id) {
        return poses.get(id);
    }


    private gcCharacter() {
    }

    public static gcCharacter parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (!parser.getName().equals("character"))
            throw new RuntimeException("Tried to parse something that wasn't a character");

        gcCharacter result = new gcCharacter();
        result.id = parser.getAttributeValue(null, "id");
        result.name = parser.getAttributeValue(null, "name");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "bio":
                            result.bio = parser.getText();
                            break;
                        case "pose":
                            result.poses.put(parser.getAttributeValue(null, "id"),
                                    parser.getAttributeValue(null, "image"));
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("character"))
                        return result;
                    break;
            }
            pEvent = parser.next();
        }
        throw new RuntimeException("CharacterParsing error : " + result.name);
    }

}
