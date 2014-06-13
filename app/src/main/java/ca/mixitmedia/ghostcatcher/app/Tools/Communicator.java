package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.utils.Tuple;

/**
 * Created by Dante on 2014-04-14.
 */
public class Communicator extends ToolFragment {

    private gcDialog pendingDialog;
    private boolean pause = false;
    public Queue<Integer> timeSlots;
    private boolean userIsScrolling = false;
    TextView subtitleView;

    public Communicator() {
    }//req'd

    // TODO: Rename and change types and number of parameters
    public static Communicator newInstance(String param1) {
        Communicator fragment = new Communicator();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_communicator, container, false);
        return view;
    }

    private void setUpSubtitleView() {
        subtitleView = (TextView) (getView().findViewById(R.id.subtitle_text_view));
        ScrollView sv = (ScrollView) (getView().findViewById(R.id.subtitle_scroll_view));
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
        subtitleView.setTypeface(font);
        subtitleView.setTextSize(20);
    }

    @Override
    public void onStart() {
        super.onStart();
        populateText("", false);
        setUpSubtitleView();

    }

    private void populateText(String st, Boolean append) {
        TextView tv = (TextView) getView().findViewById(R.id.subtitle_text_view);
        if (append) st = tv.getText() + st;
        tv.setText(st);

        ScrollView sv = (ScrollView) getView().findViewById(R.id.subtitle_scroll_view);
        if (!userIsScrolling) sv.fullScroll(View.FOCUS_DOWN);
    }

    public void loadfile(String dialogId) {
        try {
            pendingDialog = gcDialog.get(gcEngine.Access().getCurrentSeqPt(), dialogId);
            if (getView() == null) return;
            setupDialog();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Reading Dialog files");
        }
    }

    public void setupDialog() throws IOException {
        ImageView imgV = (ImageView) getView().findViewById(R.id.character_portrait);
        Bitmap image = MediaStore.Images.Media.getBitmap(
                getActivity().getContentResolver(),
                pendingDialog.portraits.get(0));

        imgV.setImageBitmap(image);

        gcAudio.playTrack(pendingDialog.audio, false);
        startDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        gcMain.showGears();
        if (!gcMain.isToolEnabled(LocationMap.class))
            ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_1)).setImageAlpha(0);
        if (!gcMain.isToolEnabled(Biocalibrate.class))
            ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_2)).setImageAlpha(0);
        if (!gcMain.isToolEnabled(Imager.class))
            ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_3)).setImageAlpha(0);
        if (!gcMain.isToolEnabled(Amplifier.class))
            ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(255);
        else ((ImageView) getView().findViewById(R.id.tool_button_4)).setImageAlpha(0);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.tool_button_1:
                if (gcMain.isToolEnabled(LocationMap.class)) gcMain.swapTo(LocationMap.class, true);
                return true;
            case R.id.tool_button_2:
                Biocalibrate.hasBackStack = true;
                if (gcMain.isToolEnabled(Biocalibrate.class))
                    gcMain.swapTo(Biocalibrate.class, true);
                return true;
            case R.id.tool_button_3:
                if (gcMain.isToolEnabled(Imager.class)) gcMain.swapTo(Imager.class, true);
                return true;
            case R.id.tool_button_4:
                if (gcMain.isToolEnabled(Amplifier.class)) gcMain.swapTo(Amplifier.class, true);
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


    public void HideTool(Class tool) {

        gcMain.ToolMap.put(tool, new Tuple<>(false, gcMain.ToolMap.get(tool).second));
        View v;

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
