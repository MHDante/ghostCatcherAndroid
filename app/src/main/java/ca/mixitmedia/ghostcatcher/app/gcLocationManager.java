package ca.mixitmedia.ghostcatcher.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import ca.mixitmedia.ghostcatcher.app.Tools.Tools;

/**
 * Created by Dante on 2014-07-27
 */
public class gcLocationManager implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    static final int GPS_SLOW_MIN_UPDATE_TIME_MS = 60000; //60 seconds
    static final int GPS_SLOW_MIN_UPDATE_DISTANCE_M = 50; //50 meters

    LocationManager locationManager;
    MainActivity gcMain;
    Location currentGPSLocation;
    //the minimal GPS update interval, in milliseconds
    int GPSMinUpdateTimeMS;
    // the minimal GPS update interval, in meters.
    int GPSMinUpdateDistanceM;

	boolean GPSStatus;

    public gcLocationManager(MainActivity gcMain) {
        this.gcMain = gcMain;
        locationManager = (LocationManager) gcMain.getSystemService(Context.LOCATION_SERVICE);
    }


    //Todo:Implement
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	    setGPSStatus();
    }

    @Override
    public void onProviderEnabled(String provider) {
        setGPSStatus();
    }

    @Override
    public void onProviderDisabled(String provider) {
        setGPSStatus();
    }

    @Override
    public void onLocationChanged(Location location) {
	    currentGPSLocation = location;
        gcMain.experienceManager.UpdateLocation(location);
        if (Tools.Current() == Tools.rfDetector) Tools.rfDetector.onLocationChanged(location);
    }

    public void setGPSStatus() {
        if (Tools.Current() == Tools.rfDetector) {
	        Tools.rfDetector.setGPSState(GPSStatus, false);
	        if (GPSStatus && currentGPSLocation != null) {
		        System.out.println("Stored location loaded");
		        onLocationChanged(currentGPSLocation);
	        }
        }
    }

    /**
     * reconfigures GPS updates to occur at the requested minimum time and distance intervals
     *
     * @param GPSMinUpdateTimeMS    the minimal GPS update interval, in milliseconds
     * @param GPSMinUpdateDistanceM the minimal GPS update interval, in meters.
     */
    public void setGPSUpdates(int GPSMinUpdateTimeMS, int GPSMinUpdateDistanceM) {
        if (GPSMinUpdateTimeMS < 0 || GPSMinUpdateDistanceM < 0) {
            throw new IllegalArgumentException("GPSMinUpdateTimeMS and GPSMinUpdateDistanceM  cannot be negative");
        }

        this.GPSMinUpdateTimeMS = GPSMinUpdateTimeMS;
        this.GPSMinUpdateDistanceM = GPSMinUpdateDistanceM;

        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER,
		        GPSMinUpdateTimeMS,
		        GPSMinUpdateDistanceM, this);
    }

    /**
     * reconfigures GPS updates to occur at the minimum every 6s and 50 meters
     */
    public void requestSlowGPSUpdates() {
        setGPSUpdates(GPS_SLOW_MIN_UPDATE_TIME_MS, GPS_SLOW_MIN_UPDATE_DISTANCE_M);
    }

    /**
     * returns the most recent known location of the user.
     * @return the most recent known location of the user.
     */
    public Location getCurrentGPSLocation() {
        return currentGPSLocation;
    }

    public void removeUpdates() {
        locationManager.removeUpdates(this);
    }

	@Override
	public void onConnected(Bundle bundle) {
		GPSStatus = true;
	}

	@Override
	public void onDisconnected() {
		GPSStatus = false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		GPSStatus = false;
	}
}
