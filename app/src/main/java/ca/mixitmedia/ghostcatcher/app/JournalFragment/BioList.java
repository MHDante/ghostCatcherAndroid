package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Shahroze on 2014-07-04.
 */
public class BioList extends Fragment{

    TextView textView;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.journal_frag_biolist, null);
        textView = (TextView) v.findViewById(R.id.textItem1);

        return v;


    }
}
