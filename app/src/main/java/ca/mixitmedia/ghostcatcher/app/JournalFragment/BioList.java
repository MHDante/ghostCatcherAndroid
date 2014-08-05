package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.experience.gcCharacter;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Shahroze on 2014-07-04.
 */
public class BioList extends ToolFragment {

    TextView name;
    TextView bio;
    ImageView image;
    ListView lv;
    TextView charName;
    List<String> keys;

    public class BioListAdapter extends android.widget.BaseAdapter{
        HashMap<String, gcCharacter> charList;


        public BioListAdapter(HashMap<String, gcCharacter> character){
            charList = character;
            keys = new ArrayList<String>(charList.keySet());
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

            //keys.get(i);

            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //inflate the xml view brah

            //charList.get("bla");
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.biolist_list, null);

            charName = (TextView) view.findViewById(R.id.charName);
            charName.setText(gcMain.gcEngine.characters.get(keys.get(i)).getName());

            image = (ImageView) view.findViewById(R.id.character_bio1);
            //image.setImageURI(Uri.fromFile(new File(gcEngine.Access().root +"/characters/" + "/"+"prof_wolfe"+"/"+"smug.png")));


            return view;
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journal_frag_biolist, null);
        name = (TextView) v.findViewById(R.id.textprofname);
        bio = (TextView) v.findViewById(R.id.profbio);
        image = (ImageView) v.findViewById(R.id.character_bio1);
        lv = (ListView) v.findViewById(R.id.masterBio);

        String a = keys.get(0);

        gcCharacter firstCharacter = gcMain.gcEngine.characters.get(a);

        BioListAdapter bla = new BioListAdapter(gcMain.gcEngine.characters);
        lv.setAdapter(bla);



 //       charName = (TextView) v.findViewById(R.id.charName);
//        charName.setText(firstCharacter.getName());

        //name.setText(firstCharacter.getName());
        //charName.setText(firstCharacter.getName());

        bio.setText(firstCharacter.getBio());
        try {
            image.setImageBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), firstCharacter.getPose("pose1")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;


    }
}
