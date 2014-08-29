package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.ProximityTest;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcAction;
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
    Boolean firstRun = true;
    public Communicator() {
    }//req'd

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setEnabled(true);
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_communicator, container, false);
        subtitleView = (Typewriter) (view.findViewById(R.id.subtitle_text_view));
        imageView = (ImageView) view.findViewById(R.id.character_portrait);
        biocalibrate = new Biocalibrate(view.findViewById(R.id.biocalibrate));

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ocean_sans.ttf");
        subtitleView.textView.setTypeface(font);
        subtitleView.textView.setTextSize(20);
        subtitleView.textView.setTextColor(Color.GREEN);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstRun) {
			startDialog();
			firstRun = false;
		}
		else CheckForMessages();
    }

    public void CheckForMessages() {
        if (currentDialog == null) {
            if (pendingMessages.size() > 0 && pendingMessages.peek().action.getType() == gcAction.Type.DIALOG) {
                biocalibrate = new Biocalibrate(getView().findViewById(R.id.biocalibrate));
                biocalibrate.show();
            }
        }else {
            phraseAdder.run();
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
        ToolMessage message = pendingMessages.peek();

        if (message.action.getType() == gcAction.Type.DIALOG) {
            biocalibrate.hide();
            currentDialog = gcDialog.get(gcMain.gcEngine.getCurrentSeqPt(), message.action.getData());
            startTime = System.currentTimeMillis();
            SoundManager.playTrack(currentDialog.audio, false);
            phraseAdder = new PhraseAdder();
            phraseAdder.run();

        }
    }

    class PhraseAdder implements Runnable {
        long pastInterval = -1;

        @Override
        public void run() {
            if (currentDialog == null || gcMain == null || currentDialog.intervals.size() < 1)
                return;
            long currentInterval = 0;
            long currentTime = Utils.TimeSince(startTime) / 1000;
            //Log.d("PhraseAdder","currentTime: " + currentTime);
            for (int interval : currentDialog.intervals) {
                //Log.d("PhraseAdder","checking Interval: " + interval);
                if (interval > currentTime)
                    break;
                currentInterval = interval;
                //Log.d("PhraseAdder","currentInterval: " + currentInterval);

            }
            //Log.d("PhraseAdder","checking difference between current:" + currentInterval + " and past : " + pastInterval);

            if (currentInterval > pastInterval) {
                subtitleView.concatenateText(currentDialog.parsed.get((int) currentInterval));
                Uri image =currentDialog.portraits.get((int) currentInterval);
                if (image !=null) {
                    Log.d("PA", image.getPath());
                    imageView.setImageURI(image);
                }
                pastInterval = currentInterval;
            }
            int duration =  currentDialog.getDuration();
            //Log.d("PhraseAdder","checking current time: " + currentTime + " vs duration:" + duration);

            if (currentTime < duration) {
            //    Log.d("PhraseAdder","restart: ");

                mHandler.postDelayed(this, 1000);
            } else {
            //    Log.d("PhraseAdder","end ");
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.shine));
                currentDialog = null;
                completeAction();
                CheckForMessages();
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
        Button fingerPrint;
        View holder;
        ImageView overlay;

        public Biocalibrate(View view) {
            holder = view;
            view.setTranslationY(Utils.GetScreenHeight(getActivity()));
            LoadingBar = (ProgressBar) holder.findViewById(R.id.calibrate_bar);
            LoadingBar.setMax(100);

            fingerPrint = (Button) holder.findViewById(R.id.biocalibrate_btn);
            fingerPrint.setOnTouchListener(this);

            overlay = (ImageView) holder.findViewById(R.id.fingerprint_mask);
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
                                int progress = (int) ((((float)totalDuration / (float)BiocalibrateDelay) * 100f));
                                Log.d("PROGRESS", ":"+progress);
                                LoadingBar.setProgress(progress);
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
                overlay.setImageResource(R.drawable.bio_calibrate_pressed);


            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                pressed = false;
                SoundManager.pauseFX();
                totalDuration += System.currentTimeMillis() - lastDown;
                getView().findViewById(R.id.calibrating_text).setVisibility(View.INVISIBLE);
                overlay.setImageResource(R.drawable.bio_calibrate_unpressed);
            }
            return false;
        }

        public void show() {
            LoadingBar.setProgress(0);
            holder.animate().translationY(0);
        }
        public void hide(){
            holder.animate().translationY(Utils.GetScreenHeight(getActivity()));
        }
    }
}
