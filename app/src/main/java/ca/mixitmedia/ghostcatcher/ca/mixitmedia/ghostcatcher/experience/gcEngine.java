package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.gcMediaService;
import ca.mixitmedia.ghostcatcher.utils.ChapterLoader;

/**
 * Created by Dante on 07/03/14.
 */
public class gcEngine {
    private static gcEngine ourInstance = new gcEngine();
    public Context context;
    ChapterLoader loader;

    public static gcEngine getInstance() {
        return ourInstance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    private gcEngine() {
        loader = new ChapterLoader("mixitmedia");
    }


    public gcLocation getNextLocation() {
        return new gcLocation();
    }

    public Uri getSoundUri(String sound) {
        File f = new File(loader.root, sound + ".mp3");
        return Uri.fromFile(f);

        //return Uri.parse("android.resource://ca.mixitmedia.ghostcatcher.app/" + R.raw.bg1);
    }

    public CharSequence getNextToDo() {
        return "You must go defeat the dargon"; //todo: correct spelling of dragon
    }
}
