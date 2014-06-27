package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);

        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);

        Uri rootUri = Uri.fromFile(gcEngine.Access().root);
        overlay.setImageURI(rootUri.buildUpon().appendPath("skins").appendPath("amplifier").appendPath("amplifier_overlay.png").build());

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
