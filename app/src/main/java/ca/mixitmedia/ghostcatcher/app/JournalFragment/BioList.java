package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcCharacter;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Shahroze on 2014-07-04.
 */
public class BioList extends Fragment{

    TextView name;
    TextView bio;
    ImageView image;
    ListView lv;

    int count;

    public class BioListAdapter extends android.widget.BaseAdapter{
        List charList;

        public BioListAdapter(List<gcCharacter> character){
            charList = character;
        }

        @Override
        public int getCount() {
            return charList.size();
        }

        @Override
        public Object getItem(int i) {
            return charList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return charList.indexOf(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //inflate the xml view brah

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.biolist_list, viewGroup);
            return view;
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journal_frag_biolist, null);
        name = (TextView) v.findViewById(R.id.textprofname);
        bio = (TextView) v.findViewById(R.id.profbio);
        image = (ImageView) v.findViewById(R.id.character_bio1);
        lv = (ListView) v.findViewById(R.id.masterBio);

        gcCharacter firstCharacter = gcEngine.Access().characters.get(0);

        BioListAdapter bla = new BioListAdapter(gcEngine.Access().characters);
        lv.setAdapter(bla);


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
