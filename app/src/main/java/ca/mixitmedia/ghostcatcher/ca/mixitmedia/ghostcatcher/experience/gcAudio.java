package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import ca.mixitmedia.ghostcatcher.app.gcMediaService;

/**
 * Created by Dante on 15/03/14.
 */
public class gcAudio {
    private static Context ctxt() {
        Context ctxt = gcEngine.getInstance().context;
        if (ctxt == null)
            throw new RuntimeException("gcAudio accessed before gcEngine Initialized;");
        return ctxt;
    }

    public static boolean isPlaying() {
        return (!gcMediaService.isPaused && gcMediaService.isStarted);
    }

    public static void play() {
        if (!gcMediaService.isStarted)
            Log.e("audio", "Tried to play without a playlist set.");
        if (!isPlaying()) {
            Intent i = new Intent(gcMediaService.ACTION_TOGGLE_PLAY);
            ctxt().sendBroadcast(i);
        }
    }

    public static void pause() {
        if (isPlaying()) {
            Intent i = new Intent(gcMediaService.ACTION_TOGGLE_PLAY);
            ctxt().sendBroadcast(i);
        }
    }

    public static void stop() {
        Intent i = new Intent(gcMediaService.ACTION_STOP);
        ctxt().sendBroadcast(i);
    }

    public static void playTrack(final String track, final boolean loop) {
        if (!gcMediaService.isStarted) {
            ctxt().startService(new Intent(ctxt(), gcMediaService.class));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gcMediaService.isStarted) {
                    Intent i = new Intent(gcMediaService.ACTION_PLAY_TRACK);
                    i.putExtra(gcMediaService.EXTRA_TRACK, track);
                    i.putExtra(gcMediaService.EXTRA_LOOP, loop);
                    ctxt().sendBroadcast(i);
                } else {
                    new Handler().postDelayed(this, 100);
                }
            }
        }, 100);

    }

    public static void queueTrack(final String track, final boolean loop) {
        if (!gcMediaService.isStarted) {
            ctxt().startService(new Intent(ctxt(), gcMediaService.class));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gcMediaService.isStarted) {
                    Intent i = new Intent(gcMediaService.ACTION_QUEUE_TRACK);
                    i.putExtra(gcMediaService.EXTRA_TRACK, track);
                    i.putExtra(gcMediaService.EXTRA_LOOP, loop);
                    ctxt().sendBroadcast(i);
                } else {
                    new Handler().postDelayed(this, 100);
                }
            }
        }, 100);
    }

    public static void stopLooping() {
        Intent i = new Intent(gcMediaService.ACTION_END_LOOP);
        ctxt().sendBroadcast(i);
    }

}
