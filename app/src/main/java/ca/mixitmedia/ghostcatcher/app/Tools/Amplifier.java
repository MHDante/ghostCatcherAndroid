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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.views.SignalBeaconView;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

    private BeaconManager beaconManager;

    SignalBeacon beaconOne = new SignalBeacon();
    SignalBeacon beaconTwo = new SignalBeacon();
    SignalBeacon beaconThree = new SignalBeacon();

    int currentStrength;

    int averageStrengthBeaconOne;
    int averageStrengthBeaconTwo;
    int averageStrengthBeaconThree;

    int beaconOneSoundStream;
    int beaconTwoSoundStream;
    int beaconThreeSoundStream;

    float beaconOneVolumeControl;
    float beaconTwoVolumeControl;
    float beaconThreeVolumeControl;

    final Uri rootUri = gcEngine.Access().root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        beaconManager = new BeaconManager(getActivity());
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);
        final TextView debugTextField = (TextView) view.findViewById(R.id.debug_text_field);

        final List<String> addressList = new ArrayList<>(Arrays.asList("CB:ED:AB:9A:95:E4","FB:6B:2C:F1:C6:B7","DB:A6:5D:34:24:3B"));


        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons){
                Map<String, Beacon> beaconMap = new HashMap<String, Beacon>();

                for (String address : addressList){
                    for(int i=0; i<beacons.size(); i++) {
                        if(beacons.get(i).getMacAddress().equals(address))
                            beaconMap.put(address, beacons.get(i));
                    }
                }

                for (String address : addressList){
                    Beacon currentBeacon = beaconMap.get(address);

                    if(!(currentBeacon==null)){
                        currentStrength = Math.abs(currentBeacon.getRssi());

                        if (currentBeacon.equals(beaconMap.get(addressList.get(0))))
                            averageStrengthBeaconOne = beaconOne.getStrength(currentStrength);
                        else if(currentBeacon.equals(beaconMap.get(addressList.get(1))))
                            averageStrengthBeaconTwo = beaconTwo.getStrength(currentStrength);
                        else if(currentBeacon.equals(beaconMap.get(addressList.get(2))))
                            averageStrengthBeaconThree = beaconThree.getStrength(currentStrength);

                    }
                }

                    debugTextField.setText(beaconOne.toString()+"\n"+beaconTwo.toString()+"\n"+beaconThree.toString());
            }

        });

        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        overlay.setImageURI(rootUri.buildUpon().appendPath("skins").appendPath("amplifier").appendPath("amplifier_overlay.png").build());

        return view;
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
        beaconOne.iterator = 0;
        beaconTwo.iterator = 0;
        beaconThree.iterator = 0;

        gcMain.soundPool.stop(beaconOneSoundStream);
        gcMain.soundPool.stop(beaconTwoSoundStream);
        gcMain.soundPool.stop(beaconThreeSoundStream);

    }

    @Override
    public void afterAnimation(boolean enter) {
        super.afterAnimation(enter);

        if(enter){
            View view = this.getView();

            beaconOneSoundStream = gcMain.soundPool.play(gcMain.sounds.creepyChains, 0, 0, 1, -1, 1);
            beaconTwoSoundStream = gcMain.soundPool.play(gcMain.sounds.test_beep_one, 0, 0, 1, -1, 1);
            beaconThreeSoundStream = gcMain.soundPool.play(gcMain.sounds.test_beep_two, 0, 0, 1, -1, 1);

            final SignalBeaconView beaconView = new SignalBeaconView(getActivity(), null);
            FrameLayout beaconViewHolder = (FrameLayout) view.findViewById(R.id.signal_beacon_holder);

            beaconViewHolder.addView(beaconView);
            //TODO: Volume control inconsistent and sound cuts entire sometimes.
            beaconView.setWaveFunction(new SignalBeaconView.WaveFunction() {
                @Override
                public float getGraphYWaveOne(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 90;
                    beaconOneVolumeControl = (0.8f - ((averageStrengthBeaconOne*1.5f)/100));

                    gcMain.soundPool.setVolume(beaconOneSoundStream,beaconOneVolumeControl,beaconOneVolumeControl);

                    return amplitude + (float) (((amplitude/1.5) - (averageStrengthBeaconOne*(Math.log10(averageStrengthBeaconOne*2)))) * Math.sin(graphX  / period + time));
                }
                public float getGraphYWaveTwo(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/230);
                    float period = 50;
                    beaconTwoVolumeControl = (0.8f - ((averageStrengthBeaconTwo*1.5f)/100));

                    gcMain.soundPool.setVolume(beaconTwoSoundStream,beaconTwoVolumeControl,beaconTwoVolumeControl);

                    return amplitude + (float) (((amplitude/1.5) - (averageStrengthBeaconTwo*(Math.log10(averageStrengthBeaconTwo*2)))) * Math.sin(graphX / period + time));
                }
                public float getGraphYWaveThree(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 70;
                    beaconThreeVolumeControl = (0.8f - ((averageStrengthBeaconThree*1.5f)/100));

                    gcMain.soundPool.setVolume(beaconThreeSoundStream,beaconThreeVolumeControl,beaconThreeVolumeControl);

                    return amplitude + (float) (((amplitude/1.5) - (averageStrengthBeaconThree*(Math.log10(averageStrengthBeaconThree*2)))) * Math.sin(graphX /period + time));
                }
            });

        }


    }

    @Override
    public Uri getGlyphUri() {
        return (rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_amplifier.png").build());
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.amplifier_button:
                gcMain.soundPool.stop(dialogueStream);
                dialogueStream = gcMain.playSound(gcMain.sounds.testSoundClip);
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
			gcMain.playSound(gcMain.sounds.leverRoll);
			return R.animator.rotate_in_from_left;
		}
        return R.animator.rotate_out_to_right;
    }

    private class SignalBeacon{
        int currentStrength,averageStrength, strengthSum, iterator, signalQueueSize=5;
        double lowPassFilterInterval, highPassFilterInterval;
        int[] strengthQueue = new int[signalQueueSize];

        public void SignalBeacon(){
            iterator = 0;
            averageStrength = 0;
            currentStrength = 0;
            //TODO: Fix this filter thing
            lowPassFilterInterval = 0.75;
            highPassFilterInterval = 1.75;
        }

        public void setSignalQueueSize(int queueSize){  signalQueueSize = queueSize;}

        public void setSignalFilterInterval(double lowPassFilter, double highPassFilter){
            lowPassFilterInterval = lowPassFilter;
            highPassFilterInterval = highPassFilter;
        }

        public int getStrength(int measuredStrength){
            currentStrength = measuredStrength;

            if(iterator <= (signalQueueSize - 1)){
                strengthQueue[iterator] = currentStrength;
                averageStrength = currentStrength;
                iterator++;
            }
            else {
                strengthSum=0;
                if ((currentStrength < (averageStrength * 1.75)) && (currentStrength > (averageStrength * 0.75))) {
                    for (int j = (signalQueueSize - 1); j >= 0; j--) {
                        if (j == 0) {
                            strengthQueue[j] = currentStrength;
                        } else {
                            strengthQueue[j] = strengthQueue[j - 1];
                        }
                    }
                }
                for (int k = 0; k < signalQueueSize; k++)
                    strengthSum += strengthQueue[k];

                averageStrength = strengthSum / signalQueueSize;
            }

            return averageStrength;
        }

        public String toString(){
            return "RSSI: "+currentStrength+"  Sum: "+(averageStrength*signalQueueSize)+"  Average: "+averageStrength;
        }
    }

}
