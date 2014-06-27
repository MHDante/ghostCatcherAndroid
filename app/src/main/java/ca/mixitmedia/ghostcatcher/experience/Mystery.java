package ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Dante on 2014-05-25.
 */
public class Mystery {
    String UnSolved;

    public String getUnsolved() {
        return UnSolved;
    }

    String Solved;

    public String getSolved() {
        return Solved;
    }

    private Mystery() {
    }

    public static Mystery parse(XmlPullParser parser)
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
}