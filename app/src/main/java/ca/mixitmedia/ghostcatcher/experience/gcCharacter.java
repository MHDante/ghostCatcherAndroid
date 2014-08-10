package ca.mixitmedia.ghostcatcher.experience;

import android.app.AlertDialog;
import android.net.Uri;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
            new AlertDialog.Builder(engine.getContext())
                    .setMessage("Character " + name + "does not have a pose with id: " + poseName)
                    .create().show();
        }
        return Uri.fromFile(new File(gcEngine.root + "/characters/" + getId() + "/" + poses.get(poseName)));
    }

}
