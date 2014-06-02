package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14.
 */
public class gcLocation {
    String id;
    String name;
    double latitude;
    double longitude;
    String description;

    private gcLocation() {
    }

    public static gcLocation parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        if (!parser.getName().equals("location"))
            throw new RuntimeException("Tried to parse something that wasn't a location");

        gcLocation result = new gcLocation();
        result.id = parser.getAttributeValue(null, "id");
        result.name = parser.getAttributeValue(null, "name");
        result.latitude = Double.parseDouble(parser.getAttributeValue(null, "latitude"));
        result.longitude = Double.parseDouble(parser.getAttributeValue(null, "longitude"));
        result.description = parser.getAttributeValue(null, "description");
        return result;
    }

    public Uri getImageUri() {
        File f = new File(gcEngine.Access().root.getPath() + "/locations/" + name + ".png");
        if (!f.exists()) throw new RuntimeException("error opening loc image");
        return Uri.fromFile(f);
    }
}

