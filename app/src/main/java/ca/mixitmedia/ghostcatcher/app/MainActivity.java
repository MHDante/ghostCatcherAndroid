package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.utils.Debug;
import ca.mixitmedia.ghostcatcher.utils.Tuple;


public class MainActivity extends Activity implements
        ToolFragment.ToolInteractionListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    float gearsize = 200;
    View backGear;
    View journalGear;
    Context ctxt; //TODO:REMOVE.
    View fragmentContainer;
    CommunicatorFragment communicator;
    JournalFragment journal;
    gcMap map;
    BiocalibrateFragment biocalib;
    AmplifierFragment amplifier;
    TesterFragment tester;
    ImagerFragment imager;

    private LocationClient mLocationClient;
    Location mCurrentLocation;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private LocationRequest mLocationRequest;


    Map<String, Tuple<Tuple<Double, Double>, Tuple<Double, Double>>> audioMap = new HashMap<String, Tuple<Tuple<Double, Double>, Tuple<Double, Double>>>() {{
        this.put("gc_0_0", new Tuple<Tuple<Double, Double>, Tuple<Double, Double>>(new Tuple<Double, Double>(43.658331051519916, -79.37825549063609), new Tuple<Double, Double>(43.659997833617425, -79.37679655432811)));
        this.put("gc_0_1", new Tuple<Tuple<Double, Double>, Tuple<Double, Double>>(new Tuple<Double, Double>(43.65814476359927, -79.38022959647105), new Tuple<Double, Double>(43.659384650720966, -79.3784702527534)));
        this.put("gc_1_0_1", new Tuple<Tuple<Double, Double>, Tuple<Double, Double>>(new Tuple<Double, Double>(43.65648367075437, -79.37776196417735), new Tuple<Double, Double>(43.6583678399643, -79.37615282416454)));
        this.put("gc_1_0_2", new Tuple<Tuple<Double, Double>, Tuple<Double, Double>>(new Tuple<Double, Double>(43.6571046454204, -79.3801652234547), new Tuple<Double, Double>(43.657847785059126, -79.37900669455638)));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctxt = this;
        super.onCreate(savedInstanceState);
        gcEngine.getInstance().init(this);
        setContentView(R.layout.activity_main);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);
        //TODO: Implement settings, you lazy fool.

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationClient = new LocationClient(this, this, this);

        //initialize all the fragments
        communicator = CommunicatorFragment.newInstance("Settings");
        journal = JournalFragment.newInstance("Settings");
        map = gcMap.newInstance("Settings");
        biocalib = BiocalibrateFragment.newInstance("Settings");
        amplifier = AmplifierFragment.newInstance("Settings");
        tester = TesterFragment.newInstance("Settings");
        imager = ImagerFragment.newInstance("Settings");
        gcAudio.play();

        if (savedInstanceState != null) {
            return;//Avoid overlapping fragments.
        }

        //begin the transaction ok
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, communicator)
                        //.addToBackStack(null)
                .commit();

    }


    //This block should automatically sync? Maybe?
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGears();
                }
            }, 500);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();


    }

    public void onClick(View view) {
        //get current fragment
        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (tf.checkClick(view)) return;
        switch (view.getId()) {
            case R.id.back_gear_btn:
                //hideGears("back");
                onBackPressed();
                break;
            case R.id.journal_gear_btn:
                swapTo("journal");
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO: doStuff
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
            findViewById(R.id.journal_gear).setVisibility(0);
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
            //startActivity(new Intent(this, ImagerFragment.class));

        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (gcAudio.isPlaying()) gcAudio.pause();
        super.onDestroy();
    }

    public void swapTo(String fragment) {
        if (fragment.equals("journal")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, journal)
                    .addToBackStack(null)
                    .commit();
            findViewById(R.id.journal_gear).setVisibility(2);
            findViewById(R.id.journal_gear_btn).setVisibility(2);
        } else if (fragment.equals("map")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, map)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("imager")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, imager)
                    .addToBackStack(null)
                    .commit();
            findViewById(R.id.journal_gear).setVisibility(2);
            findViewById(R.id.journal_gear_btn).setVisibility(2);
        } else if (fragment.equals("communicator")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, communicator)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("biocalibrate")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, biocalib)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("tester")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, tester)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("amplifier")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, amplifier)
                    .addToBackStack(null)
                    .commit();
        }
    }
    @Override
    public void startDialog(String dialog) {
        getFragmentManager().popBackStack();
        findViewById(R.id.journal_gear).setVisibility(0);
        communicator.loadfile(dialog);
    }

    @Override
    public void startDialogByLocation(String dialog) {
        startDialog(currentLocation.audio);

        //for (String s : audioMap.keySet()) {
        //Tuple<Tuple<Double, Double>, Tuple<Double, Double>> boundries = audioMap.get(s);
        //double bottom = boundries.first.first;
        //double left = boundries.first.second;
        //double top = boundries.second.first;
        //double right = boundries.second.second;
//
//
        //if (latitude > bottom && latitude < top) {
        //    if (longitude > left && longitude < right) {
        //        startDialog(s);
        //        return;
        //    }
        //}
        //Log.d("Loc", "Bot: " + bottom + " Lat: " + latitude + " Top: " + top);
        //Log.d("Loc", "Left: " + left + " Long: " + longitude + " Right: " + right);


        //}
        //throw new RuntimeException("You're outta the zone. :" + latitude + longitude);

    }

    public void hideGears(String action) {
        AnimationHandler ah = new AnimationHandler(this, action);
        backGear.animate().setListener(ah);
        backGear.animate().translationX(-gearsize);
        journalGear.animate().translationX(gearsize);
    }

    public void showGears() {
        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        backGear.animate().translationX(0);
        journalGear.animate().translationX(0);
    }

    public void startJournal() {
        startActivity(new Intent(this, JournalFragment.class));
        overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
    }

    public void hideJournal(){
        findViewById(R.id.journal_gear).setVisibility(1);
    }

    class AnimationHandler extends AnimatorListenerAdapter {
        MainActivity caller;
        String action;

        AnimationHandler(MainActivity a, String action) {
            this.caller = a;
            this.action = action;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Debug.out("OnEnd");
            if (action.equals("back"))
                caller.onBackPressed();
            else if (action.equals("journal"))
                caller.startJournal();
            else if (action.equals("map")) {
                startActivity(new Intent(ctxt, gcMap.class));
                overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            }
            //else if (action.equals("tester")) {
            //    startActivity(new Intent(ctxt, TesterFragment.class));
            //    overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            //}
            else
                throw new RuntimeException("This system is being depreciated, and you passed an incorrect parameter");
        }
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

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
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
        //switch (requestCode) {
        //    case CONNECTION_FAILURE_RESOLUTION_REQUEST :
        //    /*
        //     * If the result code is Activity.RESULT_OK, try
        //     * to connect again
        //     */
        //        switch (resultCode) {
        //            case Activity.RESULT_OK :
        //            /*
        //             * Try the request again
        //             */
        //                break;
        //        }
        //}
    }

    private boolean servicesConnected(ConnectionResult connectionResult) {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        onLocationChanged(mCurrentLocation);
        Log.d("loc", "loc :" + mCurrentLocation);

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
        }
    }

    double longitude;
    double latitude;
    private gcLocation currentLocation;

    @Override
    public gcLocation getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

        List<gcLocation> locations = gcEngine.getInstance().getCurrentSeqPt().locations;
        boolean hit = false;
        float accuracy = location.getAccuracy();
        for (gcLocation l : locations) {
            float distance[] = new float[3]; // ugh, ref parameters.
            Location.distanceBetween(l.latitude, l.longitude, location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] < accuracy) {
                currentLocation = l;
                hit = true;
            }
        }
        if (hit) {
            ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
            if (tf instanceof gcMap) {
                gcMap m = (gcMap) tf;
                for (gcLocation l : m.locations) {
                    if (l == currentLocation) {
                        m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2));
                    } else {
                        m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
                    }
                }
            }
            showLocationNotification();
            communicator.bioCalib = true;
            ShowTool("biocalibrate");
        } else {
            HideTool("biocalibrate");
            communicator.bioCalib = false;
        }

    }

    private void ShowTool(String tool) {

        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (tf instanceof CommunicatorFragment) {
            CommunicatorFragment c = (CommunicatorFragment) tf;
            c.ShowTool(tool);
        }
    }

    private void HideTool(String tool) {
        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (tf instanceof CommunicatorFragment) {
            CommunicatorFragment c = (CommunicatorFragment) tf;
            c.HideTool(tool);
        }
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
        mNotificationManager.notify(NOTIF_ID, mBuilder.build());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

