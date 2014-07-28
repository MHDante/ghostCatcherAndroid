package ca.mixitmedia.ghostcatcher.experience;

import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Dante on 07/03/14.
 */
public class gcLocation extends Location {
	String id;
	String name;
	String description;

	private gcLocation() {
		super("gcLocation Provider");
	}

	public static gcLocation parse(XmlPullParser parser)
			throws IOException, XmlPullParserException {

		if (!parser.getName().equals("location"))
			throw new RuntimeException("Tried to parse something that wasn't a location");

		gcLocation result = new gcLocation();
		result.setId(parser.getAttributeValue(null, "id"));
		result.setName(parser.getAttributeValue(null, "name"));
		result.setLatitude(Double.parseDouble(parser.getAttributeValue(null, "lat")));
		result.setLongitude(Double.parseDouble(parser.getAttributeValue(null, "long")));
		result.setDescription(parser.getAttributeValue(null, "description"));
		return result;
	}

	public Uri getImageUri() {
		File f = new File(gcEngine.Access().root.getPath() + "/locations/" + getId() + ".png");
		if (!f.exists())
			//TODO: this should be a FileNotFoundException.
			throw new RuntimeException("error opening loc image: " + f.getAbsolutePath());
		return Uri.fromFile(f);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public static gcLocation fromURI(Uri uri){

        if (uri != null && uri.getScheme().equals("troubadour") && uri.getHost().equals("ghostcatcher.mixitmedia.ca")) {
            String path = uri.getLastPathSegment();
            String[] tokens = path.split("\\.");
            String type = tokens[1];
            String id = tokens[0];
            if (type.equals("location")) {
                return gcEngine.Access().getLocation(id);

            }
            Toast.makeText(gcEngine.Access().context, "Location: " + id + " was not found", Toast.LENGTH_LONG).show();
            return null;
        }
        Toast.makeText(gcEngine.Access().context, "Invalid Location URL", Toast.LENGTH_LONG).show();
        return  null;
    }
}

