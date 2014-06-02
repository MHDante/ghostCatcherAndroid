package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Dante on 2014-04-14.
 */
public class Communicator extends ToolFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private String pendingDialog;
    private boolean stopText = false;
    private List<View> tools;

    public boolean bioCalib = false;
    public boolean map = true;
    public boolean imager = false;
    public boolean amplifier = false;


    private int drawableId = 0;  //todo: hack
    private boolean userIsScrolling = false;

    public Communicator() {
    }//req'd

    // TODO: Rename and change types and number of parameters
    public static Communicator newInstance(String param1) {
        Communicator fragment = new Communicator();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_communicator, container, false);
        view.setPivotX(0);//TODO: Fix
        view.setPivotY(view.getMeasuredHeight());
        TextView tv = (TextView) (view.findViewById(R.id.subtitle_text_view));
        ScrollView sv = (ScrollView) (view.findViewById(R.id.subtitle_scroll_view));
        sv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    userIsScrolling = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    userIsScrolling = false;
                return false;
            }
        });

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ocean_sans.ttf");
        tv.setTypeface(font);
        tv.setTextSize(20);

        if (savedInstanceState != null) drawableId = savedInstanceState.getInt("drawableId");

        if (drawableId != 0) {
            ImageView imgV = (ImageView) view.findViewById(R.id.character_portrait);
            imgV.setImageResource(drawableId);
        }

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

        ScrollView sv = (ScrollView) getView().findViewById(R.id.subtitle_scroll_view);
        if (!userIsScrolling) sv.fullScroll(View.FOCUS_DOWN);
    }

    public void loadfile(String dialogId) {
        if (getView() == null) {
            pendingDialog = dialogId;
            return;
        }
        gcDialog dialog = gcDialog.get(gcEngine.Access().getCurrentSeqPt(), dialogId);
        startDialog();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingDialog != null) {
            loadfile(pendingDialog);
            pendingDialog = null;
        } else if (getView() == null) throw new RuntimeException("View was null on Resume. Why?");

        gcMain.showGears();

        if (!map) ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(0);
        if (!bioCalib) ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(0);
        if (!imager) ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(0);
        if (!amplifier) ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(0);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.tool_button_1:
                if (map) gcMain.swapTo(LocationMap.class, true);
                return true;
            case R.id.tool_button_2:
                Biocalibrate.hasBackStack = true;
                if (bioCalib) gcMain.swapTo(Biocalibrate.class, true);
                return true;
            case R.id.tool_button_3:
                if (imager) gcMain.swapTo(Imager.class, true);
                return true;
            case R.id.tool_button_4:
                if (amplifier) gcMain.swapTo(Amplifier.class, true);
                return true;
            case R.id.sound:
                if (gcAudio.isPlaying()) gcAudio.pause();
                else gcAudio.play();
                //populateText("Hello world. ", true);
                stopText = !stopText;
                return true;
            default:
                return false;
        }

    }

    int counter;
    boolean isTimerRunning;
    Timer timer = new Timer();
    String currentString = "";

    //Hello barry, This function starts the dialog.
    protected void startDialog() {
        timer.cancel();
        timer = new Timer();
        populateText("", false);
        counter = 0;
        isTimerRunning = true;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!stopText)
                    counter += 1;  //increase every sec
                mHandler.obtainMessage(1).sendToTarget();

            }
        }, 0, 60);
    }

    private String displayString = "";
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (currentString != null && counter > currentString.length()) {
                currentString = null;
                timer.cancel();
            } else {
                if (currentString != null) displayString = currentString.substring(0, counter);
                if (getView() != null) {
                    populateText(displayString, false);
                }
            }
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("drawableId", drawableId);
        super.onSaveInstanceState(outState);
    }


    public void HideTool(String tool) {

        if (tool.equals("map")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(255);
            map = false;
        }
        if (tool.equals("biocalib")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(255);
            bioCalib = false;
        }
        if (tool.equals("amplifier")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(255);
            amplifier = false;
        }
        if (tool.equals("imager")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(255);
            imager = false;
        }
    }

    public void ShowTool(String tool) {
        if (tool.equals("map")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(0);
            map = true;
        }
        if (tool.equals("biocalib")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(0);
            bioCalib = true;
        }
        if (tool.equals("amplifier")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(0);
            amplifier = true;
        }
        if (tool.equals("imager")) {
            if (getView() != null)
                ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(0);
            imager = true;
        }
    }
}
