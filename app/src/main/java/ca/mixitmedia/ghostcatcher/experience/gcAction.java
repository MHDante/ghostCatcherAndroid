package ca.mixitmedia.ghostcatcher.experience;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Dante on 07/03/14
 */
public class gcAction {

    Type type;
    String data;
    boolean lock;
    //TODO: consumed is never set.
    private boolean consumed;

    gcAction() {
    }

    public Type getType() {
        return type;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public String getData() {
        return data;
    }

    public enum Type {
        DIALOG,
        ENABLE_TOOL,
        DISABLE_TOOL,
        END_SQPT,
        ENABLE_TRIGGER,
        COMPLETE_TASK,
        CHECK_TASK,
        ACHIEVEMENT,
        CONSUME_TRIGGER,
        OUTOFSCREEN
    }
}
