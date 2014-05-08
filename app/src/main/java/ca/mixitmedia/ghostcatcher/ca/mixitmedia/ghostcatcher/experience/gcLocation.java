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
    public String audio;

    public gcLocation(int id, String name, double latitude, double longitude, Bitmap image, String description, String audio) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.description = description;
        this.audio = audio;
    }

}
