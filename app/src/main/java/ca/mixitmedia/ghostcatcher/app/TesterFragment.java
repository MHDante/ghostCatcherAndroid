package ca.mixitmedia.ghostcatcher.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;

public class TesterFragment extends ToolFragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tester, container, false);

        ((Button) view.findViewById(R.id.tester_button_1)).setText(tester_button_1);
        ((Button) view.findViewById(R.id.tester_button_2)).setText(tester_button_2);
        ((Button) view.findViewById(R.id.tester_button_3)).setText(tester_button_3);
        ((Button) view.findViewById(R.id.tester_button_4)).setText(tester_button_4);
        ((Button) view.findViewById(R.id.tester_button_5)).setText(tester_button_5);
        ((Button) view.findViewById(R.id.tester_button_6)).setText(tester_button_6);
        ((Button) view.findViewById(R.id.tester_button_7)).setText(tester_button_7);
        ((Button) view.findViewById(R.id.tester_button_8)).setText(tester_button_8);

        return view;
    }

    public String tester_button_1 = "play";

    //public void tester_button_1(View v) {
    //    gcAudio.play();
    //}

    public String tester_button_2 = "pause";

    //public void tester_button_2(View v) {
    //    gcAudio.pause();
    //}

    public String tester_button_3 = "Stop";

    //public void tester_button_3(View v) {
    //    gcAudio.stop();
    //}

    public String tester_button_4 = "Stop Looping";

    //public void tester_button_4(View v) {
    //    gcAudio.stopLooping();
    //}

    public String tester_button_5 = "Play Track";

    //public void tester_button_5(View v) {
    //    gcAudio.playTrack("main3", false);
    //}

    public String tester_button_6 = "Queue Track";

    //public void tester_button_6(View v) {
    //    gcAudio.queueTrack("main3", false);
    //}

    public String tester_button_7 = "Play Track and loop";

    //public void tester_button_7(View v) {
    //    gcAudio.playTrack("main3", true);
    //}

    public String tester_button_8 = "Queue Track and Loop";

    //public void tester_button_8(View v) {
    //    gcAudio.queueTrack("main3", true);
    //}


    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.tester_button_1:
                gcAudio.play();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_2:
                gcAudio.pause();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_3:
                gcAudio.stop();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_4:
                gcAudio.stopLooping();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_5:
                gcAudio.playTrack("main3", false);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_6:
                gcAudio.queueTrack("main3", false);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_7:
                gcAudio.playTrack("main3", true);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_8:
                gcAudio.queueTrack("main3", true);
                //gcMain.swapTo("");
                return true;

            default:
                return false;
        }
    }


    public static TesterFragment newInstance(String settings) {
        TesterFragment fragment = new TesterFragment();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }
}