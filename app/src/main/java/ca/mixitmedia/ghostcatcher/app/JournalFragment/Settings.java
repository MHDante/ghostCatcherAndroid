package ca.mixitmedia.ghostcatcher.app.JournalFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by yo2boy on 7/15/2014.
 */
public class Settings extends Fragment {

    TextView textView;
    Button delete;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_view, null);
        //textView = (TextView) v.findViewById(R.id.textItem1);

        return v;
    }
}
