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

import java.util.Calendar;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.views.SignalBeaconView;

public class Amplifier extends ToolFragment {

    private int dialogueStream = 0;

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

    private BeaconManager beaconManager = new BeaconManager(gcEngine.Access().context);

    int averageStrengthBeaconOne=0;
    int averageStrengthBeaconTwo=0;
    int averageStrengthBeaconThree=0;

    int iteratorBeaconOne = 0;
    int iteratorBeaconTwo = 0;
    int iteratorBeaconThree = 0;

    int[] beaconOneStrengthQueue = new int[5];
    int[] beaconTwoStrengthQueue = new int[5];
    int[] beaconThreeStrengthQueue = new int[5];

    int currentStrengthBeaconOne;
    int currentStrengthBeaconTwo;
    int currentStrengthBeaconThree;

    String beaconOneDebugString;
    String beaconTwoDebugString;
    String beaconThreeDebugString;

    float fakeTime;

    final Uri rootUri = gcEngine.Access().root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_amplifier, container, false);
        final TextView debugTextField = (TextView) view.findViewById(R.id.debug_text_field);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons){

                for(int i=0; i<beacons.size(); i++){
                    currentStrengthBeaconOne = Math.abs(beacons.get(i).getRssi());

                    if(beacons.get(i).getMacAddress().equals("CB:ED:AB:9A:95:E4")){
                        currentStrengthBeaconOne = Math.abs(beacons.get(i).getRssi());

                        if(iteratorBeaconOne <=4){
                            beaconOneStrengthQueue[iteratorBeaconOne] = currentStrengthBeaconOne;
                            averageStrengthBeaconOne = currentStrengthBeaconOne;
                            iteratorBeaconOne++;
                        }
                        else {
                            int beaconOneSum = 0;
                            if (!(currentStrengthBeaconOne > (averageStrengthBeaconOne * 1.75)) && !(currentStrengthBeaconOne < (averageStrengthBeaconOne * 0.75))) {
                                for (int j = 4; j >= 0; j--) {
                                    if (j == 0) {
                                        beaconOneStrengthQueue[j] = currentStrengthBeaconOne;
                                    } else {
                                        beaconOneStrengthQueue[j] = beaconOneStrengthQueue[j - 1];
                                    }
                                }
                            }

                            for (int k = 0; k < beaconOneStrengthQueue.length; k++)
                                beaconOneSum += beaconOneStrengthQueue[k];

                            averageStrengthBeaconOne = beaconOneSum / 5;
                        }
                        beaconOneDebugString = "ONE->RSSI: "+currentStrengthBeaconOne+"   Sum: "+(averageStrengthBeaconOne*5)+"  Average: "+averageStrengthBeaconOne;
                    }

                    if(beacons.get(i).getMacAddress().equals("FB:6B:2C:F1:C6:B7")){
                        currentStrengthBeaconTwo = Math.abs(beacons.get(i).getRssi());

                        if (iteratorBeaconTwo <= 4) {
                            beaconTwoStrengthQueue[iteratorBeaconTwo] = currentStrengthBeaconTwo;
                            averageStrengthBeaconTwo = currentStrengthBeaconTwo;
                            iteratorBeaconTwo++;
                        } else {
                            int beaconTwoSum = 0;
                            if (!(currentStrengthBeaconTwo > (averageStrengthBeaconTwo * 1.75)) && !(currentStrengthBeaconTwo < (averageStrengthBeaconTwo * 0.75))) {
                                for (int j = 4; j >= 0; j--) {
                                    if (j == 0) {
                                        beaconTwoStrengthQueue[j] = currentStrengthBeaconTwo;
                                    } else {
                                        beaconTwoStrengthQueue[j] = beaconTwoStrengthQueue[j - 1];
                                    }
                                }
                            }

                            for (int k = 0; k < beaconTwoStrengthQueue.length; k++)
                                beaconTwoSum += beaconTwoStrengthQueue[k];

                            averageStrengthBeaconTwo = beaconTwoSum / 5;
                        }
                        beaconTwoDebugString = "\nTWO->RSSI: " + currentStrengthBeaconTwo + "   Sum: " + (averageStrengthBeaconTwo * 5) + "  Average: " + averageStrengthBeaconTwo;
                    }
                    if(beacons.get(i).getMacAddress().equals("DB:A6:5D:34:24:3B")){
                        currentStrengthBeaconThree = Math.abs(beacons.get(i).getRssi());

                        if(iteratorBeaconThree <=4){
                            beaconThreeStrengthQueue[iteratorBeaconThree] = currentStrengthBeaconThree;
                            averageStrengthBeaconThree = currentStrengthBeaconThree;
                            iteratorBeaconThree++;
                        }
                        else {
                            int beaconThreeSum = 0;
                            if (!(currentStrengthBeaconThree > (averageStrengthBeaconThree * 1.75)) && !(currentStrengthBeaconThree < (averageStrengthBeaconThree * 0.75))) {
                                for (int j = 4; j >= 0; j--) {
                                    if (j == 0) {
                                        beaconThreeStrengthQueue[j] = currentStrengthBeaconThree;
                                    } else {
                                        beaconThreeStrengthQueue[j] = beaconThreeStrengthQueue[j - 1];
                                    }

                                }
                            }

                            for (int k = 0; k < beaconThreeStrengthQueue.length; k++)
                                beaconThreeSum += beaconThreeStrengthQueue[k];

                            averageStrengthBeaconThree = beaconThreeSum / 5;
                        }
                        beaconThreeDebugString = "\nTHREE->RSSI: "+currentStrengthBeaconThree+"   Sum: "+(averageStrengthBeaconThree*5)+"  Average: "+averageStrengthBeaconThree;
                    }
                    debugTextField.setText(beaconOneDebugString+beaconTwoDebugString+beaconThreeDebugString);
                }
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
        iteratorBeaconOne = 0;
        iteratorBeaconTwo = 0;
        iteratorBeaconThree = 0;
    }

    @Override
    public void afterAnimation(boolean enter) {
        super.afterAnimation(enter);
        if(enter){
            View view = this.getView();

            final SignalBeaconView beaconView = new SignalBeaconView(getActivity(), null);
            FrameLayout beaconViewHolder = (FrameLayout) view.findViewById(R.id.signal_beacon_holder);

            beaconViewHolder.addView(beaconView);

            beaconView.setWaveFunction(new SignalBeaconView.WaveFunction() {
                @Override
                public float getGraphYWaveOne(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 80;

                    return amplitude + (float) (((amplitude/2) - (averageStrengthBeaconOne*(Math.log10(averageStrengthBeaconOne)))) * Math.sin(graphX  / period + time));
                }
                public float getGraphYWaveTwo(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/230);
                    float period = 50;

                    return amplitude + (float) (((amplitude/2) - (averageStrengthBeaconTwo*(Math.log10(averageStrengthBeaconTwo)))) * Math.pow(Math.cos(graphX / period + time),3));
                }
                public float getGraphYWaveThree(float graphX, float amplitude) {
                    float time = (Calendar.getInstance().get(Calendar.MILLISECOND)/250);
                    float period = 75;

                    return amplitude + (float) (((amplitude/2) - (averageStrengthBeaconThree*(Math.log10(averageStrengthBeaconThree)))) * Math.pow(Math.sin(graphX /period + time),5));
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


}
