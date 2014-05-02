package ca.mixitmedia.ghostcatcher.app;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.SoundPool;
import android.media.AudioManager;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by IAN on 15/04/2014.
 */
public class BiocalibrateFragment extends ToolFragment {

    private static final int MAX_STREAMS = 2;
    private SoundPool mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    private int dialogueStream = 0;
    private int testSoundClip;
    private String[] dialogs = new String[]{"gc_0_0", "gc_0_1", "gc_1_0_1", "gc_1_0_2"};
    private boolean inProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_biocalibrate, container, false);
        testSoundClip = mSoundPool.load(getActivity(), R.raw.biocalib2, 1);
        inProgress = true;
        return view;
    }


    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.biocalibrate_btn:

                mSoundPool.stop(dialogueStream);
                AudioManager audioMan = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
                float streamVolume = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
                dialogueStream = mSoundPool.play(testSoundClip, streamVolume, streamVolume, 1, 0, 1f);

                inProgress = true;

                getView().findViewById(R.id.fingerprint_mask).setVisibility(1);
                getView().findViewById(R.id.calibrating_text).setVisibility(0);
                getView().findViewById(R.id.biocalibrate_btn).bringToFront();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int dialog = 0 + (int) (Math.random() * ((3 - 0) + 1));
                        gcMain.startDialogByLocation(dialogs[dialog]);


                    }
                }, 2500);

                return true;
            default:
                if(!inProgress)
                    return false;
                else
                    return true;
        }
    }

    public static BiocalibrateFragment newInstance(String settings) {
        BiocalibrateFragment fragment = new BiocalibrateFragment();
        Bundle args = new Bundle();
        args.putString("thingy", settings);
        fragment.setArguments(args);
        return fragment;
    }

}
