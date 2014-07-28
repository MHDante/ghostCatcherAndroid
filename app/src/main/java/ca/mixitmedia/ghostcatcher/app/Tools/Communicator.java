package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.ProximityTest;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcDialog;
import ca.mixitmedia.ghostcatcher.views.Typewriter;

/**
 * Created by Dante on 2014-04-14
 */
public class Communicator extends ToolFragment {

    gcDialog currentDialog;
    Typewriter subtitleView;
    Biocalibrate biocalibrate;
    ImageView imageView;
    long startTime;
    ProximityTest proximityTest = new ProximityTest() {
        @Override
        public void HandleServerMessage(String s) {
            Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
        }
    };
    Handler mHandler = new Handler();
    Runnable phraseAdder = new PhraseAdder();

    public Communicator() {
    }//req'd

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_communicator, container, false);
        subtitleView = (Typewriter) (view.findViewById(R.id.subtitle_text_view));
        imageView = (ImageView) view.findViewById(R.id.character_portrait);
        biocalibrate = new Biocalibrate(view);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ocean_sans.ttf");
        subtitleView.textView.setTypeface(font);
        subtitleView.textView.setTextSize(20);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        CheckForMessages();
    }

    public void CheckForMessages() {
        if (currentDialog == null) {
            if (pendingMessages.size() > 0) {
                biocalibrate.show();
            }
        }
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.sound:
                if (SoundManager.isPlaying()) SoundManager.pause();
                else SoundManager.play();
                return true;
            case R.id.help:
                if (proximityTest.getStatus() == AsyncTask.Status.PENDING) proximityTest.execute();
                return true;
            default:
                return false;
        }
    }

    protected void startDialog() {
        ToolMessage message = pendingMessages.remove();
        if (message.data instanceof gcDialog) {
            currentDialog = (gcDialog) message.data;
            SoundManager.playTrack(currentDialog.audio, false);
            gcDialog.getDuration();
        }
    }

    class PhraseAdder implements Runnable {
        long pastInterval = -1;

        @Override
        public void run() {
            if (currentDialog == null || gcMain == null || currentDialog.intervals.size() < 1)
                return;
            long currentInterval = 0;
            for (int interval : currentDialog.intervals) {
                if (interval > Utils.TimeSince(startTime) / 1000) break;
                currentInterval = interval;
            }
            if (currentInterval > pastInterval) {
                subtitleView.concatenateText(currentDialog.parsed.get((int) currentInterval));
                imageView.setImageURI(currentDialog.portraits.get((int) currentInterval));
                pastInterval = currentInterval;
            }
            if (Utils.TimeSince(startTime) < gcDialog.getDuration()) {
                mHandler.postDelayed(this, 1000);
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.shine));
                currentDialog = null;

            }
        }
    }

    public class Biocalibrate implements View.OnTouchListener {

        static final int BiocalibrateDelay = 1500;
        boolean started;
        long lastDown;
        long totalDuration;
        boolean pressed;
        ProgressBar LoadingBar;
        ImageButton fingerPrint;

        public Biocalibrate(View view) {
            LoadingBar = (ProgressBar) view.findViewById(R.id.calibrate_bar);
            LoadingBar.setMax(100);

            fingerPrint = (ImageButton) view.findViewById(R.id.biocalibrate_btn);
            fingerPrint.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDown = System.currentTimeMillis();
                pressed = true;
                if (!started) {
                    SoundManager.playSound(SoundManager.Sounds.calibrateSoundClip);
                    started = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (LoadingBar != null) {
                                if (pressed) {
                                    totalDuration += System.currentTimeMillis() - lastDown;
                                    lastDown = System.currentTimeMillis();
                                }
                                LoadingBar.setProgress((int) ((totalDuration / BiocalibrateDelay) * 100f));
                                if (totalDuration > BiocalibrateDelay) {
                                    startDialog();
                                } else {
                                    handler.postDelayed(this, 100);
                                }
                            }
                        }
                    }, 100);
                } else SoundManager.resumeFX();

                getView().findViewById(R.id.calibrating_text).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.calibrate_pressed_layout).setAlpha(1.0f);


            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                pressed = false;
                SoundManager.pauseFX();
                totalDuration += System.currentTimeMillis() - lastDown;
                getView().findViewById(R.id.calibrating_text).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.calibrate_pressed_layout).setAlpha(0);
            }
            return false;
        }

        public void show() {

        }
    }
}
