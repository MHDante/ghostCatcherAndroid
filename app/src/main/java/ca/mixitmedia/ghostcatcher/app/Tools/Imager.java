package ca.mixitmedia.ghostcatcher.app.Tools;

import java.io.IOException;


import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ca.mixitmedia.ghostcatcher.app.R;

public class Imager extends ToolFragment {

    private CameraHolder camHolder;
    private Camera cam;
    private FrameLayout camSpace;

    public Imager() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tool_imager, null);
        camSpace = (FrameLayout) v.findViewById(R.id.camera_preview);
        camHolder = new CameraHolder(getActivity());
        return v;

    }

    public static Camera isCameraAvailiable() {
        Camera object = null;
        try {
            object = Camera.open();
        } catch (Exception e) {
        }
        return object;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        ///gcMain.hideJournal();
        getView().findViewById(R.id.imagerFrame).bringToFront();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        camSpace.removeView(camHolder);
        cam.release();
    }

    @Override
    public void onResume() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                cam = isCameraAvailiable();
                if (cam != null) {
                    camHolder.setCam(cam);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                camSpace.addView(camHolder);
            }
        }.execute((Void) null);


        super.onResume();
    }

    @Override
    public boolean checkClick(View view) {
        if (view.getId() == R.id.back_gear_btn) {
            return false;
        }
        else
            return true;
    }

    public static Imager newInstance(String settings) {
        Imager fragment = new Imager();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }

    private class CameraHolder extends SurfaceView implements SurfaceHolder.Callback {

        private Camera cam;
        private SurfaceHolder sHolder;

        private CameraHolder(Context ctxt) {
            super(ctxt);
            sHolder = getHolder();
            sHolder.addCallback(this);
        }

        public void setCam(Camera cam) {
            this.cam = cam;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                cam.setPreviewDisplay(holder);
                cam.startPreview();
            } catch (IOException e) {
                Log.d("Imager:", "Imager didn't load" + e.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
