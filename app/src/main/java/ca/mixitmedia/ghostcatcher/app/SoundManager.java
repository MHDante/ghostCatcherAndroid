package ca.mixitmedia.ghostcatcher.app;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Dante on 15/03/14.
 */
public class SoundManager {

    public static final int SOUND_POOL_MAX_STREAMS = 4;
    public static SoundPool soundPool;
    public static MainActivity gcMain;

    public static void init(MainActivity gcMain) {
        soundPool = new SoundPool(SOUND_POOL_MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        Sounds.loadSounds(gcMain);
        SoundManager.gcMain = gcMain;
    }

    private static Context getContext() {
        return gcMain;
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

    public static int getPosition() {
        return gcMediaService.mPlayer.getCurrentPosition();
    }

    public static int playSound(int soundName) {
        return soundPool.play(soundName, 0.3f, 0.3f, 1, 0, 1);
    }



    public static void resumeFX() {

        //throw new RuntimeException("NotImplemented");
    }

    public static void pauseFX() {
        //throw new RuntimeException("NotImplemented");
    }

    public static class Sounds {
        public static int metalClick, leverRoll, strangeMetalNoise, creepyChains, testSoundClip, calibrateSoundClip,amplifierMain,amplifierSoundOne, amplifierSoundTwo,amplifierSoundThree;

        public static void loadSounds(Context ctxt) {
            calibrateSoundClip = soundPool.load(ctxt, R.raw.gc_audio_amplifier, 1);
            amplifierMain = soundPool.load(ctxt, R.raw.amplifier_main, 1);
            amplifierSoundOne = soundPool.load(ctxt, R.raw.amplifier_sound_1, 1);
            amplifierSoundTwo = soundPool.load(ctxt, R.raw.amplifier_sound_2, 1);
            amplifierSoundThree = soundPool.load(ctxt, R.raw.amplifier_sound_3, 1);
            testSoundClip = soundPool.load(ctxt, R.raw.gc_audio_amplifier, 1);
            metalClick = soundPool.load(ctxt, R.raw.metal_click, 1);
            leverRoll = soundPool.load(ctxt, R.raw.lever_roll, 1);
            strangeMetalNoise = soundPool.load(ctxt, R.raw.strange_mechanical_noise, 1);
            creepyChains = soundPool.load(ctxt, R.raw.creepy_chains, 1);

        }
    }
}
