package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Alexander on 2014-06-17
 */

public class RFDetector extends ToolFragment implements SensorEventListener {

    /**
     * references to the UI elements
     */
    TextView destinationProximityTextView;

    ImageView backgroundImageView;
    ImageView arrowImageView;
    ImageView lidImageView;

    boolean backgroundFlashingState;
    boolean toolState;

    ProgressBar proximityBar;

    /**
     * SensorManager is used to register/unregister this class as a SensorEventListener
     *
     * @see <a href="http://developer.android.com/reference/android/hardware/SensorManager.html">SensorManager</a>
     * @see <a href="http://developer.android.com/reference/android/hardware/SensorEventListener.html">SensorEventListener</a>
     */
    SensorManager sensorManager;

    Vibrator vibrator;
    Handler vibrationHandler = new Handler();
    Handler flashHandler = new Handler();
    Runnable flashRunnable;
    Runnable vibrationRunnable;
    int vibrationIntervalMS;

    /**
     * The angle between magnetic north and the front of the device
     * Range: [0,360), increasing clockwise from North
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float heading;


    /**
     * The angle between magnetic north and the destination.
     * Range: [0,360), increasing clockwise from North
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float bearing;

    /**
     * //the angle between the heading and the bearing
     * Range: [0,360), increasing clockwise from North
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float relativeBearing;


    /**
     * Distance to the destination in meters
     */
    float proximity;
    ApproxDistance approxDistance;
    /**
     * debug
     */
    Location destination;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        sensorManager = (SensorManager) gcMain.getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) gcMain.getSystemService(Context.VIBRATOR_SERVICE);

        View view = inflater.inflate(R.layout.tool_rf, container, false);

        destinationProximityTextView = (TextView) view.findViewById(R.id.destinationProximityText);

        backgroundImageView = (ImageView) view.findViewById(R.id.rf_background);
        arrowImageView = (ImageView) view.findViewById(R.id.rf_arrow);

        lidImageView = (ImageView) view.findViewById(R.id.rf_lid);
        lidImageView.setVisibility(View.VISIBLE);

        proximityBar = (ProgressBar) view.findViewById(R.id.proximityBar);
        proximityBar.setMax(1000);

        vibrationRunnable = new Runnable()  {
            @Override
            public void run() {
                vibrator.vibrate(50);
                if (toolState) vibrationHandler.postDelayed(this, vibrationIntervalMS +500);
            }
        };

        //destination = new Location("dummyProvider");
        //destination.setLatitude(43.652202);
        //destination.setLongitude(-79.5814);
        destination = gcMain.gcEngine.getAllLocations().get("lake_devo");
        approxDistance = ApproxDistance.CLOSE; //TODO: why is this here?

        //set initial data right away, if available
        gcMain.locationManager.setGPSUpdates(3000, 0);

        return view;
    }

    /**
     * Registers this fragment to resume receiving sensor data
     */
    @Override
    public void onResume() {
        super.onResume();
        //register listener for the sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        gcMain.locationManager.setGPSUpdates(0, 0);
        gcMain.locationManager.setGPSStatus();
        //updateDestination();
    }

    /**
     * Unregisters this fragment to pause receiving sensor data
     */
    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);    //unregister listener for sensors
        gcMain.locationManager.requestSlowGPSUpdates(); //slow down gps updates
        setGPSState(false, false);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //stub
    }

    /**
     * Reads the deprecated (argh!) orientation pseudo-sensor to get device heading
     *
     * @param event the SensorEvent object with all the data goodies
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // get the angle around the z-axis rotated
            updateHeading(event.values[0]);
        }
    }

    /**
     * updates heading of the device and rotates the arrow
     *
     * @param newHeading the new heading, range: [0, 360), increasing clockwise from North
     */
    private void updateHeading(float newHeading) {
	    if (!toolState) return;
        newHeading = Math.round(newHeading);

        float newRelativeBearing = Math.round((newHeading - bearing + 360) % 360);

        // create a rotation animation (reverse turn newHeading degrees)
        RotateAnimation ra = new RotateAnimation(
                -relativeBearing,
                newRelativeBearing,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);// set the animation after the end of the reservation status
        arrowImageView.startAnimation(ra);


	    vibrationIntervalMS =  5000 - 5000 * (int)Math.abs(relativeBearing - 180) / 180;
	    vibrationIntervalMS = Math.max(vibrationIntervalMS, 200);
        heading = -newHeading;
        relativeBearing = newRelativeBearing;
    }

    /**
     * called by the onLocationChanged of the parent MainActivity
     *
     * @param location of the user's device
     */
    public void onLocationChanged(Location location) {
        if (location == null || getView() == null) {
            Log.d("RF", "Locations shouldn't be null, you dun fucked up.");
            return;
        }
        setGPSState(true, false);
        bearing = (location.bearingTo(destination) + 360) % 360;
        proximity = location.distanceTo(destination);
        proximityBar.setProgress(1000 - (int) proximity);

        ApproxDistance currentDistance;
        if (proximity >= 1000) {
            currentDistance = ApproxDistance.FAR_FAR_AWAY;
        } else if (proximity >= 250) {
            currentDistance = ApproxDistance.FAR;
        } else if (proximity >= 100) {
            currentDistance = ApproxDistance.MEDIUM;
        } else if (proximity >= 25) {
            currentDistance = ApproxDistance.CLOSE;
        } else {
            currentDistance = ApproxDistance.THERE;
        }

        //if a change in distance interval has changed, GPS will be reconfigured.
        if (approxDistance != currentDistance) {
            switch (currentDistance) {
                case FAR_FAR_AWAY:
                    gcMain.locationManager.setGPSUpdates(60000, 100); //60 seconds, 100 meters
                    Log.d("RFDetector", "setGPSUpdates(60000, 100)");
                    break;
                case FAR:
                    gcMain.locationManager.setGPSUpdates(30000, 25); //30 seconds, 25 meters
                    Log.d("RFDetector", "setGPSUpdates(30000, 25)");
                    break;
                case MEDIUM:
                    gcMain.locationManager.setGPSUpdates(10000, 10); //10 seconds, 10 meters
                    Log.d("RFDetector", "setGPSUpdates(10000, 10)");
                    break;
                case CLOSE:
                    gcMain.locationManager.setGPSUpdates(0, 0); //0 seconds, 0 meters
                    Log.d("RFDetector", "setGPSUpdates(0,0)");
                    break;
                case THERE:
                    gcMain.locationManager.setGPSUpdates(0, 0); //0 seconds, 0 meters
                    Log.d("RFDetector", "We're here bitches!"); //TODO:Are we here? or are we THERE?
                    destinationProximityTextView.setText("#Location Reached#"); //TODO: use story terminology
                    flashRunnable = new Runnable()  {
                        int ghettoStateMachine;
                        @Override
                        public void run() {
                            if(ghettoStateMachine++%2==0){
                                backgroundImageView.setColorFilter(0x33FF0000);
                            }else {
                                backgroundImageView.setColorFilter(0x00000000);
                            }
                            if (ghettoStateMachine < 6 ) flashHandler.postDelayed(this, 500);
                            else{

                                gcMain.experienceManager.ToolSuccess(RFDetector.this);
                                gcMain.swapTo(Tools.communicator);
                            }
                        }
                    };
                    flashRunnable.run();
                    break;
            }
            approxDistance = currentDistance;
        }

        destinationProximityTextView.setText("Proximity: " + Math.round(proximity) + " m");
    }

    /**
     * opens/closes the lid.
     *
     * @param state   true for open, false for closed.
     * @param instant true for instance animation, false for animation with duration
     */
    public void setGPSState (boolean state, boolean instant) {
        if (state == toolState) return;

        RotateAnimation ra;
        if (state) {
            ra = new RotateAnimation(0, 180,
                    Animation.RELATIVE_TO_SELF, 0.1797323136f,
                    Animation.RELATIVE_TO_SELF, 0.2093457944f);
            vibrationHandler.post(vibrationRunnable);
        }
        else {
            ra = new RotateAnimation(180, 0,
                    Animation.RELATIVE_TO_SELF, 0.1797323136f,
                    Animation.RELATIVE_TO_SELF, 0.2093457944f);
            destinationProximityTextView.setText("Location Unavailable");
            vibrationHandler.removeCallbacks(vibrationRunnable);
        }

        ra.setDuration(instant ? 0 : 1000); //sets duration to 1s or 0.
        ra.setFillAfter(true);// set the animation after the end of the reservation status
        lidImageView.startAnimation(ra);
        toolState = state;
    }

    public void updateDestination() {
        destination = gcMain.experienceManager.getDestination();
    }

    /**
     * An enumeration that stores the frequency with which location updates should be received.
     * Faster updates are necessary for accuracy at close proximity, but use significantly more
     * battery energy, and heats up the phone.
     */
    enum ApproxDistance {
        THERE, CLOSE, MEDIUM, FAR, FAR_FAR_AWAY
    }

}
