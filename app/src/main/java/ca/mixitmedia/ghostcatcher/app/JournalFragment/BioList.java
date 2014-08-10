package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.experience.gcCharacter;

/**
 * Created by Shahroze (Yo2Boy)  2014-07-04.
 */
public class BioList extends ToolFragment {

    TextView name;
    TextView bio;
    ImageView image;

    //int count;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journal_frag_biolist, container);
        name = (TextView) v.findViewById(R.id.textprofname);
        bio = (TextView) v.findViewById(R.id.profbio);
        image = (ImageView) v.findViewById(R.id.character_bio1);

        gcCharacter firstCharacter = gcMain.gcEngine.getCharacters().get("prof_wolfe");

        name.setText(firstCharacter.getName());
        bio.setText(firstCharacter.getBio());
        try {
            image.setImageBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), firstCharacter.getPose("pose1")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;


    }
}
