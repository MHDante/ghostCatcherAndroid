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
        import android.os.Bundle;
        import android.os.Handler;
        import android.util.Log;
        import android.view.View;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GooglePlayServicesClient;
        import com.google.android.gms.common.GooglePlayServicesUtil;
        import com.google.android.gms.location.LocationClient;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;

        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import ca.mixitmedia.ghostcatcher.app.Tools.*;
        import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
        import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
        import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;


public class MainActivity extends Activity implements
        ToolFragment.ToolInteractionListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    float gearsize = 200;
    View backGear;
    View journalGear;

    Map<Class, ToolFragment> ToolMap;

    boolean debugging = true;
    int debugLoc = 2;

    private LocationClient mLocationClient;
    Location mCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gcEngine.getInstance().init(this);
        setContentView(R.layout.activity_main);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);
        //TODO: Implement settings, you lazy fool.

        mLocationClient = new LocationClient(this, this, this);

        ToolMap = new HashMap<Class, ToolFragment>(){{
            put(Communicator.class  ,Communicator.newInstance("Settings"));
            put(Journal     .class  ,Journal.newInstance("Settings"));
            put(LocationMap .class  ,LocationMap.newInstance("Settings"));
            put(Biocalibrate.class  ,Biocalibrate.newInstance("Settings"));
            put(Amplifier   .class  ,Amplifier.newInstance("Settings"));
            put(Tester      .class  ,Tester.newInstance("Settings"));
            put(Imager      .class  ,Imager.newInstance("Settings"));
        }};

        gcAudio.play();

        if (savedInstanceState != null) {
            return;//Avoid overlapping fragments.
        }

        //begin the transaction ok
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, getTool(Communicator.class))
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
        findViewById(R.id.fragment_container).setLayerType(View.LAYER_TYPE_HARDWARE, null);


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
                swapTo(Journal.class, true);
                break;

        }
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
        findViewById(R.id.fragment_container).setLayerType(View.LAYER_TYPE_NONE, null);
    }


    @Override
    protected void onDestroy() {
        if (gcAudio.isPlaying()) gcAudio.pause();
        super.onDestroy();
    }

    public void swapTo(Class toolType, boolean addToBackStack) {
        if (ToolMap.containsKey(toolType)){
            if(addToBackStack){
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ToolMap.get(toolType))
                        .addToBackStack(null)
                        .commit();
            }
            else{
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ToolMap.get(toolType))
                        .commit();
            }
        } else throw new RuntimeException("That Class is not a Tool, You Tool!");

    }

    public void startDialog(String dialog) {
        getTool(Communicator.class).loadfile(dialog);
    }

    @Override
    public void startDialogByLocation(String dialog) {
        startDialog(currentLocation.audio);
    }

    public void showGears() {
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

    private gcLocation currentLocation;

    @Override
    public gcLocation getCurrentLocation() {
        return currentLocation;
    }

    //
    @Override
    public void onLocationChanged(Location location) {

        if (location == null) { currentLocation = null; return; }
        List<gcLocation> locations = gcEngine.getInstance().getCurrentSeqPt().locations;
        boolean hit = false;

        if (debugging) {
            currentLocation = locations.get(debugLoc);
            hit = true;
        }
        else {
            float accuracy = location.getAccuracy();
            for (gcLocation l : locations) {
                float distance[] = new float[3]; // ugh, ref parameters.
                Location.distanceBetween(l.latitude, l.longitude, location.getLatitude(), location.getLongitude(), distance);
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
            showLocationNotification();
            getTool(Communicator.class).bioCalib = true;
            ShowTool("biocalibrate");
        } else {
            HideTool("biocalibrate");
            getTool(Communicator.class).bioCalib = false;
        }

    }

    private void ShowTool(String tool) {

        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (tf instanceof Communicator) {
            Communicator c = (Communicator) tf;
            c.ShowTool(tool);
        }
    }

    private void HideTool(String tool) {
        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (tf instanceof Communicator) {
            Communicator c = (Communicator) tf;
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

    public <T extends ToolFragment> T CurrentToolFragment(Class<T> cls){
        if (getFragmentManager().findFragmentById(R.id.fragment_container).getClass() == cls){
            return cls.cast(getFragmentManager().findFragmentById(R.id.fragment_container));
        }
        return null;
    }
    public <T extends ToolFragment> T getTool(Class<T> cls){
        if (ToolMap.containsKey(cls)) {
            return cls.cast(ToolMap.get(cls));
        }
        else return null;
    }
}

