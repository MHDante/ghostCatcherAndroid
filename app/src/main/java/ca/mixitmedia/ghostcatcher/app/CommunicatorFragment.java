package ca.mixitmedia.ghostcatcher.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Dante on 2014-04-14.
 */
public class CommunicatorFragment extends ToolFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    public CommunicatorFragment() {
    }//req'd

    // TODO: Rename and change types and number of parameters
    public static CommunicatorFragment newInstance(String param1) {
        CommunicatorFragment fragment = new CommunicatorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_communicator, container, false);
        view.setPivotX(0);//TODO: Fix
        view.setPivotY(view.getMeasuredHeight());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateText("", false);

    }

    private void populateText(String st, Boolean append) {
        View v = getView().findViewById(R.id.subtitle_text_view);
        TextView tv = (TextView) v;
        String stPrev = (String) tv.getText();
        if (append) st = stPrev + st;
        tv.setText(st);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.tool_button_1:
                gcMain.swapTo("map");
                return true;
            case R.id.tool_button_2:
                gcMain.hideGears("tester");
                return true;
            case R.id.sound:
                //if (gcAudio.isPlaying()) gcAudio.pause();
                //else gcAudio.play();
                populateText("Hello world. ", true);
                return true;
            default:
                return false;
        }

    }
}
