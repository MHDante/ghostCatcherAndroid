package ca.mixitmedia.ghostcatcher.app.Tools;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.media.SoundPool;
import android.media.AudioManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;


/**
 * Created by IAN on 15/04/2014.
 */
public class Biocalibrate extends ToolFragment {

    private static final int MAX_STREAMS = 2;
    private SoundPool mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    private int soundEffectStream = 0;
    private int calibrateSoundClip;
    private boolean started;
    long lastDown;
    long totalDuration;
    boolean pressed;
    ProgressBar LoadingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_biocalibrate, container, false);
        calibrateSoundClip = mSoundPool.load(getActivity(), R.raw.gc_audio_amplifier, 1);
        started = false;
        pressed = false;
        totalDuration = 0;

        LoadingBar = (ProgressBar) view.findViewById(R.id.calibrate_bar);
        LoadingBar.setMax(100);

        ImageButton fingerPrint = (ImageButton) view.findViewById(R.id.biocalibrate_btn);
        fingerPrint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastDown = System.currentTimeMillis();
                    pressed = true;
                    if (!started) {
                        gcMain.hideGears(true, true);
                        gcMain.hideTool(Biocalibrate.class);
                        mSoundPool.stop(soundEffectStream);
                        AudioManager audioMan = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                        float streamVolume = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
                        soundEffectStream = mSoundPool.play(calibrateSoundClip, streamVolume, streamVolume, 1, 0, 1f);
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
                    } else mSoundPool.resume(soundEffectStream);

                    //getView().findViewById(R.id.fingerprint_mask).setVisibility(1);
                    getView().findViewById(R.id.calibrating_text).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.biocalibrate_btn).bringToFront();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pressed = false;
                    mSoundPool.pause(soundEffectStream);
                    totalDuration += System.currentTimeMillis() - lastDown;
                    getView().findViewById(R.id.calibrating_text).setVisibility(View.INVISIBLE);
                    getView().findViewById(R.id.fingerprint_mask).bringToFront();
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_biocalibrate);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.biocalibrate_btn:
                return true;
            default:
                if (!started)
                    return false;
                else
                    return true;
        }
    }


}
