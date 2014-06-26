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
    //public AnimationDrawable gearsBackground;

    //////////////////LifeCycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcEngine.init(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        //gearsBackground = (AnimationDrawable) findViewById(R.id.gearsbg).getBackground();
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
    }

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
