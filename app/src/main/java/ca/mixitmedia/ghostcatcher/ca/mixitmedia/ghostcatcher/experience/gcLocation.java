package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14.
 */
public class gcLocation {
    public int id;
    public String name;
    public double latitude;
    public double longitude;
    public Bitmap image;
    public String description;

    public gcLocation(int id, String name, double latitude, double longitude, String description) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

}
