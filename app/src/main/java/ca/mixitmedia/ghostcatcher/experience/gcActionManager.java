package ca.mixitmedia.ghostcatcher.experience;


import com.google.android.gms.games.achievement.Achievement;

/**
 * Created by Dante on 2014-06-03.
 */
public abstract class gcActionManager {
    public void execute(gcTrigger trigger) {
        boolean exit;
        for (gcAction action : trigger.getActions()) {
            if (action.isConsumed()) continue;
            switch (action.getType()) {
                case ACHIEVEMENT:
                    achievement(action.getData());
                    break;
                case CHECK_TASK:
                    exit = checkTask(action.getData());
                    //todo: implement
                    break;
                case COMPLETE_TASK:
                    completeTask(action.getData());
                    //todo: implement
                    break;
                case CONSUME_TRIGGER:
                    consumeTrigger(action.getData());
                    //todo: implement
                    break;
                case ENABLE_TRIGGER:
                    enableTrigger(action.getData());
                    //todo: implement
                    break;
                case DISABLE_TOOL:
                    disableTool(action.getData());
                    //todo: implement
                    break;
                case ENABLE_TOOL:
                    enableTool(action.getData());
                    //todo: implement
                    break;
                case END_SQPT:
                    endSqPt(action.getData());
                    //todo: implement
                    break;
                case DIALOG:
                    startDialog(action.getData());
                    break;
            }
        }
    }

    public void startDialog(String dialogId) {

    }

    public void endSqPt(String NextSqPt) {

    }

    public void enableTool(String toolName) {

    }

    public void disableTool(String toolName) {

    }

    public void enableTrigger(String triggerId) {

    }

    public void consumeTrigger(String triggerId) {

    }

    public void completeTask(String taskName) {

    }

    public boolean checkTask(String taskName) {
        return false;
    }

    public void achievement(String data) {

    }

}
