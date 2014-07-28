package ca.mixitmedia.ghostcatcher.app.Tools;

import java.util.ArrayList;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;

/**
 * Created by Dante on 2014-07-27.
 */
public class Tools{
    public static Communicator communicator ;
    public static LocationMap  locationMap  ;
    public static Amplifier    amplifier    ;
    public static Tester       tester       ;
    public static Imager       imager       ;
    public static RFDetector   rfDetector   ;

    private static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        Tools.gcMain = gcMain;
        communicator = new Communicator();
        locationMap = new LocationMap();
        amplifier = new Amplifier();
        tester = new Tester();
        imager = new Imager();
        rfDetector = new RFDetector();
        for(ToolFragment tool : Tools.All()){
            int toolLightId = Utils.findIdByName(tool.getClass().getSimpleName().toLowerCase());
            tool.toolLight = (ToolLightButton) gcMain.findViewById(toolLightId);
        }
    }
    public static Iterable<ToolFragment> All(){
        ArrayList<ToolFragment> ret = new ArrayList<>();
        ret.add(communicator);
        ret.add(locationMap );
        ret.add(amplifier   );
        ret.add(tester      );
        ret.add(imager      );
        ret.add(rfDetector  );
        return ret;
    }

    public static ToolFragment Current() {
        return (ToolFragment)gcMain.getFragmentManager().findFragmentById(R.id.fragment_container);
    }
}