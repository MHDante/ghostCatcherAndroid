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

import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;


/**
 * Created by IAN on 15/04/2014.
 */
public class Biocalibrate extends ToolFragment {

    private static final int MAX_STREAMS = 2;
    public static boolean hasBackStack;
    private SoundPool mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    private int dialogueStream = 0;
    private int testSoundClip;
    private String[] dialogs = new String[]{"gc_0_0", "gc_0_1", "gc_1_0_1", "gc_1_0_2"};
    private boolean started;
    long lastDown;
    long totalDuration;
    boolean pressed;
    ImageView LoadingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_biocalibrate, container, false);
        testSoundClip = mSoundPool.load(getActivity(), R.raw.gc_audio_amplifier, 1);
        started = false;
        pressed = false;
        totalDuration = 0;

        LoadingBar = (ImageView) view.findViewById(R.id.loading_bar);

        ImageButton fingerPrint = (ImageButton) view.findViewById(R.id.biocalibrate_btn);
        fingerPrint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastDown = System.currentTimeMillis();
                    pressed = true;
                    if (!started) {
                        gcMain.hideGears(true, true);
                        gcMain.HideTool("biocalib");
                        mSoundPool.stop(dialogueStream);
                        AudioManager audioMan = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                        float streamVolume = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
                        dialogueStream = mSoundPool.play(testSoundClip, streamVolume, streamVolume, 1, 0, 1f);
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

                                    int maxWidth = ((LinearLayout) LoadingBar.getParent()).getWidth();
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) LoadingBar.getLayoutParams();
                                    params.width = (int) (maxWidth * ((float) totalDuration / 3000f));
                                    LoadingBar.setLayoutParams(params);
                                    LoadingBar.invalidate();
                                    if (totalDuration > 3000) {
                                        gcMain.triggerLocation();
                                        if (hasBackStack) {
                                            getActivity().onBackPressed();
                                            hasBackStack = false;
                                        } else gcMain.swapTo(Communicator.class, false);
                                        if (MainActivity.debugLoc == 1) {
                                            gcMain.ShowTool("amplifier");
                                            gcMain.ShowTool("imager");
                                        }
                                    } else {
                                        handler.postDelayed(this, 100);
                                    }
                                }
                            }
                        }, 100);
                    } else mSoundPool.resume(dialogueStream);

                    //getView().findViewById(R.id.fingerprint_mask).setVisibility(1);
                    getView().findViewById(R.id.calibrating_text).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.biocalibrate_btn).bringToFront();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pressed = false;
                    mSoundPool.pause(dialogueStream);
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

    public static Biocalibrate newInstance(String settings) {
        Biocalibrate fragment = new Biocalibrate();
        Bundle args = new Bundle();
        args.putString("thingy", settings);
        fragment.setArguments(args);
        return fragment;
    }


}
