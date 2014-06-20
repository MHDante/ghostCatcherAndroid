package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcTrigger;
import ca.mixitmedia.ghostcatcher.utils.Tuple;
import ca.mixitmedia.ghostcatcher.utils.Utils;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;

/**
 * Created by Dante on 2014-04-14.
 */
public class Communicator extends ToolFragment {

    private gcDialog currentDialog;
    private boolean dialogPending;
    private boolean isStarted;
    private boolean pause = false;
    private boolean userIsScrolling = false;
    TextView subtitleView;
    List<Integer> intervals = new ArrayList<>();

    public Communicator() {
    }//req'd


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_communicator, container, false);
        imgV = (ImageView) view.findViewById(R.id.character_portrait);
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
            currentDialog = gcDialog.get(gcEngine.Access().getCurrentSeqPt(), dialogId);
            if (getView() == null) {
                dialogPending = true;
                return;
            }
            setupDialog();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Reading Dialog files");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        gcMain.hideGears(true, false);
        try {
            if (dialogPending) {
                dialogPending = !dialogPending;
                setupDialog();
                isStarted = true;
            }
            if (portrait != null) imgV.setImageBitmap(MediaStore.Images.Media.getBitmap(
                    getActivity().getContentResolver(), portrait));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Reading Dialog files");
        }
    }

    @Override
    public void onPause() {
        gcMain.showGears();
        super.onPause();
    }

    ImageView imgV;

    public void setupDialog() throws IOException {
        gcAudio.playTrack(currentDialog.audio, false);
        for (int interval : currentDialog.parsed.keySet()) intervals.add(interval);
        intervals.add(currentDialog.duration);
        //int secondsElapsed = gcAudio.getPosition();
        startDialog();

        if (gcMain != null) {
            Bitmap image = MediaStore.Images.Media.getBitmap(
                    getActivity().getContentResolver(),
                    currentDialog.portraits.get(intervalCounter));

            imgV.setImageBitmap(image);
        }
        portrait = currentDialog.portraits.get(intervalCounter);
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_communicator);
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.sound:
                if (gcAudio.isPlaying()) gcAudio.pause();
                else gcAudio.play();
                //populateText("Hello world. ", true);
                pause = !pause;
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
                if (!pause)
                    counter += 1;  //increase every sec
                mHandler.obtainMessage(1).sendToTarget();

            }
        }, 0, 50);
    }

    int intervalCounter = 0;
    float currentPosition = 0f;
    Uri portrait;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            currentPosition += 50f;
            if (currentPosition > intervals.get(intervalCounter + 1) * 1000) {
                intervalCounter++;
                if (intervalCounter >= intervals.size() - 1) {
                    timer.cancel();
                    if (gcMain != null) {
                        imgV.setImageDrawable(gcMain.getResources().getDrawable(R.drawable.shine));
                        if (gcEngine.Access().getCurrentSeqPt().getAutoTrigger() != null)
                            gcMain.triggerLocation();
                    }
                    portrait = Utils.resIdToUri(R.drawable.shine);
                    isStarted = false;


                    return;
                }
                try {
                    if (gcMain != null) {
                    Bitmap image = MediaStore.Images.Media.getBitmap(
                            gcMain.getContentResolver(),
                            currentDialog.portraits.get(intervalCounter));
                    imgV.setImageBitmap(image);
                    }
                    portrait = currentDialog.portraits.get(intervalCounter);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            int num = intervals.get(intervalCounter);
            int stringLength = currentDialog.parsed.get(num).length();
            int difference = (intervals.get(intervalCounter + 1) - num) * 1000;
            int timePerLetter = (difference / stringLength);

            int currentLength = (int) (currentPosition - num) / timePerLetter;

            String displayString = currentDialog.parsed.get(num).substring(0, Math.min(currentLength, currentDialog.parsed.get(num).length() - 1));
            if (getView() != null) {
                populateText(displayString, false);
            }

        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    protected int getAnimatorId(boolean enter) {
        if(enter) gcMain.playSound(gcMain.sounds.metalClick);
        return (enter) ? R.animator.rotate_in_from_left : R.animator.rotate_out_to_left;
    }

}
