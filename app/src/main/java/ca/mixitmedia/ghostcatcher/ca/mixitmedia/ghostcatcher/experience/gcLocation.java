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
    public float latitude;
    public float longitude;
    public Bitmap image;
    public String description;

    public gcLocation() {
        image = BitmapFactory.decodeResource(gcEngine.getInstance().context.getResources(), R.drawable.ghost);
    }


}
