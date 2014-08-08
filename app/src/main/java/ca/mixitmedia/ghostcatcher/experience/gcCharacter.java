package ca.mixitmedia.ghostcatcher.experience;

import android.app.AlertDialog;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14
 */
public class gcCharacter {

    String id;
    String name;
    String bio;
    Map<String, String> poses = new HashMap<>();
    gcEngine engine;
    gcCharacter(gcEngine engine) {}



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

    public String getBio() {
        return bio;
    }

    public Uri getPose(String poseName) {
        if (!poses.containsKey(poseName)) {
            new AlertDialog.Builder(engine.context)
                    .setMessage("Character " + name + "does not have a pose with id: " + poseName)
                    .create().show();
        }
        return Uri.fromFile(new File(engine.root + "/characters/" + getId() + "/" + poses.get(poseName)));
    }

    public Uri getDefaultPose() {
        if (poses.size() <1){
            return Utils.resIdToUri(engine.context, R.drawable.shine);
        }
        else return getPose(new ArrayList<>(poses.keySet()).get(0));
    }

}
