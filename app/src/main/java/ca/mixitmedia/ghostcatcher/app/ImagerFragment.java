package ca.mixitmedia.ghostcatcher.app;

import java.io.File;
import java.io.FileOutputStream;


import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView.CaptureScreenCallback;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;

public class ImagerFragment extends AbstractArchitectCamFragmentV4 {

    protected static final String WIKITUDE_SDK_KEY = "tW3Ht861dC8lNC2/xnahrP7DRI7nqfXDGZfUvdeIGJsc64QLICf0leLIO50svo8me8yrGBcglCEHwAQEhqs8eOceTQ0iXJhF6Glf016W/tDsCSXMetFz4vdZIHzOdNOOBJEKuIEPYZgyffutNigYtdWpPzS33L1OQLoVkdQ3z/NTYWx0ZWRfX21xcPVHf8nkO6IcVarrd+BPN3s78E/Ac39sjGX90LKiVK47DeMMovn5X5X3vXIcwrxe3WRZqL6sczpdObKvll+Qwvpbrnej1ausee5uUCCXFC6oevbcnuWyqgYXzctjCjrJlcdfzL1EgPi9W4i87bnT1uf8IYISiPwBlK7vNvLpBIfryzyn9t4EKMGr5X0GTgXA11G5BJW2wkgukMSt7zp8fE1XurT/C97tTtN9P/w5lyWnpjm/1Qt8n409QxB09A9ZW2x9vcOoTzyCKQyTXduWyhW4hv6gXHZUU5A88PJBGtFpX63f40VURgi+6hMNy2Q4I57Ck0d+NTJHQnPbm2NoHKiAkFr5LS2A91lnhF3BI9Qx8YPpjStPNslW93NJPXOstmSFDLep9MpNx+tLzPMRBj5HLjSTPCzWZSQSuXSj31hc/nwoaVl+0MvQzNC1YQdS1QcjeQx5Ln6nFaW+eCmRHOOAZZQJUJeaVvLSMll+zaYwZ+WrIYDCMx/nCwjtQ7Y+Voj2sivGIxPNrcDUuWul6q2D3WJmUwjquibs2M97lLljwojpgLfRf8hWynpvnfliTgk2OZUT96+vcwLW3FHkCLFteuRQ2g==";
    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
    protected ArchitectView architectView;
    private int selectedGhost = 1;

    private String ARUrl = "samples"
            + File.separator + "1_$Ghost$Catcher$AR$Tool_"
            + File.separator + "index.html";

    private String ARTitle = "Imager Tool";

    private String jsMethod = "World.setGhostMarker";

    public ImagerFragment() {
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.architectView != null) {
            final String js = (jsMethod + "( " + selectedGhost + " );");
            this.architectView.callJavascript(js);
        }

    }

    @Override
    public String getARchitectWorldPath() {
        return ARUrl;
    }

    //name of layout xml
    @Override
    public int getContentViewId() {
        return R.layout.sample_cam;
    }

    @Override
    public int getArchitectViewId() {
        return R.id.architectView;
    }


    @Override
    public String getWikitudeSDKLicenseKey() {
        return WIKITUDE_SDK_KEY;
    }


    @Override
    public SensorAccuracyChangeListener getSensorAccuracyListener() {
        return new SensorAccuracyChangeListener() {
            @Override
            public void onCompassAccuracyChanged(int accuracy) {
                // UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && ImagerFragment.this != null && !getActivity().isFinishing() && System.currentTimeMillis() - ImagerFragment.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                    Toast.makeText(getActivity(), R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    ImagerFragment.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
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
                    ImagerFragment.this.architectView.captureScreen(ArchitectView.CaptureScreenCallback.CAPTURE_MODE_CAM, new CaptureScreenCallback() {
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
                            } catch (final Exception e) {
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
    public boolean checkClick(View view) {
        return false;
    }

    @Override
    public ILocationProvider getLocationProvider(final LocationListener locationListener) {
        return new LocationProvider(getActivity(), locationListener);
    }

    @Override
    public float getInitialCullingDistanceMeters() {
        // you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
        return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
    }


    public static ImagerFragment newInstance(String settings) {
        ImagerFragment fragment = new ImagerFragment();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }

}
