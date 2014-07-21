package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
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
    /*debug
    TextView latitudeTextView;
	TextView longitudeTextView;
	TextView compassTextView;*/
	TextView destinationProximityTextView;

	ImageView backgroundImageView;
	ImageView arrowImageView;
	ImageView lidImageView;

	boolean backgroundFlashingState;
	boolean lidState;

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
	enum ApproxDistance{THERE, CLOSE, MEDIUM, FAR, FAR_FAR_AWAY}
	ApproxDistance approxDistance;

	/**
	 * debug
	 */
	Location destination;

    Map<String, Uri> imageFileLocationMap;

    public RFDetector(){
        createImageURIs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_rf, container, false);


		/*Debug
		latitudeTextView = (TextView) view.findViewById(R.id.latitude);
		longitudeTextView = (TextView) view.findViewById(R.id.longitude);
		compassTextView = (TextView) view.findViewById(R.id.compassText);*/
		destinationProximityTextView = (TextView) view.findViewById(R.id.destinationProximityText);

	    backgroundImageView = (ImageView) view.findViewById(R.id.rf_background);
		arrowImageView = (ImageView) view.findViewById(R.id.rf_arrow);
		ImageView overlay = (ImageView) view.findViewById(R.id.rf_overlay);
	    lidImageView = (ImageView) view.findViewById(R.id.rf_lid);
		lidImageView.setVisibility(View.VISIBLE);

	    backgroundImageView.setImageURI(imageFileLocationMap.get("rf_background"));
		arrowImageView.setImageURI(imageFileLocationMap.get("rf_arrow"));
	    overlay.setImageURI(imageFileLocationMap.get("rf_overlay"));
	    lidImageView.setImageURI(imageFileLocationMap.get("rf_lid"));

		proximityBar = (ProgressBar) view.findViewById(R.id.proximityBar);
		proximityBar.setMax(1000);

		sensorManager = (SensorManager) gcMain.getSystemService(Context.SENSOR_SERVICE);

		destination = new Location("dummyProvider");
		destination.setLatitude(43.652202);
		destination.setLongitude(-79.5814);

	    approxDistance = ApproxDistance.CLOSE;

		//set initial data right away, if available
		gcMain.setGPSUpdates(3000, 0);
		Location currentLocation = gcMain.getCurrentGPSLocation();
		if (currentLocation != null) {
			System.out.println("Stored location loaded");
			onLocationChanged(currentLocation);
			setLidState(true, true);
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
		//updateDestination();
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
        return (imageFileLocationMap.get("icon_rf_detector"));
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
		//compassTextView.setText("Heading: " + Float.toString(newHeading) + " degrees");

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
		heading = -newHeading;
		relativeBearing = newRelativeBearing;
	}

	/**
	 * called by the onLocationChanged of the parent MainActivity
	 * @param location of the user's device
	 */
	public void onLocationChanged(Location location) {
		if (location == null) {
			Log.d("RF", "Locations shouldn't be null, you dun fucked up.");
			return;
		}
		setLidState(true, false);
		bearing = (location.bearingTo(destination) + 360) % 360;
		proximity = location.distanceTo(destination);
		proximityBar.setProgress(1000 - (int) proximity);

		ApproxDistance currentDistance;
		if (proximity >= 1000) {
			currentDistance = ApproxDistance.FAR_FAR_AWAY;
		}
		else if (proximity >= 250) {
			currentDistance = ApproxDistance.FAR;
		}
		else if (proximity >= 100){
			currentDistance = ApproxDistance.MEDIUM;
		}
		else if (proximity >= 25) {
		currentDistance = ApproxDistance.CLOSE;
		}
		else {
			currentDistance = ApproxDistance.THERE;
		}

		//if a change in distance interval has changed, GPS will be reconfigured.
		if (approxDistance != currentDistance) {
			switch (currentDistance) {
				case FAR_FAR_AWAY:
					gcMain.setGPSUpdates(60000, 100); //60 seconds, 100 meters
					Log.d("RFDetector", "setGPSUpdates(60000, 100)");
					break;
				case FAR:
					gcMain.setGPSUpdates(30000, 25); //30 seconds, 25 meters
					Log.d("RFDetector", "setGPSUpdates(30000, 25)");
					break;
				case MEDIUM:
					gcMain.setGPSUpdates(10000, 10); //10 seconds, 10 meters
					Log.d("RFDetector", "setGPSUpdates(10000, 10)");
					break;
				case CLOSE:
					gcMain.setGPSUpdates(0, 0); //0 seconds, 0 meters
					Log.d("RFDetector", "setGPSUpdates(0,0)");
					break;
				case THERE:
					gcMain.setGPSUpdates(0, 0); //0 seconds, 0 meters
					Log.d("RFDetector", "We're here bitches!");
					destinationProximityTextView.setText("#Location Reached#"); //TODO: use story terminology
					new ProximityTest().execute();
					gcMain.swapTo(Communicator.class);
					break;
			}
			approxDistance = currentDistance;
		}

		if (currentDistance == ApproxDistance.THERE && !backgroundFlashingState) {
			backgroundImageView.setColorFilter(0x33FF0000);
			backgroundFlashingState = true;
		}
		else {
			backgroundImageView.setColorFilter(0x00000000);
			backgroundFlashingState = false;
		}

		/*debug
		latitudeTextView.setText("Lat: " + location.getLatitude() + "°");
		longitudeTextView.setText("Long: " + location.getLongitude() + "°");*/
		destinationProximityTextView.setText("Proximity: " + Math.round(proximity) + " m");
	}


	public void setGPSStatus(boolean gpsAvailablity) {
		setLidState(gpsAvailablity, false);
		destinationProximityTextView.setText("Location Unavailable");
	}

	/**
	 * opens/closes the lid.
	 * @param state true for open, false for closed.
	 * @param instant true for instance animation, false for animation with duration
	 */
	public void setLidState(boolean state, boolean instant) {
		if (state != lidState) {
			RotateAnimation ra;
			if (state)  ra = new RotateAnimation(0, 180,
						Animation.RELATIVE_TO_SELF, 0.180952381f,
						Animation.RELATIVE_TO_SELF, 0.211382114f);
			else ra = new RotateAnimation(180, 0,
						Animation.RELATIVE_TO_SELF, 0.180952381f,
						Animation.RELATIVE_TO_SELF, 0.211382114f);

			ra.setDuration((instant)?0:1000); //sets duration to 1s or 0.
			ra.setFillAfter(true);// set the animation after the end of the reservation status
			lidImageView.startAnimation(ra);
			lidState = state;
		}
	}

    private void createImageURIs(){
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String,Uri>(){{
	        put("rf_background", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_background.png").build());
            put("rf_overlay", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_overlay.png").build());
            put("rf_arrow", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_arrow.png").build());
	        put("rf_lid", rootUri.buildUpon().appendPath("skins").appendPath("rf_detector").appendPath("rf_lid.png").build());
            put("icon_rf_detector", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_rf_detector.png").build());

            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

    public void updateDestination() {
        destination = gcMain.getPlayerLocationInStory();
        }

//	@Override
//	protected int getAnimatorId(boolean enter) {
//		//TODO: implement this
//	}

	private class ProximityTest extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				BufferedReader in;
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				URI website = new URI(getString(R.string.proximity_activation_url));

				request.setURI(website);
				HttpResponse response = httpclient.execute(request);
				in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				return in.readLine();
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
				return null;
			}
		}

		@Override
		protected void onPostExecute(String s) {
			Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
		}
	}
}
