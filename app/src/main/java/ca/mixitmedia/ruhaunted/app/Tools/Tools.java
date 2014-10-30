package ca.mixitmedia.ruhaunted.app.Tools;

import android.util.Log;

import java.util.Arrays;

import ca.mixitmedia.ruhaunted.app.MainActivity;
import ca.mixitmedia.ruhaunted.app.R;
import ca.mixitmedia.ruhaunted.views.LightButton;

/**
 * Created by Dante on 2014-07-27
 */
public class Tools {
    public static Communicator communicator ;
    public static LocationMap  locationMap  ;
    public static RFDetector   rfDetector   ;
    static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        Tools.gcMain = gcMain;
        communicator    = new Communicator();
        locationMap     = new LocationMap();
        rfDetector      = new RFDetector();

        communicator    .setToolLight((LightButton) gcMain.findViewById(R.id.left_toolLight         ));
        locationMap     .setToolLight((LightButton) gcMain.findViewById(R.id.tool_light_locationMap ));
        rfDetector      .setToolLight((LightButton) gcMain.findViewById(R.id.tool_light_rfDetector  ));

    }

    public static Iterable<ToolFragment> All() {
        return Arrays.asList(
		        communicator,
		        locationMap,
		        rfDetector
                );
    }

    public static ToolFragment Current() {
        return (ToolFragment) gcMain.getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public static ToolFragment byName(String ToolName) {
		switch (ToolName.toLowerCase()) {
			case "communicator":    return communicator;
			case "locationmap":     return locationMap;
            case "rfdetector":      return rfDetector;
			default:
				Log.e("Tools", "Tried to get non-Existent Tool" + ToolName);
				return null;
		}
    }
}