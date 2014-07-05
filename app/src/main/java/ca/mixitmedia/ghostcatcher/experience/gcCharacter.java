package ca.mixitmedia.ghostcatcher.experience;

import android.app.AlertDialog;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dante on 07/03/14
 */
public class gcCharacter {

    String id;

	public void setId(String id) {
		this.id = id;
	}

    public String getId() {
        return id;
    }

    String name;

	public void setName(String name) {
		this.name = name;
	}
    public String getName() {
        return name;
    }

    String bio;

    public String getBio() {
        return bio;
    }

    Map<String, String> poses = new HashMap<>();

    public Uri getPose(String poseName) {
        if (!poses.containsKey(poseName)) {
            new AlertDialog.Builder(gcEngine.Access().context)
                    .setMessage("Character " + name + "does not have a pose with id: " + poseName)
                    .create().show();
        }
        return Uri.fromFile(new File(gcEngine.Access().root +"/characters/" + getId() +"/"+ poses.get(poseName)));
    }

    private gcCharacter() {}

    public static gcCharacter parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (!parser.getName().equals("character"))
            throw new RuntimeException("Tried to parse something that wasn't a character");

        gcCharacter result = new gcCharacter();
        result.setId(parser.getAttributeValue(null, "id"));
        result.setName(parser.getAttributeValue(null, "name"));

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "bio":
                            if (parser.next() == XmlPullParser.TEXT) {
                                result.bio = parser.getText();
                            }

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
