package ca.mixitmedia.ghostcatcher.app;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.SoundPool;
import android.media.AudioManager;


/**
 * Created by IAN on 15/04/2014.
 */
public class BiocalibrateFragment extends ToolFragment {

    private static final int MAX_STREAMS = 2;
    private SoundPool mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    private int dialogueStream = 0;
    private int testSoundClip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_biocalibrate, container, false);
        testSoundClip = mSoundPool.load( getActivity(), R.raw.biocalib2, 1);
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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gcMain.swapTo("communicator");
                    }
                }, 2500);

                return true;
            default:
                return false;
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
