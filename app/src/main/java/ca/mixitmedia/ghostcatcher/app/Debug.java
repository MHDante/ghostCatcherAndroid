package ca.mixitmedia.ghostcatcher.app;

import android.util.Log;


/**
 * Created by Dante on 02/03/14.
 */
public final class Debug {
    private Debug() {
    }

    public static void out(Object msg) {
        Log.i("info", msg.toString());
    }
}