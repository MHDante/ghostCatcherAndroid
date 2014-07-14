package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.sql.Time;
import java.util.Calendar;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.views.SignalBeaconView;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;
    final Uri rootUri = gcEngine.Access().root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);

        final SignalBeaconView beaconView = (SignalBeaconView) view.findViewById(R.id.signal_beacon_wave);

        beaconView.setWaveFunction(new SignalBeaconView.WaveFunction() {
            @Override
            public float getGraphYWaveOne(float graphX, float amplitude) {
                float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/200);
                float period = 100;
                if(time == 0)    time = 200;
                return amplitude + (float) ((amplitude*Math.pow(time,3)%300) * Math.sin(graphX  / period + time));
            }
            public float getGraphYWaveTwo(float graphX, float amplitude) {
                float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/200);
                float period = 50;
                if(time == 0)   time = 200;
                return amplitude + (float) ((amplitude*Math.pow(time,5)%300) * Math.pow(Math.cos(graphX / period + time),3));
            }
            public float getGraphYWaveThree(float graphX, float amplitude) {
                float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/220);
                float period = 75;
                if(time == 0)   time = 200;
                return amplitude + (float) ((amplitude*Math.pow(time,3)%300) * Math.pow(Math.sin(graphX /period + time),5));
            }
        });

        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        overlay.setImageURI(rootUri.buildUpon().appendPath("skins").appendPath("amplifier").appendPath("amplifier_overlay.png").build());

        return view;
    }

    @Override
    public Uri getGlyphUri() {
        return ( rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_amplifier.png").build());
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.amplifier_button:

                gcMain.soundPool.stop(dialogueStream);
                AudioManager audioMan = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                float streamVolume = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
                dialogueStream = gcMain.playSound(gcMain.sounds.testSoundClip);

                return true;
            default:
                return false;
        }
    }

    @Override
    public pivotOrientation getPivotOrientation(boolean enter) {
        return pivotOrientation.LEFT;
    }

    protected int getAnimatorId(boolean enter) {
        if(enter) gcMain.playSound(gcMain.sounds.leverRoll);
        return (enter) ? R.animator.rotate_in_from_left : R.animator.rotate_out_to_right;
    }


}
