package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.Tools.*;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcActionManager;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcTrigger;


public class MainActivity extends Activity implements
        LocationListener {

    static final boolean debugging = false;
    public static int debugLoc = 2;

    public static boolean transitionInProgress;
    Map<Class, ToolFragment> ToolMap;
    Location mCurrentLocation;
    public AnimationDrawable gearsBackground;

    //////////////////LifeCycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcEngine.init(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        gearsBackground = (AnimationDrawable) findViewById(R.id.activity_bg).getBackground();

        ToolMap = new HashMap<Class, ToolFragment>() {{
            put(Communicator.class, Communicator.newInstance("Settings"));
            put(Journal.class, Journal.newInstance("Settings"));
            put(LocationMap.class, LocationMap.newInstance("Settings"));
            put(Biocalibrate.class, Biocalibrate.newInstance("Settings"));
            put(Amplifier.class, Amplifier.newInstance("Settings"));
            put(Tester.class, Tester.newInstance("Settings"));
            put(Imager.class, Imager.newInstance("Settings"));
            //
            //
        }};

        if (savedInstanceState == null) {  //Avoid overlapping fragments.
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getTool(Biocalibrate.class))
                    .commit();
        }
        handleIntent(getIntent());
        onLocationChanged(null);
    }

    @Override
    protected void onResume() {
        gcAudio.play();
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.nfc.action.TECH_DISCOVERED")) {
            ShowTool("biocalib");
            debugLoc = 1;
            onLocationChanged(mCurrentLocation);
        }
    }

    private Handler decorViewHandler = new Handler();

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
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
        switch (view.getId()) {
            case R.id.back_gear_btn:
                //hideGears("back");
                onBackPressed();
                break;
            case R.id.journal_gear_btn:
                swapTo(Journal.class, true);
                break;

        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            //Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
            findViewById(R.id.journal_gear).setVisibility(0);
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
            //startActivity(new Intent(this, ImagerFragment.class));

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

    public void swapTo(Class toolType, boolean addToBackStack) {
        if (ToolMap.containsKey(toolType)) {
            if (addToBackStack) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ToolMap.get(toolType))
                        .addToBackStack(null)
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ToolMap.get(toolType))
                        .commit();
            }
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

    public void ShowTool(String tool) {
        getTool(Communicator.class).ShowTool(tool);
    }

    public void HideTool(String tool) {
        getTool(Communicator.class).HideTool(tool);
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

    public <T extends ToolFragment> T CurrentToolFragment(Class<T> cls) {
        FragmentManager f = getFragmentManager();
        Fragment ff = f.findFragmentById(R.id.fragment_container);
        Class c = ff.getClass();
        if (c == cls) {
            return cls.cast(getFragmentManager().findFragmentById(R.id.fragment_container));
        }
        return null;
    }

    public <T extends ToolFragment> T getTool(Class<T> cls) {
        if (ToolMap.containsKey(cls)) {
            return cls.cast(ToolMap.get(cls));
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            decorViewHandler.postDelayed(decor_view_settings, 500);
        }
        return super.onKeyDown(keyCode, event);
    }
}

