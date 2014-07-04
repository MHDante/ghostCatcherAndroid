package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Alexander on 2014-06-17
 */

public class RFDetector extends ToolFragment implements SensorEventListener {

	/**
	 * references to the UI elements
	 */
	TextView latitudeTextView;
    TextView longitudeTextView;
    TextView compassTextView;
	TextView destinationProximityTextView;

	ImageView arrowImageView;

	ProgressBar proximityBar;
	/**
	 * SensorManager is used to register/unregister this class as a SensorEventListener
	 * @see <a href="http://developer.android.com/reference/android/hardware/SensorManager.html">SensorManager</a>
	 * @see <a href="http://developer.android.com/reference/android/hardware/SensorEventListener.html">SensorEventListener</a>
	 */
	SensorManager sensorManager;

    /**
     * The angle between magnetic north and the front of the device
     * Range: [0,360), increasing clockwise from North
     *
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float heading;


    /**
     * The angle between magnetic north and the destination.
     * Range: [0,360), increasing clockwise from North
     *
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float bearing;

    /**
     * //the angle between the heading and the bearing
     * Range: [0,360), increasing clockwise from North
     *
     * @see <a href="http://imgur.com/Y3KXHyn">Helpful Diagram</a>
     */
    float relativeBearing;


	/**
	 * Distance to the destination in meters
	 */
	float proximity;


	/**
	 * debug
	 */
	Location destination;

    Map<String, Uri> imageFileLocationMap;

    public RFDetector(){
        createImageURIs();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_rf, container, false);

        latitudeTextView = (TextView) view.findViewById(R.id.latitude);
        longitudeTextView = (TextView) view.findViewById(R.id.longitude);
        compassTextView = (TextView) view.findViewById(R.id.compassText);
	    destinationProximityTextView = (TextView) view.findViewById(R.id.destinationProximityText);

	    arrowImageView = (ImageView) view.findViewById(R.id.arrowImage);

	    proximityBar = (ProgressBar) view.findViewById(R.id.proximityBar);

	    sensorManager = (SensorManager) gcMain.getSystemService(Context.SENSOR_SERVICE);

	    destination = new Location("dummyProvider");
	    destination.setLatitude(43.652202);
	    destination.setLongitude(-79.5814);

	    //set initial data right away, if available
	    gcMain.setGPSUpdates(3000, 0); //TODO: set to be distance dependant
	    Location currentLocation = gcMain.getCurrentGPSLocation();
	    if (currentLocation != null) {
		    System.out.println("Cached location loaded");
		    onLocationChanged(currentLocation);
	    }



        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        ImageView compass_arrow = (ImageView) view.findViewById(R.id.arrowImage);
        ImageView background = (ImageView) view.findViewById(R.id.background);

        overlay.setImageURI(imageFileLocationMap.get("overlay"));
        compass_arrow.setImageURI(imageFileLocationMap.get("compass_arrow"));
        background.setImageURI(imageFileLocationMap.get("background"));

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
		gcMain.setGPSUpdates(3000, 0); //TODO: set to be distance dependant
	}

	/**
	 * Unregisters this fragment to pause receiving sensor data
	 */
	@Override
    public void onPause() {
        sensorManager.unregisterListener(this);    //unregister listener for sensors
		gcMain.requestSlowGPSUpdates(); //slow down gps updates
		super.onPause();
    }

    @Override
    public Uri getGlyphUri() {
        return (imageFileLocationMap.get("rf_button_glyph"));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //stub
    }

	/**
	 * Reads the deprecated (argh!) orientation pseudo-sensor to get device heading
	 *
	 * @param event
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
     * @param newHeading the new heading, range: [0, 360), increasing clockwise from North
     */
    private void updateHeading(float newHeading) {
        newHeading = Math.round(newHeading);
        compassTextView.setText("Heading: " + Float.toString(newHeading) + " degrees");
        //float newRelativeBearing = Math.round(-(heading - bearing) % 360);

        // proven working:
        float newRelativeBearing = Math.round((newHeading - bearing + 360) % 360);

        // create a rotation animation (reverse turn newHeading degrees)
        RotateAnimation ra = new RotateAnimation(
                -relativeBearing,
                newRelativeBearing,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
	    arrowImageView.startAnimation(ra);
	    heading = -newHeading;
        relativeBearing = newRelativeBearing;
    }

	/**
	 * called by the onLocationChanged of the parent MainActivity
	 * @param location of the user's device
	 */
	public void onLocationChanged(Location location) {
        // called when the listener is notified with a location update from the GPS
        bearing = (location.bearingTo(destination) + 360) % 360;
		proximity = destination.distanceTo(location);

		//TODO: copy proximity stuff

		destinationProximityTextView.setText("Proximity: " + proximity + " m");
		latitudeTextView.setText("Lat: " + location.getLatitude() + "°");
		longitudeTextView.setText("Long: " + location.getLongitude()+"°");
	}

    public void createImageURIs(){
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String,Uri>(){{
            put("overlay", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_overlay.png").build());
            put("compass_arrow", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_arrow.png").build());
            put("background", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_background.png").build());
            put("rf_button_glyph", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_rf_detector.png").build());

            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }
}
