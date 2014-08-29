package ca.mixitmedia.ghostcatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14
 */
public class Utils {

    public static String removeExtension(String str) {
        return str.substring(0, str.lastIndexOf('.'));
    }

    public static Uri resIdToUri(Context context, int resId) {
        return Uri.parse("android.resource://"+context.getPackageName()+"/"+resId);
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
    public static long getMediaDuration(Uri uri){

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(uri.getPath());
                // convert duration to minute:seconds
        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.d("utilS",duration);
        return Long.parseLong(duration);
    }

    public static void messageDialog(Context context, String title, String message) {
		new AlertDialog.Builder(context)
		        .setTitle(title)
		        .setMessage(message)
		        .setNeutralButton("OK", null)
		        .create()
			    .show();
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
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
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
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
            Log.e("MyTag", "Failure to get drawable id: " + name, e);
            return 0;
        }
    }

    //ft. OrbIt
    public static float Triangle(float num, float mod) {
        float a = Math.abs(num) % (2 * mod); //holy shit variable names, Bat man!
        float b = a - mod;
        float c = Math.abs(b);
        float d = mod - c;
        return d;
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

    public static  interface Callback<V>{
        void Run(V parameter);
    }

    public static void checkNetworkAvailability(final Context context, final Callback<Boolean> action) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                ConnectivityManager cm = (ConnectivityManager)context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    try {
                        URL url = new URL("http://www.google.com/");
                        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
                        urlc.setRequestProperty("User-Agent", "test");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(1000); // mTimeout is in seconds
                        urlc.connect();
                        return urlc.getResponseCode() == 200;
                    } catch (IOException e) {
                        Log.i("warning", "Error checking internet connection", e);
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean connected) {
                action.Run(connected);
            }
        }.execute();

    }

    public static long TimeSince(long startTimeMillis) {
        return System.currentTimeMillis() - startTimeMillis;
    }

    public static int GetScreenWidth(Activity ctxt){
        return GetScreenSize(ctxt).x;
    }
    public static int GetScreenHeight(Activity ctxt){
        return GetScreenSize(ctxt).y;
    }

    public static Point GetScreenSize(Activity ctxt){
        Point size = new Point();
	    ctxt.getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }
}
