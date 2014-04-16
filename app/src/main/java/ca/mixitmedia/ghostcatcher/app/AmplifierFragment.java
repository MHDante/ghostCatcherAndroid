package ca.mixitmedia.ghostcatcher.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;

public class AmplifierFragment extends ToolFragment {

    private static final int MAX_STREAMS = 2;
    private SoundPool mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    private int dialogueStream = 0;
    private int testSoundClip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_amplifier, container, false);
        testSoundClip = mSoundPool.load(getActivity(), R.raw.gc_audio_amplifier, 1);
        view.setPivotX(0);
        view.setPivotY(view.getMeasuredHeight());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.amplifier_button:

                mSoundPool.stop(dialogueStream);
                AudioManager audioMan = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
                float streamVolume = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
                dialogueStream = mSoundPool.play(testSoundClip, streamVolume, streamVolume, 1, 0, 1f);

                return true;
            default:
                return false;
        }
    }
    public static AmplifierFragment newInstance(String settings) {
        AmplifierFragment fragment = new AmplifierFragment();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }
}
