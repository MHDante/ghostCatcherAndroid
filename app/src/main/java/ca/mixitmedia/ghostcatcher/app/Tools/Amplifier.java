package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.mixitmedia.ghostcatcher.app.R;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);
        return view;
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_amplifier);
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
