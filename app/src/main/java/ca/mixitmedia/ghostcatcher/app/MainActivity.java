package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import ca.mixitmedia.ghostcatcher.app.Tools.*;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcActionManager;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcTrigger;
import ca.mixitmedia.ghostcatcher.utils.Tuple;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;


public class MainActivity extends Activity implements
        LocationListener, View.OnClickListener {

    static final boolean debugging = false;
    public static int debugLoc = 2;


    public static boolean transitionInProgress;
    public Map<Class, ToolLightButton> ToolMap;

    Location mCurrentLocation;
    public AnimationDrawable gearsBackground;


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private ProgressDialog mProgressDialog;

    private static String url = "http://mhdante.com/mixitmedia.zip";
    private File fileDir = new File(Environment.getExternalStorageDirectory(), "/mixitmedia");
    String unzipLocation = Environment.getExternalStorageDirectory() + "/";
    private String zipFile = Environment.getExternalStorageDirectory() + "/mixitmedia.zip";

    //////////////////LifeCycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcEngine.init(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        gearsBackground = (AnimationDrawable) findViewById(R.id.gearsbg).getBackground();
        ToolMap = new HashMap<Class, ToolLightButton>() {{
            put(Communicator.class, getToolLight(Communicator.class, R.id.tool_light_left));
            put(Journal.class, getToolLight(Journal.class, R.id.tool_light_right));
            put(LocationMap.class, getToolLight(LocationMap.class, R.id.tool_light_1));
            put(Biocalibrate.class, getToolLight(Biocalibrate.class, R.id.tool_light_2));
            put(Amplifier.class, getToolLight(Amplifier.class, R.id.tool_light_3));
            put(Tester.class, getToolLight(Tester.class, R.id.tool_light_4));
            put(Imager.class, getToolLight(Imager.class, R.id.tool_light_5));
        }};

        if (savedInstanceState == null) {  //Avoid overlapping fragments.
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getTool(Biocalibrate.class))
                    .commit();
        }
        handleIntent(getIntent());
        onLocationChanged(null);
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private int swipe_Min_Distance = 100;
            private int swipe_Max_Distance = 350;
            private int swipe_Min_Velocity = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {

                final float xDistance = Math.abs(e1.getX() - e2.getX());
                final float yDistance = Math.abs(e1.getY() - e2.getY());

                velocityX = Math.abs(velocityX);
                velocityY = Math.abs(velocityY);
                boolean result = false;

                if (velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance) {
                    if (e1.getX() > e2.getX()) {
                        if (!(getCurrentFragment() instanceof Journal))
                            onClick(ToolMap.get(Journal.class));
                    } else {
                        if (!(getCurrentFragment() instanceof Communicator))
                            onClick(ToolMap.get(Communicator.class));
                    }
                    result = true;
                } else if (velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance) {
                    if (e1.getY() > e2.getY()) {
                        if (toolHolderShown && !transitionInProgress)
                            toggleToolMenu();
                    } else {
                        if (!toolHolderShown && !transitionInProgress)
                            toggleToolMenu();
                    }
                    result = true;
                }
                return result;
            }
        });

        /////////////////////////////////////////////////////////////////////////////////////////////////
        File file = new File(zipFile);


        if (!fileDir.exists()) {

            if (file.exists()) {
                try {
                    unzip();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "File doesn't exist. Downloading...", Toast.LENGTH_LONG).show();
                // Trigger Async Task (onPreExecute method)
                new DownloadZipFile().execute(url);

            }
        } else {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    //-This is method is used for Download Zip file from server and store in Desire location.
    class DownloadZipFile extends AsyncTask<String, String, String> {
        boolean result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Downloading file. Please wait...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // Get Music file length
                int lengthOfFile = connection.getContentLength();
                // input stream to read file - with 8k buffer

                InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);

                // Output stream to write file in SD card
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/mixitmedia.zip");

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress which triggers onProgressUpdate method
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // Write data to file
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();
                // Close streams
                output.close();
                input.close();

                //Update flag when done
                result = true;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            if (result) {
                try {
                    unzip();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    //This is the method for unzip file which is store your location. And unzip folder will                 store as per your desire location.
    public void unzip() throws IOException {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Extracting the downloaded file...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        new UnZipTask().execute(zipFile, unzipLocation);
    }


    private class UnZipTask extends AsyncTask<String, Void, Boolean> {
        private int isExtracted = 0;

        @Override
        protected Boolean doInBackground(String... params) {

            String filePath = params[0];
            Log.d("FILE PATH IS", filePath);
            String destinationPath = params[1];
            Log.d("DEST PATH IS", destinationPath);

            File archive = new File(filePath);
            try {
                ZipFile zipfile = new ZipFile(archive);
                int fileCount = zipfile.size();
                mProgressDialog.setMax(zipfile.size());
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    unzipEntry(zipfile, entry, destinationPath);
                    isExtracted++;
                    mProgressDialog.setProgress((isExtracted * 100) / fileCount);
                }

                UnzipUtil d = new UnzipUtil(zipFile, unzipLocation);
                d.unzip();

            } catch (Exception e) {
                return false;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
        }


        private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

            if (entry.isDirectory()) {
                createDir(new File(outputDir, entry.getName()));
                return;
            }

            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                createDir(outputFile.getParentFile());
            }

            // Log.v("", "Extracting: " + entry);
            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        }

        private void createDir(File dir) {
            if (dir.exists()) {
                return;
            }
            if (!dir.mkdirs()) {
                throw new RuntimeException("Can not create dir " + dir);
            }
        }
    }

    public class UnzipUtil {
        private String zipFile;
        private String location;

        public UnzipUtil(String zipFile, String location) {
            this.zipFile = zipFile;
            this.location = location;

            dirChecker("");
        }

        public void unzip() {
            try {
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    Log.v("Decompress", "Unzipping " + ze.getName());

                    if (ze.isDirectory()) {
                        dirChecker(ze.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(location + ze.getName());

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                        fout.close();

                        zin.closeEntry();

                    }
                }
                zin.close();
            } catch (Exception e) {
                Log.e("Decompress", "unzip", e);
            }
        }

        private void dirChecker(String dir) {
            File f = new File(location + dir);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    GestureDetector detector;

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    private <T extends ToolFragment> ToolLightButton getToolLight(Class<T> cls, int resID) {
        ToolLightButton ret = (ToolLightButton) findViewById(resID);
        try {
            ret.setToolFragment(cls.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ret.setSrc(BitmapFactory.decodeResource(getResources(), ret.getToolFragment().getGlyphID()));
        ret.setEnabled(true);
        ret.setOnClickListener(this);
        return ret;
    }

    @Override
    protected void onResume() {
        gcAudio.play();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (toolHolderShown) toggleToolMenu();
            }
        }, 500);
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.nfc.action.TECH_DISCOVERED")) {
            ShowTool(Biocalibrate.class);
            debugLoc = 1;
            onLocationChanged(mCurrentLocation);
        }
    }

    private Handler decorViewHandler = new Handler();
    private boolean useDecorView;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && useDecorView) {
            decorViewHandler.post(decor_view_settings);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onClick(View view) {
        //get current fragment
        if (transitionInProgress) return;
        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        //todo: abstract
        if (tf.checkClick(view)) return;

        if (view instanceof ToolLightButton) {

            final ToolLightButton button = (ToolLightButton) view;
            if (tf.getClass() == Communicator.class && button.getToolFragment() == tf) {
                finish();
                return;
            }
            if (button.isEnabled() && !button.isSelected()) {
                if (toolHolderShown) {
                    toggleToolMenu();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swapTo(button.getToolFragment().getClass());
                        }
                    }, 400);
                } else {
                    swapTo(button.getToolFragment().getClass());
                }

            }
        }

        switch (view.getId()) {
            case R.id.tool_holder_tab:
                toggleToolMenu();
                break;

        }
    }

    boolean toolHolderShown = true;

    private void toggleToolMenu() {

        View toolHolder = findViewById(R.id.tool_holder);
        if (toolHolderShown) {
            toolHolder.animate().translationY((toolHolder.getMeasuredHeight() / 7) * -6);
            findViewById(R.id.journal_gear).animate().rotationBy(360);
            findViewById(R.id.back_gear).animate().rotationBy(-360);
        } else {
            toolHolder.animate().translationY(0);
            findViewById(R.id.journal_gear).animate().rotationBy(-360);
            findViewById(R.id.back_gear).animate().rotationBy(360);
        }
        toolHolderShown = !toolHolderShown;
    }

    @Override
    public void onBackPressed() {
        Log.d("Main", "OnBackPressed");
        Fragment f = getCurrentFragment();
        if (f instanceof Communicator) {
            super.onBackPressed();
        } else {
            onClick(ToolMap.get(Communicator.class));
        }
    }


    @Override
    protected void onPause() {
        if (gcAudio.isPlaying()) gcAudio.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        gcAudio.stop();
        super.onDestroy();
    }

    public void swapTo(Class toolType) {
        if (ToolMap.containsKey(toolType)) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, getTool(toolType))
                    .commit();
        } else throw new RuntimeException("That Class is not a Tool, You Tool!");

    }


    public void startDialog(String dialog) {

    }

    public void prepareLocation() {

    }

    public void triggerLocation() {
        gcTrigger trigger = gcEngine.Access().getCurrentSeqPt().getAutoTrigger();
        if (trigger == null) gcEngine.Access().getCurrentSeqPt().getTrigger(currentLocation);
        if (trigger != null) {
            trigger.activate(actionManager);
        }

    }

    public gcActionManager actionManager = new gcActionManager() {
        @Override
        public void startDialog(String dialogId) {
            getTool(Communicator.class).loadfile(dialogId);
        }

        @Override
        public void enableTool(String toolName) {
            for (Class c : ToolMap.keySet()) {
                if (c.getSimpleName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().equals(toolName.toLowerCase())) {
                    ToolMap.get(c).setEnabled(true);
                    return;
                }
            }
            throw new RuntimeException("Could not Find Tool: " + toolName);
        }

        @Override
        public void disableTool(String toolName) {
            for (Class c : ToolMap.keySet()) {
                if (c.getSimpleName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().equals(toolName.toLowerCase())) {
                    ToolMap.get(c).setEnabled(false);
                    return;
                }
            }
            throw new RuntimeException("Could not Find Tool: " + toolName);
        }
    };


    public void hideGears(boolean back, boolean journal) {
        View backGear = findViewById(R.id.back_gear);
        View journalGear = findViewById(R.id.journal_gear);
        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        if (back) backGear.animate().translationX(-200);
        if (journal) journalGear.animate().translationX(200);
    }

    public void showGears() {
        View backGear = findViewById(R.id.back_gear);
        View journalGear = findViewById(R.id.journal_gear);

        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        backGear.animate().translationX(0);
        journalGear.animate().translationX(0);
    }

    //GOOGLE SERVICES CODE


    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        //TODO: Error Handling
        return;
    }


    private gcLocation currentLocation;

    public gcLocation getCurrentLocation() {
        return currentLocation;
    }

    //
    @Override
    public void onLocationChanged(Location location) {

        if (location == null && !debugging) {
            currentLocation = null;
            return;
        }
        List<gcLocation> locations = gcEngine.Access().getCurrentSeqPt().getLocations();
        boolean hit = false;

        if (debugging) {
            currentLocation = locations.get(debugLoc);
            hit = true;
        } else {
            float accuracy = location.getAccuracy();
            for (gcLocation l : locations) {
                float distance[] = new float[3]; // ugh, ref parameters.
                Location.distanceBetween(l.getLatitude(), l.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                if (distance[0] < accuracy) {
                    currentLocation = l;
                    hit = true;
                }
            }
        }

        if (hit) {
            ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
            if (tf instanceof LocationMap) {
                LocationMap m = (LocationMap) tf;
                for (gcLocation l : m.locations) {
                    if (l == currentLocation) {
                        m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2));
                    } else {
                        m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
                    }
                }
            }
        }

    }

    public void ShowTool(Class tool) {
        ToolMap.get(tool).setEnabled(true);
    }

    public void HideTool(Class tool) {
        ToolMap.get(tool).setEnabled(false);
    }

    private final int NOTIF_ID = 2013567;

    public void showLocationNotification() {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ghost)
                        .setContentTitle("Ghost Catcher")
                        .setContentText("You have arrived to your next location!");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID, mBuilder.getNotification());      //Oh come on, google, one is depreciated, the other is unsupported. :(
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //todo:implement
    }

    @Override
    public void onProviderEnabled(String provider) {
        //todo:implement
    }

    @Override
    public void onProviderDisabled(String provider) {
        //todo:implement
    }

    public Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public <T extends ToolFragment> T getCurrentToolFragment(Class<T> cls) {
        Fragment tf = getCurrentFragment();
        if (tf.getClass().equals(cls)) {
            return cls.cast(tf);
        }
        return null;
    }

    public <T extends ToolFragment> boolean isToolEnabled(Class<T> cls) {
        if (ToolMap.containsKey(cls)) {
            return ToolMap.get(cls).isEnabled();
        } else throw new RuntimeException("Tool not Found");
    }

    public <T extends ToolFragment> T getTool(Class<T> cls) {
        if (ToolMap.containsKey(cls)) {
            return cls.cast(ToolMap.get(cls).getToolFragment());
        } else return null;
    }


    ///////////////////////////DECOR VIEW CODE

    private Runnable decor_view_settings = new Runnable() {
        public void run() {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (useDecorView && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            decorViewHandler.postDelayed(decor_view_settings, 500);
        }
        return super.onKeyDown(keyCode, event);
    }
}
