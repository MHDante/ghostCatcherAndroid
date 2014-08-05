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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dante on 2014-07-29.
 */
public class gcParser {

    private static XmlPullParser parser;
    private static gcEngine engine;

    public static gcEngine parseXML(Context context) throws XmlPullParserException, IOException {

        gcEngine.root = Uri.parse(new File(context.getExternalFilesDir("mixitmedia"), "ghostcatcher").getAbsolutePath());
        String textPath =  gcEngine.root + "/Exp1Chapter1.xml";
        InputStream in_s = new BufferedInputStream(new FileInputStream(textPath));

        parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in_s, null);

        engine = new gcEngine(context);
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    engine.characters = new HashMap<>();
                    engine.locations = new HashMap<>();
                    engine.seqPts = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "character":
                            gcCharacter chr = parseCharacter();
                            engine.characters.put(chr.getId(), chr);
                            break;
                        case "location":
                            gcLocation loc = parseLocation();
                            engine.locations.put(loc.getId(), loc);
                            break;
                        case "seq_pt":
                            engine.seqPts.add(parseSeqPt());
                            break;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return engine;
    }
    public static gcAction parseAction()
            throws IOException, XmlPullParserException {

        if (!parser.getName().equalsIgnoreCase("action"))
            throw new RuntimeException("Tried to parse something that wasn't an action");

        gcAction result = new gcAction();
        result.type = gcAction.Type.valueOf(parser.getAttributeValue(null, "type").toUpperCase());
        //result.lock = Boolean.parseBoolean(parser.getAttributeValue(null, "visible"));
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

    public static gcSeqPt parseSeqPt() throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("seq_pt"))
            throw new RuntimeException("Tried to parse something that wasn't a seqPt");


        gcSeqPt result = new gcSeqPt(engine);
        result.id = parser.getAttributeValue(null, "id");
        result.name = parser.getAttributeValue(null, "name");

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

        gcLocation result = new gcLocation(engine);
        result.setId(parser.getAttributeValue(null, "id"));
        result.setName(parser.getAttributeValue(null, "name"));
        result.setLatitude(Double.parseDouble(parser.getAttributeValue(null, "lat")));
        result.setLongitude(Double.parseDouble(parser.getAttributeValue(null, "long")));
        result.setDescription(parser.getAttributeValue(null, "description"));
        return result;
    }

    public static gcTrigger parseTrigger()
            throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("trigger"))
            throw new RuntimeException("Tried to parse something that wasn't a trigger");

        gcTrigger result = new gcTrigger();
        result.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        result.type = gcTrigger.Type.valueOf(parser.getAttributeValue(null, "type").toUpperCase());
        result.data = parser.getAttributeValue(null, "data");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "action":
                            result.actions.add(parseAction());
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
