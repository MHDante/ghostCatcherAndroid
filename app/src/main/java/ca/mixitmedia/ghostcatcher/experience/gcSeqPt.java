package ca.mixitmedia.ghostcatcher.experience;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;

/**
 * Created by Dante on 07/03/14
 */

public class gcSeqPt {
    public Map<String, gcDialog> dialogCache = new HashMap<>();
    String id;
    String name;
    List<Task> tasks;
    List<Mystery> mysteries;
    List<gcTrigger> triggers;
    public gcEngine engine;

    gcSeqPt(gcEngine engine) {
        this.engine = engine;
        tasks = new ArrayList<>();
        mysteries = new ArrayList<>();
        triggers = new ArrayList<>();
    }

    public List<gcLocation> getActiveLocations() {
        List<gcLocation> locations = new ArrayList<>();
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.LOCATION_ENTER) { //todo: abstract.
                for (gcLocation l : engine.getAllLocations().values()) {
                    if (t.data.equals(l.getId()))
                        locations.add(l);
                }
            }
        }
        return locations;
    }

    public gcTrigger getTrigger(gcLocation loc) {
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.LOCATION_ENTER) { //todo: abstract.
                if (t.data.equalsIgnoreCase(loc.getId()))
                    if (t.isEnabled())
                        return t;
            }
        }
        return null;
    }
    public gcTrigger getSuccessTrigger(ToolFragment tf) {
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.TOOL_SUCCESS) { //todo: abstract.
                if (t.data.equalsIgnoreCase(((Object) tf).getClass().getSimpleName()))
                    if (t.isEnabled())
                        return t;
            }
        }
        return null;
    }

    public gcTrigger getAutoTrigger() {
        for (gcTrigger t : triggers) {
            if (t.type == gcTrigger.Type.AUTO) { //todo: abstract.
                return t;
            }
        }
        return null;
    }

    public gcTrigger getTrigger(int triggerId) {
        for (gcTrigger trigger : triggers) {
            if (trigger.getId() == triggerId) {
                return trigger;
            }
        }
        return null;
    }
}
