package ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dante on 2014-07-29
 */
public class gcParser {

    private static XmlPullParser parser;
    private static gcEngine engine;

    public static gcEngine parseXML(Context context) throws XmlPullParserException, IOException {

        gcEngine.root = Uri.parse(new File(context.getExternalFilesDir("mixitmedia"), "ghostcatcher").getAbsolutePath());
        InputStream in_s = new BufferedInputStream(new FileInputStream(gcEngine.root + "/Exp1Chapter1.xml"));

        parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in_s, null);

        engine = new gcEngine(context);
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "character":
                        gcCharacter chr = parseCharacter();
                        engine.getCharacters().put(chr.getId(), chr);
                        break;
                    case "location":
                        gcLocation loc = parseLocation();
                        engine.getAllLocations().put(loc.getId(), loc);
                        break;
                    case "seq_pt":
                        engine.getSeqPts().add(parseSeqPt());
                        break;
                }
            }
            eventType = parser.next();
        }
        return engine;
    }
	
    public static gcAction parseAction(gcTrigger trigger)
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("action"))
            throw new RuntimeException("Tried to parse something that wasn't an action");

        gcAction result = new gcAction();
        result.trigger = trigger;
        result.type = gcAction.Type.valueOf(get("type").toUpperCase());
        result.locked = Boolean.parseBoolean(get("lock"));
        if (parser.next() == XmlPullParser.TEXT) {
            result.data = parser.getText();
        }

        return result;
    }

    public static gcCharacter parseCharacter()
            throws IOException, XmlPullParserException {
        if (!parser.getName().equals("character"))
            throw new RuntimeException("Tried to parse something that wasn't a character");

        gcCharacter result = new gcCharacter(engine);
        result.setId(get("id"));
        result.setName(get("name"));

        for (int pEvent = parser.next(); pEvent != XmlPullParser.END_DOCUMENT; pEvent = parser.next()) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "bio":
                            if (parser.next() == XmlPullParser.TEXT) result.bio = parser.getText();
                            break;

                        case "pose":
                            result.poses.put(get("id"), get("image"));
                            break;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("character")) return result;
                    break;
            }
            ;
        }
        throw new RuntimeException("CharacterParsing error : " + result.name);
    }

    public static gcSeqPt parseSeqPt() throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("seq_pt"))
            throw new RuntimeException("Tried to parse something that wasn't a seqPt");


        gcSeqPt result = new gcSeqPt(engine);
        result.id = get("id");
        result.name = get("name");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "mystery":
                            Mystery m = parseMystery();
                            result.mysteries.add(m);
                            break;
                        case "task":
                            result.tasks.add(parseTask());
                            break;
                        case "trigger":
                            result.triggers.add(parseTrigger());
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("seq_pt"))
                        return result;
                    break;
            }
            pEvent = parser.next();
        }
        throw new RuntimeException("SeqPt Parsing error : " + result.name);
    }

    public static gcLocation parseLocation()
            throws IOException, XmlPullParserException {

        if (!parser.getName().equals("location"))
            throw new RuntimeException("Tried to parse something that wasn't a location");

        gcLocation result = new gcLocation(engine,
		        Double.parseDouble(get("lat")),
		        Double.parseDouble(get("long")));
        result.setId(get("id"));
        result.setName(get("name"));
        result.setDescription(get("description"));
        return result;
    }

    public static gcTrigger parseTrigger()
            throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("trigger"))
            throw new RuntimeException("Tried to parse something that wasn't a trigger");

        gcTrigger result = new gcTrigger();
        result.id = Integer.parseInt(get("id"));
        result.type = gcTrigger.Type.valueOf(get("type").toUpperCase());
        result.data = get("data");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "action":
                            result.actions.add(parseAction(result));
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
	
    public static Mystery parseMystery()
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("mystery"))
            throw new RuntimeException("Tried to parse something that wasn't a mystery");

        Mystery result = new Mystery();

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "unsolved":
                            if (parser.next() == XmlPullParser.TEXT) {
                                result.UnSolved = parser.getText();
                            }
                            break;
                        case "solved":
                            if (parser.next() == XmlPullParser.TEXT) {
                                result.Solved = parser.getText();
                            }
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equalsIgnoreCase("mystery"))
                        return result;
                    break;
            }
            pEvent = parser.next();
        }
        throw new RuntimeException("mystery Parsing error : " + result.UnSolved);
    }
	
    public static Task parseTask()
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("Task"))
            throw new RuntimeException("Tried to parse something that wasn't a Task");

        Task result = new Task();
        result.id = get("id");
        result.enabled = Boolean.parseBoolean(get("enabled"));
        result.visible = Boolean.parseBoolean(get("visible"));
        result.completed = Boolean.parseBoolean(get("completed"));
        if (parser.next() == XmlPullParser.TEXT) {
            result.description = parser.getText();
        }

        return result;
    }
	
	public static String get(String name) {
		return parser.getAttributeValue(null, name);
	}
}
