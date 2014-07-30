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
    gcEngine engine;
    gcLocation(gcEngine engine) {
        super("gcLocation Provider");
        this.engine = engine;
    }

    public Uri getImageUri() {
        File f = new File(gcEngine.root.getPath() + "/locations/" + getId() + ".png");
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
}

