package ca.mixitmedia.ghostcatcher.experience;

import android.net.Uri;

import java.io.File;
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
    gcCharacter(gcEngine engine) {
        this.engine = engine;
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

    public String getBio() {
        return bio;
    }

    public Uri getPose(String poseName) {
        if (!poses.containsKey(poseName)) {
            Utils.messageDialog( engine.getContext(), "Error","Character " + name + " does not have a pose with id: " + poseName);
            return null;
        }
        return Uri.fromFile(new File(gcEngine.root + "/characters/" + getId() + "/" + poses.get(poseName)));
    }

    public Uri getDefaultPose() {
        if (poses.size() <1){
            return Utils.resIdToUri(engine.getContext(), R.drawable.shine);
        }
        else return getPose(new ArrayList<>(poses.keySet()).get(0));
    }
}
