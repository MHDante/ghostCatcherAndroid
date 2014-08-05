package ca.mixitmedia.ghostcatcher.app.Tools;

import java.util.ArrayList;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.JournalFragment.BioList;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.views.LightButton;

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
    public static BioList      bioList      ;

    static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        Tools.gcMain = gcMain;
        communicator    = new Communicator();
        locationMap     = new LocationMap();
        amplifier       = new Amplifier();
        tester          = new Tester();
        imager          = new Imager();
        rfDetector      = new RFDetector();
        bioList         = new BioList();

        communicator    .setToolLight((LightButton)gcMain.findViewById(R.id.left_toolLight           ));
        locationMap     .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_locationMap   ));
        amplifier       .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_amplifier     ));
        tester          .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_bioList       ));
        imager          .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_imager        ));
        rfDetector      .setToolLight((LightButton)gcMain.findViewById(R.id.tool_light_rfDetector    ));
        bioList         .setToolLight( (LightButton)gcMain.findViewById(R.id.tool_light_bioList      ));
        bioList.setEnabled(true);
    }
    public static Iterable<ToolFragment> All(){
        ArrayList<ToolFragment> ret = new ArrayList<>();
        ret.add(communicator);
        ret.add(locationMap );
        ret.add(amplifier   );
        ret.add(tester      );
        ret.add(imager      );
        ret.add(rfDetector  );
        ret.add(bioList     );
        return ret;
    }

    public static ToolFragment Current() {
        return (ToolFragment)gcMain.getFragmentManager().findFragmentById(R.id.fragment_container);
    }
    public static ToolFragment byName(String ToolName){

        if(ToolName.equalsIgnoreCase("communicator"   )) return communicator;
        if(ToolName.equalsIgnoreCase("locationMap"    )) return locationMap ;
        if(ToolName.equalsIgnoreCase("amplifier"      )) return amplifier   ;
        if(ToolName.equalsIgnoreCase("tester"         )) return tester      ;
        if(ToolName.equalsIgnoreCase("imager"         )) return imager      ;
        if(ToolName.equalsIgnoreCase("rfDetector"     )) return rfDetector  ;
        if(ToolName.equalsIgnoreCase("biolist"        )) return bioList     ;
        Utils.messageDialog(gcMain, "Error", "Tried to get non-Existent Tool" + ToolName);
        return null;
    }
}