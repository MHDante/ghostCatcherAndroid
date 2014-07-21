package ca.mixitmedia.ghostcatcher.app.Tools;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;


/**
 * Created by IAN on 15/04/2014
 */
public class Biocalibrate extends ToolFragment {

    private int soundEffectStream = 0;
    private boolean started;
    long lastDown;
    long totalDuration;
    boolean pressed;
    ProgressBar LoadingBar;

    Map<String, Uri> imageFileLocationMap;

    public Biocalibrate(){
        createImageURIs();}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_biocalibrate, container, false);

        started = false;
        pressed = false;
        totalDuration = 0;

        LoadingBar = (ProgressBar) view.findViewById(R.id.calibrate_bar);
        LoadingBar.setMax(100);

        final ImageView overlay_pressed = (ImageView) view.findViewById(R.id.biocalibrate_btn);
		//TODO: this warning ↓
        ImageView overlay_unpressed = (ImageView) view.findViewById(R.id.fingerprint_mask);

        overlay_unpressed.setImageURI(imageFileLocationMap.get("unpressed"));

        ImageButton fingerPrint = (ImageButton) view.findViewById(R.id.biocalibrate_btn);
        fingerPrint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    overlay_pressed.setImageURI(imageFileLocationMap.get("pressed"));
                    overlay_pressed.setScaleType(ImageView.ScaleType.FIT_START);
                    lastDown = System.currentTimeMillis();
                    pressed = true;
                    if (!started) {
                        gcMain.hideGears(true, true);
                        gcMain.hideTool(Biocalibrate.class);
                        gcMain.soundPool.stop(soundEffectStream);
                        soundEffectStream = gcMain.playSound(gcMain.sounds.calibrateSoundClip);
                        started = true;
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (LoadingBar != null) {
                                    if (pressed) {
                                        totalDuration += System.currentTimeMillis() - lastDown;
                                        lastDown = System.currentTimeMillis();
                                    }

                                    LoadingBar.setProgress((int) ((totalDuration / 3000f) * 100f));
                                    if (totalDuration > 3000) {
                                        gcMain.triggerLocation();
                                        gcMain.swapTo(Communicator.class);
                                        gcMain.showGears();
                                    } else {
                                        handler.postDelayed(this, 100);
                                    }
                                }
                            }
                        }, 100);
                    } else gcMain.soundPool.resume(soundEffectStream);

                    getView().findViewById(R.id.calibrating_text).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.calibrate_pressed_layout).setAlpha(1.0f);


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pressed = false;
                    gcMain.soundPool.pause(soundEffectStream);
                    totalDuration += System.currentTimeMillis() - lastDown;
                    getView().findViewById(R.id.calibrating_text).setVisibility(View.INVISIBLE);
                    getView().findViewById(R.id.calibrate_pressed_layout).setAlpha(0);
                }
                return false;
            }
        });




        return view;
    }

    @Override
    public Uri getGlyphUri() {
        return (imageFileLocationMap.get("bio_calibrate_glyph"));
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.biocalibrate_btn:
                return true;
            default:
                return false;
        }
    }

    protected int getAnimatorId(boolean enter) {
        if (enter) {
			gcMain.playSound(gcMain.sounds.strangeMetalNoise);
			return R.animator.transition_in_from_top;
		}
        return R.animator.transition_out_from_bottom;
    }

    public void createImageURIs(){
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String,Uri>(){{
            put("unpressed", rootUri.buildUpon().appendPath("skins").appendPath("bio_calibrate").appendPath("bio_calibrate_unpressed.png").build());
            put("pressed", rootUri.buildUpon().appendPath("skins").appendPath("bio_calibrate").appendPath("bio_calibrate_pressed.png").build());
            put("bio_calibrate_glyph", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_biocalibrate.png").build());
            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

}
