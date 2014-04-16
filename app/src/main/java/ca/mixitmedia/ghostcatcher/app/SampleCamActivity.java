//package ca.mixitmedia.ghostcatcher.app;
//
//import java.io.File;
//import java.io.FileOutputStream;
//
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.hardware.SensorManager;
//import android.location.LocationListener;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.View;
//import android.widget.Toast;
//
//import com.wikitude.architect.ArchitectView;
//import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
//import com.wikitude.architect.ArchitectView.CaptureScreenCallback;
//import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
//import com.example.imagertool.R;
//
//public class SampleCamActivity extends AbstractArchitectCamActivity {
//    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
//
//    private int selectedGhost;
//    @Override
//    public void onCreate(final Bundle savedInstanceState ) {
//        super.onCreate( savedInstanceState );
//        selectedGhost = getIntent().getExtras().getInt(MainActivity.SELECTED_GHOST);
//    }
//
//    @Override
//    protected void onPostCreate( final Bundle savedInstanceState ) {
//        super.onPostCreate( savedInstanceState );
//        this.loadData();
//
//    }
//
//    final Runnable loadData = new Runnable() {
//        @Override
//        public void run() {
//            SampleCamActivity.this.callJavaScript("World.setGhostMarker");
//        }
//    };
//
//    protected void loadData() {
//        final Thread t = new Thread(loadData);
//        t.start();
//
//    }
//
//    /**
//     * call JacaScript in architectView
//     * @param methodName
//     * @param arguments
//     */
//    private void callJavaScript(final String methodName) {
//        if (this.architectView!=null) {
//            final String js = ( methodName + "( " + selectedGhost + " );" );
//            this.architectView.callJavascript(js);
//        }
//    }
//
//    @Override
//    public String getARchitectWorldPath() {
//        return getIntent().getExtras().getString(
//                MainActivity.EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL);
//    }
//
//    @Override
//    public String getActivityTitle() {
//        return (getIntent().getExtras() != null && getIntent().getExtras().get(
//                MainActivity.EXTRAS_KEY_ACTIVITY_TITLE_STRING) != null) ? getIntent()
//                .getExtras().getString(MainActivity.EXTRAS_KEY_ACTIVITY_TITLE_STRING)
//                : "Test-World";
//    }
//
//
//    //name of layout xml
//    @Override
//    public int getContentViewId() {
//        return R.layout.sample_cam;
//    }
//
//    @Override
//    public int getArchitectViewId() {
//        return R.id.architectView;
//    }
//
//
//    @Override
//    public String getWikitudeSDKLicenseKey() {
//        return WikitudeSDKConstants.WIKITUDE_SDK_KEY;
//    }
//
//
//
//    @Override
//    public SensorAccuracyChangeListener getSensorAccuracyListener() {
//        return new SensorAccuracyChangeListener() {
//            @Override
//            public void onCompassAccuracyChanged( int accuracy ) {
//                // UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3
//                if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && SampleCamActivity.this != null && !SampleCamActivity.this.isFinishing() && System.currentTimeMillis() - SampleCamActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
//                    Toast.makeText( SampleCamActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
//                    SampleCamActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
//                }
//            }
//        };
//    }
//
//
//    @Override
//    public ArchitectUrlListener getUrlListener() {
//        return new ArchitectUrlListener() {
//
//            @Override
//            public boolean urlWasInvoked(String uriString) {
//                Uri invokedUri = Uri.parse(uriString);
//                if ("button".equalsIgnoreCase(invokedUri.getHost())) {
//                    SampleCamActivity.this.architectView.captureScreen(ArchitectView.CaptureScreenCallback.CAPTURE_MODE_CAM, new CaptureScreenCallback() {
//                        @Override
//                        public void onScreenCaptured(final Bitmap screenCapture) {
//                            // store screenCapture into external cache directory
//                            final File screenCaptureFile = new File(Environment.getExternalStorageDirectory().toString(), "screenCapture_" + System.currentTimeMillis() + ".jpg");
//
//
//                            // 1. Save bitmap to file & compress to jpeg. You may use PNG too
//                            try {
//                                final FileOutputStream out = new FileOutputStream(screenCaptureFile);
//                                screenCapture.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                                out.flush();
//                                out.close();
//
//                                final Intent share = new Intent(Intent.ACTION_VIEW);
//                                share.setDataAndType(Uri.fromFile(screenCaptureFile), "image/*");
//                                startActivity(share);
//                            }catch (final Exception e) {
//                                // should not occur when all permissions are set
//                                SampleCamActivity.this.runOnUiThread(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        // show toast message in case something went wrong
//                                        Toast.makeText(SampleCamActivity.this, "Unexpected error, " + e.getMessage(), Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                }
//                return true;
//            }
//        };
//    }
//
//    @Override
//    public ILocationProvider getLocationProvider(final LocationListener locationListener) {
//        return new LocationProvider(this, locationListener);
//    }
//
//    @Override
//    public float getInitialCullingDistanceMeters() {
//        // you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
//        return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
//    }
//
//
//
//
//}
