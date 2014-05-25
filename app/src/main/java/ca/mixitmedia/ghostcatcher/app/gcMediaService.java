package ca.mixitmedia.ghostcatcher.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class gcMediaService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    public static final String INTENT_BASE_NAME = "ca.mixitmedia.ghostCatcher.audio";
    public static final String ACTION_TOGGLE_PLAY = INTENT_BASE_NAME + ".TOGGLE_PLAY";
    public static final String ACTION_STOP = INTENT_BASE_NAME + ".STOP";
    public static final String ACTION_PLAY_TRACK = INTENT_BASE_NAME + ".PLAY_TRACK";
    public static final String ACTION_QUEUE_TRACK = INTENT_BASE_NAME + ".QUEUE_TRACK";
    public static final String ACTION_END_LOOP = INTENT_BASE_NAME + ".END_LOOP";
    public static final String EXTRA_TRACK = INTENT_BASE_NAME + ".EXTRA_TRACK";
    public static final String EXTRA_LOOP = INTENT_BASE_NAME + ".EXTRA_LOOP";

    static final int NOTIFICATION_MPLAYER = 56584;
    public static int duration = -1;
    public static boolean isStarted;
    public static boolean isPaused;
    static Notification status;
    gcEngine engine = gcEngine.Access();
    MediaPlayer mPlayer = null;
    static Queue<Uri> tracks = new ConcurrentLinkedQueue<Uri>();

    boolean looping;

    BroadcastReceiver receiver = new AudioReceiver();
    public static boolean receiverRegistered;

    ///////////////////////////////////Service methods
    @Override
    public void onCreate() {

        super.onCreate();
        IntentFilter i = new IntentFilter();

        i.addAction(ACTION_TOGGLE_PLAY);
        i.addAction(ACTION_STOP);
        i.addAction(ACTION_PLAY_TRACK);
        i.addAction(ACTION_QUEUE_TRACK);
        i.addAction(ACTION_END_LOOP);

        registerReceiver(receiver, i);
        receiverRegistered = true;
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateNotification();
        startForeground(NOTIFICATION_MPLAYER, status);
        return (START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        mPlayer.release();
    }

    public IBinder onBind(Intent intent) {
        return (null);
    }

    ///////////////////////////////////Media Player Listeners
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        duration = mp.getDuration();
        isPaused = false;
        updateNotification();
    }

    public void onCompletion(MediaPlayer mp) {
        duration = -1;
        if (tracks.peek() != null) {
            try {
                Uri track = tracks.remove();
                if (looping && tracks.size() > 1) tracks.add(track);
                mPlayer.reset();
                mPlayer.setDataSource(getApplicationContext(), track);
                mPlayer.prepareAsync();

            } catch (IOException e) {
                Log.e("AudioPlayer", "Error:" + e.getMessage());
            }
        } else {
            stop();
        }
    }


    ///////////////////////////////////Broadcast Receiver
    private class AudioReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_TOGGLE_PLAY)) {
                if (isStarted) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        isPaused = true;
                        updateNotification();
                    } else {
                        mPlayer.start();
                        duration = mPlayer.getDuration();
                        isPaused = false;
                        updateNotification();
                    }
                } else startPlaying();
            } else if (intent.getAction().equals(ACTION_STOP)) {
                stop();
            } else if (intent.getAction().equals(ACTION_PLAY_TRACK)) {
                String track = intent.getStringExtra(EXTRA_TRACK);
                looping = intent.getBooleanExtra(EXTRA_LOOP, false);
                tracks = new ConcurrentLinkedQueue<Uri>();
                tracks.add(engine.getSoundUri(track));
                startPlaying();
            } else if (intent.getAction().equals(ACTION_QUEUE_TRACK)) {
                String track = intent.getStringExtra(EXTRA_TRACK);
                looping = intent.getBooleanExtra(EXTRA_LOOP, false);
                tracks.add(engine.getSoundUri(track));
                if (mPlayer.isLooping() && tracks.size() > 1) mPlayer.setLooping(false);
            }
        }
    }

    ///////////////////////////////////Utility methods
    void stop() {
        receiverRegistered = false;
        isStarted = false;
        mPlayer.stop();
        tracks = new ConcurrentLinkedQueue<Uri>();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_MPLAYER);
        stopSelf();

    }

    void startPlaying() {

        if (tracks.size() == 0) {
            Log.e("audio", "Tried to play without a playlist set.");
            return;
        }
        mPlayer.reset();

        if (tracks.size() == 1) mPlayer.setLooping(looping);

        try {
            Uri track = tracks.remove();
            if (looping && tracks.size() > 1) tracks.add(track);
            mPlayer.setDataSource(getApplicationContext(), track);
            mPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e("AudioPlayer", "Error:" + e.getMessage());
        }
        isStarted = true;
    }

    void updateNotification() {
        int requestID = (int) System.currentTimeMillis();
        RemoteViews statusBarView = new RemoteViews(getPackageName(), R.layout.status_bar);
        Uri ImgUri = engine.getCurrentSeqPt().getLocations().get(0).getImage();
        Bitmap nextLocation = gcEngine.readBitmap(getApplicationContext(), ImgUri);
        statusBarView.setImageViewBitmap(R.id.icon, nextLocation);
        statusBarView.setTextViewText(R.id.title, "Ghost Catcher");
        statusBarView.setTextViewText(R.id.to_do, engine.getNextToDo());

        statusBarView.setImageViewResource(R.id.status_bar_play,
                (isPaused || !isStarted) ? R.drawable.btn_playback_play : R.drawable.btn_playback_pause);


        statusBarView.setOnClickPendingIntent(R.id.status_bar_play,
                PendingIntent.getBroadcast(getApplicationContext(), requestID, new Intent(ACTION_TOGGLE_PLAY), PendingIntent.FLAG_CANCEL_CURRENT));
        statusBarView.setOnClickPendingIntent(R.id.status_bar_clear,
                PendingIntent.getBroadcast(getApplicationContext(), requestID + 2, new Intent(ACTION_STOP), PendingIntent.FLAG_CANCEL_CURRENT));


        status = new Notification();
        status.contentView = statusBarView;
        status.icon = R.drawable.ghost;
        status.contentIntent = PendingIntent.getActivity(this, requestID + 1,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_MPLAYER, status);

    }

}
