package ca.mixitmedia.ghostcatcher.app;

import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;

import ca.mixitmedia.ghostcatcher.Utils;
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
    public gcAction pendingLock;

    public ExperienceManager(MainActivity gcMain) {
        this.gcMain = gcMain;
        this.engine = gcMain.gcEngine;
        execute(engine.getCurrentSeqPt().getAutoTrigger());
    }

    public void unLock(gcAction key){
        if (pendingLock == key) {
            gcTrigger pendingTrigger = pendingLock.getTrigger();
            pendingLock = null;
            execute(pendingTrigger);
        }
    }

    public boolean isLocked(){
        return pendingLock != null;
    }

	public void execute(gcTrigger trigger) {
        if (trigger == null || trigger.isConsumed()){return;}
            while (true) {
            if (trigger.getActions().size() < 1){
                trigger.consume();
                return;
            }

            gcAction action = trigger.getActions().remove();
            boolean lock = action.isLocked();

            String data = action.getData().toLowerCase();
            switch (action.getType()) {
                case ACHIEVEMENT:
                    lock = false;
                    break;
                case CHECK_TASK:
                    lock = false;
                    break;
                case COMPLETE_TASK:
                    lock = false;
                    break;
                case CONSUME_TRIGGER:
                    lock = false;
                    break;
                case ENABLE_TRIGGER:
                    engine.getCurrentSeqPt().getTrigger(Integer.parseInt(data)).setEnabled(true);
                    lock = false;
                    break;
                case DISABLE_TOOL:
                    ToolFragment t = Tools.byName(data);
                    t.setEnabled(false);
                    if(Tools.Current() == t) {
                        gcMain.swapTo(Tools.communicator);
                    }
                    lock = false;
                    break;
                case ENABLE_TOOL:
                    ToolFragment t2 = Tools.byName(data);
                    t2.setEnabled(true);
                    t2.sendMessage(new ToolFragment.ToolMessage(action, lock));
                    if (lock) pendingLock = action;
                    break;
                case END_SQPT:
                    engine.EndSeqPt();
                    lock = false;
                    break;
                case DIALOG:
                    try {
                        gcDialog.loadDialog(engine.getCurrentSeqPt(), data);//todo:threading
                        Tools.communicator.sendMessage(new ToolFragment.ToolMessage(action, lock));

                        if (Tools.Current() == Tools.communicator) Tools.communicator.CheckForMessages();
                        else Tools.communicator.getToolLight().setState(LightButton.State.flashing);
	                        
                        if (lock) pendingLock = action;
                    }
                    catch (IOException e) {
                        Utils.messageDialog(gcMain, "Error", e.getMessage());
                    }
                    break;
                case OUTOFSCREEN:
                    ProximityTest p = new ProximityTest() {
                        @Override
                        public void HandleServerMessage(String s) {
                            Toast.makeText(gcMain,s, Toast.LENGTH_LONG).show();
                        }
                    };
                    p.execute();
                    lock = false;
                    break;
            }
                if (lock)
                    return;
        }
    }

    public void UpdateLocation(Uri uri) {
        if (uri != null && uri.getScheme().equals("troubadour") && uri.getHost().equals("ghostcatcher.mixitmedia.ca")) {
            String[] tokens = uri.getLastPathSegment().split("\\.");
            String type = tokens[1];
            String id = tokens[0];
            if (type.equals("location")) UpdateLocation(gcMain.gcEngine.getAllLocations().get(id));
            else Toast.makeText(gcMain, "Location: " + id + " was not found", Toast.LENGTH_LONG).show();
        }
        else Toast.makeText( gcMain, "Invalid Location URL", Toast.LENGTH_LONG).show();
    }

    public void UpdateLocation(Location location) {
        for (gcLocation l : engine.getAllLocations().values()) {
            float distance[] = new float[3]; // ugh, ref parameters.
            Location.distanceBetween(l.getLatitude(), l.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] <= location.getAccuracy()) {
                Tools.rfDetector.onLocationChanged(l);
                execute(engine.getCurrentSeqPt().getTrigger(l));
                //todo:decide what to do
            }
        }
    }

    public gcLocation getDestination(){
        return engine.getAllLocations().get("lake_devo");
    }

    public void ToolSuccess(ToolFragment toolFragment) {
        execute(engine.getCurrentSeqPt().getSuccessTrigger(toolFragment));
    }
}