package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.graphics.BitmapFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14.
 */
public class gcSeqPt {
    int id;
    String name;
    List<Task> tasks;
    List<Mystery> mysteries;
    List<gcTrigger> triggers;

    public List<gcLocation> getLocations() {
        List<gcLocation> locations = new ArrayList<>();
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.LOCATION) { //todo: abstract.
                for (gcLocation l : gcEngine.Access().locations) {
                    if (t.data == l.id)
                        locations.add(l);
                }
            }
        }
        return locations;
    }

    public gcTrigger getTrigger(gcLocation loc) {
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.LOCATION) { //todo: abstract.
                if (t.data.equalsIgnoreCase(loc.id) && t.isEnabled())
                    return t;
            }
        }
        return null;
    }

    private gcSeqPt() {
    }

    public static gcSeqPt parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (!parser.getName().equalsIgnoreCase("seq_pt"))
            throw new RuntimeException("Tried to parse something that wasn't a seqPt");

        gcSeqPt result = new gcSeqPt();
        result.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        result.name = parser.getAttributeValue(null, "name");

        int pEvent = parser.next();

        while (pEvent != XmlPullParser.END_DOCUMENT) {
            switch (pEvent) {
                case XmlPullParser.START_TAG:
                    switch (parser.getName().toLowerCase()) {
                        case "mystery":
                            result.mysteries.add(Mystery.parse(parser));
                            break;
                        case "task":
                            result.tasks.add(Task.parse(parser));
                            break;
                        case "triggers":
                            result.triggers.add(gcTrigger.parse(parser));
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

}
