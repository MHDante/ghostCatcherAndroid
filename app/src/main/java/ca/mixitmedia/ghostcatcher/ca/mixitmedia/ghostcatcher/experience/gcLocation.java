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
    public String id;
    public String name;
    double latitude;

    public double getLatitude() {
        return latitude;
    }

    double longitude;

    public double getLongitude() {
        return longitude;
    }

    public String description;

    private gcLocation() {
    }

    public static gcLocation parse(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        if (!parser.getName().equals("location"))
            throw new RuntimeException("Tried to parse something that wasn't a location");

        gcLocation result = new gcLocation();
        result.id = parser.getAttributeValue(null, "id");
        result.name = parser.getAttributeValue(null, "name");
        result.latitude = Double.parseDouble(parser.getAttributeValue(null, "lat"));
        result.longitude = Double.parseDouble(parser.getAttributeValue(null, "long"));
        result.description = parser.getAttributeValue(null, "description");
        return result;
    }

    public Uri getImageUri() {
        File f = new File(gcEngine.Access().root.getPath() + "/locations/" + id + ".png");
        if (!f.exists())
            throw new RuntimeException("error opening loc image: " + f.getAbsolutePath());
        return Uri.fromFile(f);
    }
}

