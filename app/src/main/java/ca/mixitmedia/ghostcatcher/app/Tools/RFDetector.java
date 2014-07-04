package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
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
	 * An enumeration that stores the frequency with which location updates should be recieved.
	 * Faster updates are neceassary for accuracy at close proximity, but use significantly more
	 * battery energy, and heats up the phone.
	 */
	enum ApproxDistance{CLOSE, MEDIUM, FAR, FAR_FAR_AWAY};
	ApproxDistance approxDistance;

	/**
	 * debug
	 */
	Location destination;

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
		proximityBar.setMax(1000);

		sensorManager = (SensorManager) gcMain.getSystemService(Context.SENSOR_SERVICE);

		destination = new Location("dummyProvider");
		destination.setLatitude(43.652202);
		destination.setLongitude(-79.5814);
		//updateDestination();

		//set initial data right away, if available
		gcMain.setGPSUpdates(3000, 0); //TODO: set to be distance dependant
		Location currentLocation = gcMain.getCurrentGPSLocation();
		if (currentLocation != null) {
			System.out.println("Cached location loaded");
			onLocationChanged(currentLocation);
		}

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
		gcMain.setGPSUpdates(0, 0);
		updateDestination();
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
	public int getGlyphID() {
		return (R.drawable.icon_rf_detector);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//stub
	}

	/**
	 * Reads the deprecated (argh!) orientation pseudo-sensor to get device heading
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
	 * @param newHeading the new heading, range: [0, 360), increasing clockwise from North
	 */
	private void updateHeading(float newHeading) {
		newHeading = Math.round(newHeading);
		compassTextView.setText("Heading: " + Float.toString(newHeading) + " degrees");

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
		proximityBar.setProgress(1000 - (int) proximity);

		ApproxDistance currentDistance;
		if (proximity >= 1000) {
			currentDistance = ApproxDistance.FAR_FAR_AWAY;
		}
		else if (proximity >= 250) {
			currentDistance = ApproxDistance.FAR;
		}
		else if (proximity >= 100) {
			currentDistance = ApproxDistance.MEDIUM;
		}
		else {
			currentDistance = ApproxDistance.CLOSE;
		}

		//if a change in distance interval has changed, GPS will be reconfigured.
		if (approxDistance != currentDistance) {
			switch (approxDistance) {
				case FAR_FAR_AWAY:
					gcMain.setGPSUpdates(60000, 100); //60 seconds, 100 meters
					Log.d("RFDectector", "setGPSUpdates(60000, 100)");
					break;
				case FAR:
					gcMain.setGPSUpdates(30000, 25); //30 seconds, 25 meters
					Log.d("RFDectector", "setGPSUpdates(30000, 25)");
					break;
				case MEDIUM:
					gcMain.setGPSUpdates(10000, 10); //10 seconds, 10 meters
					Log.d("RFDectector", "setGPSUpdates(10000, 10)");
					break;
				case CLOSE:
					gcMain.setGPSUpdates(0, 0); //0 seconds, 0 meters
					Log.d("RFDectector", "setGPSUpdates(0,0)");
					break;
			}
			approxDistance = currentDistance;
		}

		destinationProximityTextView.setText("Proximity: " + proximity + " m");
		latitudeTextView.setText("Lat: " + location.getLatitude() + "°");
		longitudeTextView.setText("Long: " + location.getLongitude()+"°");
	}

	/**
	 * Sets the destination to the story's destination.
	 */
	public void updateDestination() {
		destination = gcMain.getPlayerLocationInStory();
	}
}