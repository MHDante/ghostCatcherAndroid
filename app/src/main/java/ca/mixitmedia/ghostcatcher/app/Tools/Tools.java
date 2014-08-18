package ca.mixitmedia.ghostcatcher.app.Tools;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 2014-07-27
 */
public class Tools {
    public static Communicator communicator ;
    public static LocationMap  locationMap  ;
    public static Imager       imager       ;
    public static RFDetector   rfDetector   ;

    static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        Tools.gcMain = gcMain;
        communicator    = new Communicator();
        locationMap     = new LocationMap();
        imager          = new Imager();
        rfDetector      = new RFDetector();

    }

    public static List<ToolFragment> All() {
        return Arrays.asList(
		        communicator,
		        locationMap,
		        imager,
		        rfDetector);
    }

    public static ToolFragment Current() {
        if (gcMain == null) return null;
        return (ToolFragment) gcMain.getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public static ToolFragment byName(String ToolName) {
		switch (ToolName.toLowerCase()) {
			case "communicator": return communicator;
			case "locationmap": return locationMap;
			case "imager": return imager;
            case "rfdetector": return rfDetector;
			default:
				//TODO: should probably remove this dialog in favor of Log.e (below), since returning
				// null almost surely crashes everything, preventing the prompt from showing
				Utils.messageDialog(gcMain, "Error", "Tried to get non-Existent Tool" + ToolName);
				Log.e("Tools", "Tried to get non-Existent Tool" + ToolName);
				return null;
		}
    }
}