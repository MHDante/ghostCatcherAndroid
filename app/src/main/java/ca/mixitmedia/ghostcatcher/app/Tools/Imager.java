package ca.mixitmedia.ghostcatcher.app.Tools;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Imager extends ToolFragment {

    private SurfaceView preview = null;
    private SurfaceHolder previewHolder = null;
    private ImageView ImagerFrame = null;
    private Camera camera = null;
    private boolean inPreview = false;
    private boolean cameraConfigured = false;

    public Imager() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tool_imager, container, false);
        preview = (SurfaceView) v.findViewById(R.id.camera_preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        ImagerFrame = (ImageView) v.findViewById(R.id.overlay);

        ImageView overlay = (ImageView) v.findViewById(R.id.overlay);

        Uri rootUri = Uri.fromFile(gcEngine.Access().root);
        overlay.setImageURI(rootUri.buildUpon().appendPath("skins").appendPath("imager").appendPath("imager.png").build());

        return v;
    }

    @Override
    public boolean checkClick(View view) {
        return false;
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_imager);
    }

    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open();
        Log.d("Surface:", "Invalidate");


        startPreview();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera = null;
        inPreview = false;

        super.onPause();
    }

    @Override
    public void afterAnimation(boolean enter) {
        super.afterAnimation(enter);
        if (false) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (preview != null) {
                        if (inPreview) {
                            camera.stopPreview();
                        }

                        camera.release();
                        camera = null;
                        inPreview = false;
                        camera = Camera.open();
                        startPreview();

                        preview.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                        ImagerFrame.getParent().requestTransparentRegion(ImagerFrame);

                        Log.d("Imager", "Preview Swicharoo");
                    } else Log.e("Imager", "Preview Screen was null.");
                }
            }, 1000);
        }

    }


    private void initPreview(int width, int height) {
        if (camera != null && previewHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast
                        .makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height,
                        parameters);

                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }
        }
    }

    public static Camera.Size getBestPreviewSize(int width, int height,
                                                 Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return (result);
    }

    private void startPreview() {
        if (cameraConfigured && camera != null) {
            camera.setDisplayOrientation(90);
            camera.startPreview();
            inPreview = true;
            Log.d("Surface:", "StartPreview");

        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {

            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    @Override
    public pivotOrientation getPivotOrientation(boolean enter) {
        return pivotOrientation.RIGHT;
    }

    protected int getAnimatorId(boolean enter) {
        if(enter) gcMain.playSound(gcMain.sounds.leverRoll);
        return (enter) ? R.animator.rotate_in_from_right : R.animator.rotate_out_to_left;
    }



}
