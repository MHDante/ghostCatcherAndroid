package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.experience.gcCharacter;

/**
 * Created by Shahroze on 2014-07-04
 */
public class BioList extends ToolFragment {

	TextView charName;
    TextView nameTextView;
    TextView bioTextView;
    ImageView bioImageView;
    ListView lv;
    List<String> keys;


    public class BioListAdapter extends android.widget.BaseAdapter{
        HashMap<String, gcCharacter> charList;


        public BioListAdapter(HashMap<String, gcCharacter> character){
            charList = character;
            //eys = Arrays.asList("prof_wolfe");
            keys = new ArrayList<>(charList.keySet());
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

            String charId ="";

            charId = keys.get(i);
            gcCharacter ghostCatcherCharacters = gcMain.gcEngine.getCharacters().get(charId);




            //       charName = (TextView) v.findViewById(R.id.charName);
            //       charName.setText(ghostCatcherCharacters.getName());

            //nameTextView.setText(ghostCatcherCharacters.getName());

           // bioTextView.setText("hi");
            bioTextView.setText(ghostCatcherCharacters.getBio());

            bioImageView = (ImageView) view.findViewById(R.id.imageView);

            try {
                bioImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ghostCatcherCharacters.getDefaultPose()));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("BioList","bioImageView cant set bitmap, I/O error?", e);
            }


            charName = (TextView) view.findViewById(R.id.charName);

            HashMap<String, gcCharacter> charMap = gcMain.gcEngine.getCharacters();
            String aa = keys.get(0);
            gcCharacter gcC = charMap.get(aa);
            String text = gcC.getName();
            charName.setText(text);

            charName.setText(ghostCatcherCharacters.getName());


            //bioImageView.setImageURI(Uri.fromFile(new File(gcEngine.Access().root +"/characters/" + "/"+"prof_wolfe"+"/"+"smug.png")));


            return view;
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journal_frag_biolist, null);
        nameTextView = (TextView) v.findViewById(R.id.textprofname);
        bioTextView = (TextView) v.findViewById(R.id.profbio);
        bioImageView = (ImageView) v.findViewById(R.id.character_bio1);
        lv = (ListView) v.findViewById(R.id.masterBio);

        gcCharacter firstCharacter = gcMain.gcEngine.getCharacters().get("prof_wolfe");

        return v;
    }
}
