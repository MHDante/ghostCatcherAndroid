package ca.mixitmedia.ruhaunted.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import ca.mixitmedia.ruhaunted.app.Tools.Tools;

public class gcLocationManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    MainActivity gcMain;

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

    public gcLocationManager(MainActivity context) {
        this.gcMain = context;
        mLocationClient = new LocationClient(context, this, this);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }
    protected void onStart() {
        mLocationClient.connect();
    }
    public void onPause(){
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
    }
    public void onResume(){
        if (mLocationClient.isConnected()) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }
    protected void onStop() {
        mLocationClient.disconnect();
    }
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(gcMain, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(gcMain, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        gcMain, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 8135;

    public void setGPSUpdates(int ms) {
        mLocationRequest.setInterval(ms);
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    //@Override
    //protected void onActivityResult(
    //        int requestCode, int resultCode, Intent data) {
    //    // Decide what to do based on the original request code
    //    switch (requestCode) {
//
    //        case CONNECTION_FAILURE_RESOLUTION_REQUEST :
    //        /*
    //         * If the result code is Activity.RESULT_OK, try
    //         * to connect again
    //         */
    //            switch (resultCode) {
    //                case Activity.RESULT_OK :
    //                /*
    //                 * Try the request again
    //                 */
//
    //                    break;
    //            }
//
    //    }
//
    //}

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        gcMain.experienceManager.UpdateLocation(location);
        if (Tools.Current() == Tools.rfDetector) Tools.rfDetector.onLocationChanged(location);
    }
    //private boolean servicesConnected() {
    //    // Check that Google Play services is available
    //    int resultCode =
    //            GooglePlayServicesUtil.
    //                    isGooglePlayServicesAvailable(gcMain);
    //    // If Google Play services is available
    //    if (ConnectionResult.SUCCESS == resultCode) {
    //        // In debug mode, log the status
    //        Log.d("Location Updates",
    //                "Google Play services is available.");
    //        // Continue
    //        return true;
    //        // Google Play services was not available for some reason
    //    } else {
    //        showErrorDialog(resultCode);
//
    //        return false;
    //    }
    //}

    private void showErrorDialog(int resultCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                resultCode,
                gcMain,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment =
                    new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(
                    gcMain.getFragmentManager(),
                    "Location Updates");
        }
    }
}