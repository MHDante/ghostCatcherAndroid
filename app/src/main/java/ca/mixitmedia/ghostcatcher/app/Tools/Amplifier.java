package ca.mixitmedia.ghostcatcher.app.Tools;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.views.SignalBeaconView;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

    private BeaconManager beaconManager;

    List<SignalBeacon> beaconList;

    int currentStrength;

    int  amplifierSoundOne, amplifierSoundTwo, amplifierSoundThree;

    final Uri rootUri = gcEngine.root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        beaconList = new ArrayList<>(Arrays.asList(
                new SignalBeacon("CB:ED:AB:9A:95:E4", amplifierSoundOne),
                new SignalBeacon("FB:6B:2C:F1:C6:B7", amplifierSoundTwo),
                new SignalBeacon("DB:A6:5D:34:24:3B", amplifierSoundThree)));

        beaconManager = new BeaconManager(getActivity());
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);
        final TextView debugTextField = (TextView) view.findViewById(R.id.debug_text_field);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons){
                for (SignalBeacon beacon : beaconList){
                    Beacon currentBeacon = null;

                    for(Beacon aBeacon : beacons) {
                        if (aBeacon.getMacAddress().equals(beacon.macAddress))
                            currentBeacon = aBeacon;
                    }

                    if(currentBeacon!=null){
                        currentStrength = Math.abs(currentBeacon.getRssi());
                        if(currentStrength == 0)
                            currentStrength = 100;
                    }
                    else
                        currentStrength = 100;

                    beacon.calculateAverage(currentStrength);
                }

                    debugTextField.setText(beaconList.get(0).toString()+"\n"+beaconList.get(1).toString()+"\n"+beaconList.get(2).toString());
            }

        });

        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        overlay.setImageURI(rootUri.buildUpon().appendPath("skins").appendPath("amplifier").appendPath("amplifier_overlay.png").build());

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amplifierSoundOne = SoundManager.soundPool.load(gcMain, R.raw.amplifier_sound_1, 1);
        amplifierSoundTwo = SoundManager.soundPool.load(gcMain, R.raw.amplifier_sound_2, 1);
        amplifierSoundThree = SoundManager.soundPool.load(gcMain, R.raw.amplifier_sound_3, 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SoundManager.soundPool.unload(amplifierSoundOne);
        SoundManager.soundPool.unload(amplifierSoundTwo);
        SoundManager.soundPool.unload(amplifierSoundThree);
    }

    @Override
    public void onStart() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {
                    Log.e("BEACON", "Cannot start ranging", e);
                }
            }
        });
        super.onStart();
    }

    @Override
    public void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.e("BEACON", "Cannot stop but it does not matter now", e);
        }

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();

        for(SignalBeacon beacon : beaconList){
            beacon.iterator = 0;
            beacon.stopSound();
        }

        SoundManager.stop();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        for(SignalBeacon beacon : beaconList){
//            if(beacon.soundStream == 0)
//                beacon.initializeBeaconSound();
//            else
//                SoundManager.soundPool.resume(beacon.soundStream);
//        }
//
//        SoundManager.play();
//
//    }

    @Override
    public void afterAnimation(boolean enter) {
        super.afterAnimation(enter);

        if (enter){
            View view = this.getView();

            SoundManager.playTrack(Uri.parse("android.resource://"+getActivity().getPackageName()+"/raw/amplifier_main"),true);
            for(SignalBeacon beacon : beaconList)
                beacon.initializeBeaconSound();

            final SignalBeaconView beaconView = new SignalBeaconView(getActivity(), null);
            FrameLayout beaconViewHolder = (FrameLayout) view.findViewById(R.id.signal_beacon_holder);

            beaconViewHolder.addView(beaconView);
            beaconView.setWaveFunction(new SignalBeaconView.WaveFunction() {
                @Override
                public float getGraphYWaveOne(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 90;

                    beaconList.get(0).alterVolume();

                    return amplitude + (float) (((amplitude/1.5) - ((beaconList.get(0).averageStrength)*(Math.log10((beaconList.get(0).averageStrength)*2)))) * Math.sin(graphX  / period + time));
                }
                public float getGraphYWaveTwo(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/230);
                    float period = 50;

                    beaconList.get(1).alterVolume();

                    return amplitude + (float) (((amplitude/1.5) - ((beaconList.get(1).averageStrength)*(Math.log10((beaconList.get(1).averageStrength)*2)))) * Math.sin(graphX / period + time));
                }
                public float getGraphYWaveThree(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 70;

                    beaconList.get(2).alterVolume();

                    return amplitude + (float) (((amplitude/1.5) - ((beaconList.get(2).averageStrength)*(Math.log10((beaconList.get(2).averageStrength)*2)))) * Math.sin(graphX /period + time));
                }
            });
        }
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.amplifier_button:
                SoundManager.soundPool.stop(dialogueStream);
                dialogueStream = SoundManager.playSound(SoundManager.Sounds.testSoundClip);
                return true;
            default:
                return false;
        }
    }

    @Override
    public pivotOrientation getPivotOrientation(boolean enter) {
        return pivotOrientation.LEFT;
    }

    protected int getAnimatorId(boolean enter) {
        if (enter) {
			SoundManager.playSound(SoundManager.Sounds.leverRoll);
			return R.animator.rotate_in_from_left;
		}
        return R.animator.rotate_out_to_right;
    }

    private class SignalBeacon{
        int currentStrength,averageStrength, strengthSum, iterator, signalQueueSize=5,soundStream, sound;
        String macAddress;
        int[] strengthQueue = new int[signalQueueSize];
        float volumeInitial = 0.8f, volumeControl;

        public SignalBeacon(String beaconAddress, int soundFile){
            sound = soundFile;
            macAddress = beaconAddress;
            iterator = 0;
            averageStrength = 0;
            currentStrength = 0;
        }

        public void setSignalQueueSize(int queueSize){  signalQueueSize = queueSize;}

        public void calculateAverage(int measuredStrength){
            currentStrength = measuredStrength;

            if (iterator <= (signalQueueSize - 1)){
                strengthQueue[iterator] = currentStrength;
                averageStrength = currentStrength;
                iterator++;
            }
            else {
                strengthSum=0;
                for (int j = (signalQueueSize - 1); j >= 0; j--) {
                    if (j == 0) {
                        strengthQueue[j] = currentStrength;
                    } else {
                        strengthQueue[j] = strengthQueue[j - 1];
                    }

                }
                for (int k = 0; k < signalQueueSize; k++)
                    strengthSum += strengthQueue[k];

                averageStrength = strengthSum / signalQueueSize;
            }
        }

        public void initializeBeaconSound(){
            soundStream = SoundManager.soundPool.play(sound, 0, 0, 1, -1, 1);
        }

        public void stopSound(){
            SoundManager.soundPool.stop(soundStream);
        }

        public void alterVolume(){
            volumeControl = (volumeInitial - ((averageStrength-50)/50));
            SoundManager.soundPool.setVolume(soundStream,volumeControl,volumeControl);
        }

        public String toString(){
            return "RSSI: "+currentStrength+"  Sum: "+(averageStrength*signalQueueSize)+"  Average: "+averageStrength;
        }
    }
}
