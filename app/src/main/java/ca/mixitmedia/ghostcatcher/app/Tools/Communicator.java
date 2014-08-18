package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.ProximityTest;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.experience.gcDialog;

/**
 * Created by Dante on 2014-04-14
 */
public class Communicator extends ToolFragment {

    gcDialog currentDialog;
    TextView subtitleView;
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
        subtitleView = (TextView) (view.findViewById(R.id.subtitle_text_view));
        imageView = (ImageView) view.findViewById(R.id.character_portrait);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ocean_sans.ttf");
        subtitleView.setTypeface(font);
        subtitleView.setTextSize(20);
        subtitleView.setTextColor(Color.GREEN);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstRun) startDialog(); else CheckForMessages();
        firstRun = false;
    }

    public void CheckForMessages() {
        if (currentDialog == null) {
            if (pendingMessages.size() > 0 && pendingMessages.peek().action.getType() == gcAction.Type.DIALOG) {
                startDialog();
            }
        }else {
            phraseAdder.run();
        }
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            //case R.id.sound:
            //    if (SoundManager.isPlaying()) SoundManager.pause();
            //    else SoundManager.play();
            //    return true;
            //case R.id.help:
            //    if (proximityTest.getStatus() == AsyncTask.Status.PENDING) proximityTest.execute();
            //    return true;
            default:
                return false;
        }
    }

    protected void startDialog() {
        ToolMessage message = pendingMessages.peek();
        if (message.action.getType() == gcAction.Type.DIALOG) {
            currentDialog = gcDialog.get(gcMain.gcEngine.getCurrentSeqPt(),message.action.getData());
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
                CharSequence prevText = subtitleView.getText();
                subtitleView.setText(prevText + currentDialog.parsed.get((int) currentInterval));
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

}
