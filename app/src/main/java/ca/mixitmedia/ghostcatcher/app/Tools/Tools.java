package ca.mixitmedia.ghostcatcher.app.Tools;

import android.util.Log;

import java.util.Arrays;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.views.LightButton;

/**
 * Created by Dante on 2014-07-27
 */
public class Tools {
    public static Communicator communicator ;
    public static LocationMap  locationMap  ;
    public static Amplifier    amplifier    ;
    public static Tester       tester       ;
    public static Imager       imager       ;
    public static RFDetector   rfDetector   ;
    public static Achievements achievements ;
    public static TrophyCase   trophyCase   ;

    static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        Tools.gcMain = gcMain;
        communicator    = new Communicator();
        locationMap     = new LocationMap();
        amplifier       = new Amplifier();
        tester          = new Tester();
        imager          = new Imager();
        rfDetector      = new RFDetector();
        achievements    = new Achievements();
        trophyCase      = new TrophyCase();

        communicator    .setToolLight((LightButton)gcMain.findViewById(R.id.left_toolLight  ));
        locationMap     .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_locationMap   ));
        amplifier       .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_amplifier     ));
        tester          .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_bioList       ));
        imager          .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_imager        ));
        rfDetector      .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_rfDetector    ));
        achievements    .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_badges       ));
        trophyCase      .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_vault        ));

    }

    public static Iterable<ToolFragment> All() {
        return Arrays.asList(
		        communicator,
		        locationMap,
		        amplifier,
		        tester,
		        imager,
		        rfDetector,
                achievements,
                trophyCase);
    }

    public static ToolFragment Current() {
        return (ToolFragment) gcMain.getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public static ToolFragment byName(String ToolName) {
		switch (ToolName.toLowerCase()) {
			case "communicator": return communicator;
			case "locationmap": return locationMap;
			case "amplifier": return amplifier;
			case "tester": return tester;
			case "imager": return imager;
			case "rfdetector": return rfDetector;
            case "achievements": return achievements;
            case "trophycase":return trophyCase;
			default:
				//TODO: should probably remove this dialog in favor of Log.e (below), since returning
				// null almost surely crashes everything, preventing the prompt from showing
				Utils.messageDialog(gcMain, "Error", "Tried to get non-Existent Tool" + ToolName);
				Log.e("Tools", "Tried to get non-Existent Tool" + ToolName);
				return null;
		}
    }
}