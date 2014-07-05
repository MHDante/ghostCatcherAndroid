package ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import ca.mixitmedia.ghostcatcher.app.gcMediaService;

/**
 * Created by Dante on 15/03/14.
 */
public class gcAudio {

    private static Context getContext() {
        Context context = gcEngine.Access().context;
        if (context == null)
            throw new RuntimeException("gcAudio accessed before gcEngine Initialized;");
        return context;
    }

    public static boolean isPlaying() {
        return (gcMediaService.isStarted && !gcMediaService.isPaused);
    }

    public static void play() {
        if (!gcMediaService.isStarted)
            Log.e("audio", "Tried to play without a playlist set.");
        if (!isPlaying()) {
            Intent i = new Intent(gcMediaService.ACTION_TOGGLE_PLAY);
            getContext().sendBroadcast(i);
        }
    }

    public static void pause() {
        if (isPlaying()) {
            Intent i = new Intent(gcMediaService.ACTION_TOGGLE_PLAY);
            getContext().sendBroadcast(i);
        }
    }

    public static void stop() {
        Intent i = new Intent(gcMediaService.ACTION_STOP);
        getContext().sendBroadcast(i);
    }

    public static void playTrack(final Uri track, final boolean loop) {
        if (!gcMediaService.isStarted) {
            getContext().startService(new Intent(getContext(), gcMediaService.class));
        }
        new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(gcMediaService.ACTION_PLAY_TRACK);
                i.putExtra(gcMediaService.EXTRA_TRACK, track);
                i.putExtra(gcMediaService.EXTRA_LOOP, loop);
                getContext().sendBroadcast(i);
                if (!gcMediaService.receiverRegistered) {
                    new Handler().postDelayed(this, 50);
                }
            }
        }.run();
    }

    public static void queueTrack(final Uri track, final boolean loop) {
        if (!gcMediaService.isStarted) {
            getContext().startService(new Intent(getContext(), gcMediaService.class));
        }
        new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(gcMediaService.ACTION_QUEUE_TRACK);
                i.putExtra(gcMediaService.EXTRA_TRACK, track);
                i.putExtra(gcMediaService.EXTRA_LOOP, loop);
                getContext().sendBroadcast(i);
                if (!gcMediaService.receiverRegistered) {
                    new Handler().postDelayed(this, 50);
                }
            }
        }.run();
    }

    public static void stopLooping() {
        Intent i = new Intent(gcMediaService.ACTION_END_LOOP);
        getContext().sendBroadcast(i);
    }

    public static int getDuration() {
        if (!gcMediaService.isStarted) {
            Log.e("audio", "Can't get duration if it doesn't exist");
            return -1;
        }
        return gcMediaService.duration / 1000;
    }

	//TODO: unused
    public static int getPosition() {
        return gcMediaService.mPlayer.getCurrentPosition();
    }
}
