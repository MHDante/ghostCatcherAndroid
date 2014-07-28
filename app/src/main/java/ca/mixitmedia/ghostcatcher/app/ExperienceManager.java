package ca.mixitmedia.ghostcatcher.app;

import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.app.Tools.Tools;
import ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcTrigger;

public class ExperienceManager {

    MainActivity gcMain;

    public ExperienceManager(MainActivity gcMain) {
        this.gcMain = gcMain;
    }

    public void execute(gcTrigger trigger) {
        boolean exit;
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
                    gcEngine.Access().getCurrentSeqPt().getTrigger(Integer.parseInt(data)).setEnabled(true);
                    break;
                case DISABLE_TOOL:
                    Tools.byName(data).setEnabled(false);
                case ENABLE_TOOL:
                    Tools.byName(data).setEnabled(true);
                case END_SQPT:
                    gcEngine.EndSeqPt();
                    break;
                case DIALOG:
                    gcDialog dialog = gcDialog.get(gcEngine.Access().getCurrentSeqPt(), data);
                    Tools.communicator.sendMessage(new ToolFragment.ToolMessage(data, false));
                    break;
            }
        }
    }

}