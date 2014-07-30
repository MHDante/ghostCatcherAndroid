package ca.mixitmedia.ghostcatcher.app;

import android.location.Location;
import android.net.Uri;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.Tools.RFDetector;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.app.Tools.Tools;
import ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.experience.gcTrigger;
import ca.mixitmedia.ghostcatcher.views.LightButton;

public class ExperienceManager {

    MainActivity gcMain;
    public gcLocation location;
    public gcEngine engine;

    public ExperienceManager(MainActivity gcMain) {
        this.gcMain = gcMain;
        this.engine = gcMain.gcEngine;
        execute(engine.getCurrentSeqPt().getAutoTrigger());
    }

    public void execute(gcTrigger trigger) {
        boolean exit;
        if (trigger == null){
            Log.d("","Nothing to Do.");
            return;
        }
        for (gcAction action : trigger.getActions()) {
            if (action.isConsumed()) continue;
            String data = action.getData().toLowerCase();
            switch (action.getType()) {
                case ACHIEVEMENT:
                    break;
                case CHECK_TASK:
                    break;
                case COMPLETE_TASK:
                    break;
                case CONSUME_TRIGGER:
                    break;
                case ENABLE_TRIGGER:
                    engine.getCurrentSeqPt().getTrigger(Integer.parseInt(data)).setEnabled(true);
                    break;
                case DISABLE_TOOL:
                    Tools.byName(data).setEnabled(false);
                    break;
                case ENABLE_TOOL:
                    ToolFragment t = Tools.byName(data);
                    t.setEnabled(true);
                    LightButton.RefreshAll();
                    break;
                case END_SQPT:
                    engine.EndSeqPt();
                    break;
                case DIALOG:
                    try {
                        gcDialog dialog = gcDialog.get(engine.getCurrentSeqPt(), data);

                        Tools.communicator.sendMessage(new ToolFragment.ToolMessage(dialog, false));
                    } catch (IOException e) {
                        Utils.messageDialog(gcMain, "Error", e.getMessage());
                    }
                    break;
                case OUTOFSCREEN:
                    ProximityTest p = new ProximityTest() {
                        @Override
                        public void HandleServerMessage(String s) {
                            Utils.messageDialog(gcMain, "OUT OF APP", "It happened");
                        }
                    };
                            break;
            }
        }
    }
    public void UpdateLocation(Uri uri) {

        if (uri != null && uri.getScheme().equals("troubadour") && uri.getHost().equals("ghostcatcher.mixitmedia.ca")) {
            String path = uri.getLastPathSegment();
            String[] tokens = path.split("\\.");
            String type = tokens[1];
            String id = tokens[0];
            if (type.equals("location")) {
                UpdateLocation(gcMain.gcEngine.locations.get(id));
                return;
            }
            Toast.makeText(gcMain, "Location: " + id + " was not found", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText( gcMain, "Invalid Location URL", Toast.LENGTH_LONG).show();
        return;
    }

    public void UpdateLocation(Location location) {
        float accuracy = location.getAccuracy();
        for (gcLocation l : engine.locations.values()) {
            float distance[] = new float[3]; // ugh, ref parameters.
            Location.distanceBetween(l.getLatitude(), l.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] <= accuracy + 60) {
                location = l;
                Tools.rfDetector.onLocationChanged(l);
                gcTrigger trigger = engine.getCurrentSeqPt().getTrigger(l);

                execute(trigger);
                //Log.d("","Success");
                //todo:decide what to do
            }
        }
    }

    public gcLocation getDestination(){
        return engine.locations.get("lake_devo");
    }

    public void ToolSuccess(ToolFragment toolFragment) {
        execute(engine.getCurrentSeqPt().getSuccessTrigger(toolFragment));
    }
}