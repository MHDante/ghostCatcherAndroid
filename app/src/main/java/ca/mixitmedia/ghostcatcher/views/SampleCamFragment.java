package ca.mixitmedia.ghostcatcher.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;

import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.Tools;


public class SampleCamFragment extends AbstractArchitectCamFragmentV4{

    private static final java.lang.String ARCHITECT_ACTIVITY_EXTRA_KEY_URL = "url2load";
    /**
	 * last time the calibration toast was shown, this avoids too many toast shown when compass needs calibration
	 */
	private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
    private int selectedGhost =1;

    @Override
	public String getARchitectWorldPath() {
		try {
            String url = "imager/barry/index.html";//"samples/ss/index.html"; //getResources().getAssets().list("samples")[0];
            Log.d("@@@@@@@@@@@@", url);
            return  url;
			//final String decodedUrl = URLDecoder.decode(getActivity().getIntent().getExtras().getString(ARCHITECT_ACTIVITY_EXTRA_KEY_URL), "UTF-8");
			//return decodedUrl;
		} catch (Exception e) {
			Toast.makeText(this.getActivity(), "Unexpected Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		}
	}

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        this.architectView.callJavascript("World.setGhostMarker( " + selectedGhost + " );");
    }

    @Override
	public int getContentViewId() {
		return R.layout.architect_view;
	}

	@Override
	public int getArchitectViewId() {
		return R.id.architectView;
	}
	
	@Override
	public String getWikitudeSDKLicenseKey() {
		return getString(R.string.Wikitude_Key);
	}
	

	@Override
	public SensorAccuracyChangeListener getSensorAccuracyListener() {
		return new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && getActivity() != null && !getActivity().isFinishing()  && System.currentTimeMillis() - SampleCamFragment.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
					Toast.makeText( getActivity(), R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
				}
			}
		};
	}

	@Override
	public ArchitectUrlListener getUrlListener() {
		return new ArchitectUrlListener() {

            @Override
            public boolean urlWasInvoked(String uriString) {
                Uri invokedUri = Uri.parse(uriString);
                if ("button".equalsIgnoreCase(invokedUri.getHost())) {
                    if(invokedUri.getBooleanQueryParameter("visible", false)){
                        ((MainActivity)getActivity()).experienceManager.ToolSuccess(Tools.imager);
                        ((MainActivity)getActivity()).swapTo(Tools.communicator);                   //Todo:Hack
                    }
                    else Toast.makeText(getActivity(),"~NO GHOST FOUND!~",Toast.LENGTH_LONG).show();
                    if (System.currentTimeMillis() > 0) return true;
                    architectView.captureScreen(ArchitectView.CaptureScreenCallback.CAPTURE_MODE_CAM, new ArchitectView.CaptureScreenCallback() {
                        @Override
                        public void onScreenCaptured(final Bitmap screenCapture) {
                            // store screenCapture into external cache directory
                            final File screenCaptureFile = new File(Environment.getExternalStorageDirectory().toString(), "screenCapture_" + System.currentTimeMillis() + ".jpg");


                            // 1. Save bitmap to file & compress to jpeg. You may use PNG too
                            try {
                                final FileOutputStream out = new FileOutputStream(screenCaptureFile);
                                screenCapture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                                out.close();

                                final Intent share = new Intent(Intent.ACTION_VIEW);
                                share.setDataAndType(Uri.fromFile(screenCaptureFile), "image/*");
                                startActivity(share);
                            }catch (final Exception e) {
                                // should not occur when all permissions are set
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // show toast message in case something went wrong
                                        Toast.makeText(getActivity(), "Unexpected error, " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
                return true;
            }
		};
	}

	@Override
	public ILocationProvider getLocationProvider(final LocationListener locationListener) {
		return new WikitudeProvider(this.getActivity(), locationListener);
	}

}
