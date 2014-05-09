package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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

/**
 * Created by Dante on 2014-04-14.
 */
public class Communicator extends ToolFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private String pendingDialog;
    private boolean stopText = false;
    private List<View> tools;
    public boolean bioCalib = true;
    private int drawableId = 0;  //todo: hack

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

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/pixelated.ttf");
        tv.setTypeface(font);

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
        sv.fullScroll(View.FOCUS_DOWN);
    }

    public void loadfile(String file) {
        if (getView() == null) {
            pendingDialog = file;
            return;
        }
        AssetManager assetManager = getResources().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(file + ".txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            currentString = total.toString();

            drawableId = getResources().getIdentifier(file, "drawable", getActivity().getPackageName());
            ImageView imgV = (ImageView) getView().findViewById(R.id.character_portrait);
            imgV.setImageResource(drawableId);

            gcAudio.playTrack(file, false);

            startDialog();

            //new Handler().postDelayed(new Runnable() {
            //    @Override
            //    public void run() {
            //        startDialog();
            //    }
            //}, 500);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
// ...
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingDialog != null) {
            loadfile(pendingDialog);
            pendingDialog = null;
        } else if (getView() == null) throw new RuntimeException("View was null on Resume. Why?");
        if (!bioCalib) {
            getView().findViewById(R.id.tool_button_2).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.tool_button_2).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.tool_button_1:
                gcMain.swapTo(LocationMap.class, true);
                return true;
            case R.id.tool_button_2:
                gcMain.swapTo(Biocalibrate.class, true);
                return true;
            case R.id.tool_button_3:
                gcMain.swapTo(Imager.class, true);
                return true;
            case R.id.tool_button_4:
                gcMain.swapTo(Amplifier.class, true);
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
                displayString = currentString.substring(0, counter);
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

        if (tool.equals("biocalibrate")) {
            getView().findViewById(R.id.tool_button_2).setVisibility(View.GONE);
        }
    }

    public void ShowTool(String tool) {
        if (tool.equals("biocalibrate")) {
            getView().findViewById(R.id.tool_button_2).setVisibility(View.VISIBLE);
        }
    }
}
