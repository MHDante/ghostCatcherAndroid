package ca.mixitmedia.ghostcatcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Dante on 07/03/14.
 */
public class Utils {

    public static String removeExtension(String name) {
        final int lastPeriodPos = name.lastIndexOf('.');

        if (lastPeriodPos <= 0) {
            // No period after first character - return name as it was passed in
            return name;
        } else {
            // Remove the last period and everything after it
            return name.substring(0, lastPeriodPos);
        }
    }

    public static Uri resIdToUri(int resId) {
        return Uri.parse("android.resource://" + gcEngine.Access().context.getPackageName()
                + "/" + resId);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void messageDialog(Context context, String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setNeutralButton("OK", null);
        dialog.create().show();
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static int convertDpToPixelInt(float dp, Context context) {
        return Math.round(convertDpToPixel(dp, context));
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }


    //todo:One day I will use reflection for something cool.
    //public static <T> void AutoInitialize(Class<T> cls) {
//
    //    for (Field f : cls.getFields()) {
    //        try {
    //            f.set(null, f.getType().newInstance());
    //        } catch (InstantiationException | IllegalAccessException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }
    //}
//
    //public static <T, Q> Iterable<Q> MemberIterator(Class<T> container, Class<Q> MemberClass) {
    //    ArrayList<Q> ret = new ArrayList<>();
    //    for (Field f : container.getFields()) {
    //        try {
    //            ret.add(MemberClass.cast(f.get(null)));
    //        } catch (IllegalAccessException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }
    //    return ret;
    //}

    public static int findIdByName(String name) {
        try {
            Class res = R.id.class;
            Field field = res.getField(name);
            return field.getInt(null);
        } catch (Exception e) {
            Log.e("MyTag", "Failure to get drawable id.", e);
            return 0;
        }
    }

    public static int findDrawableIDByName(String name) {
        try {
            Class res = R.drawable.class;
            Field field = res.getField(name);
            return field.getInt(null);
        } catch (Exception e) {
            Log.e("MyTag", "Failure to get drawable id.", e);
            return 0;
        }
    }

    public static List<Uri> getNdefIntentURIs(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        ArrayList<Uri> ret = new ArrayList<>();
        if (rawMsgs != null) {
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
            for (NdefMessage message : msgs) {
                for (NdefRecord record : message.getRecords()) {
                    //Ignore the api warning, this is for demo, during which we will have api 16 at least
                    ret.add(record.toUri());
                }
            }
        }
        return ret;
    }

    public static long TimeSince(long startTimeMillis) {
        return startTimeMillis - System.currentTimeMillis();
    }
}
