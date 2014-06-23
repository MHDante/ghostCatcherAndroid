package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Alexander on 2014-06-17.
 */

public class RFDetector extends ToolFragment implements SensorEventListener, LocationListener {

    TextView latitudeTextView;
    TextView longitudeTextView;
    TextView compassTextView;
    TextView destinationTextView;

    ImageView compassFace;

    SensorManager sensorManager;
    double latitude;
    double longitude;

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

    Location destination;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_rf, container, false);

        latitudeTextView = (TextView) view.findViewById(R.id.latitude);
        longitudeTextView = (TextView) view.findViewById(R.id.longitude);
        compassTextView = (TextView) view.findViewById(R.id.compassText);
        destinationTextView = (TextView) view.findViewById(R.id.destinationAngle);

        compassFace = (ImageView) view.findViewById(R.id.compassFace);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);


        destination = new Location("dummyprovider");
        destination.setLatitude(43.652202);
        destination.setLongitude(-79.5814);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //register listener for the sensors
        registerSensor(Sensor.TYPE_ORIENTATION);


    }

    public void registerSensor(int sensorType) {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);    //unregister listener for sensors
        super.onPause();
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_rf_detector);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.amplifier_button:

                System.out.println("button pressed");

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //stub
    }

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
        newHeading = Math.round(newHeading);

        compassTextView.setText("Heading: " + Float.toString(newHeading) + " degrees");
        //float newRelativeBearing = Math.round(-(heading - bearing) % 360);

        // proven working:
        float newRelativeBearing = Math.round((newHeading - bearing + 360) % 360);

        destinationTextView.setText("Bearing: " + newRelativeBearing);

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
        compassFace.startAnimation(ra);
        heading = -newHeading;
        relativeBearing = newRelativeBearing;
    }

    @Override
    public void onLocationChanged(Location location) {
        // called when the listener is notified with a location update from the GPS
        System.out.println();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        bearing = (location.bearingTo(destination) + 360) % 360;

        // proven working:
        //bearing = location.bearingTo(destination);

        latitudeTextView.setText("Lat: " + latitude);
        longitudeTextView.setText("Long: " + longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        //stub
    }

    @Override
    public void onProviderDisabled(String provider) {
        //stub
    }
}
