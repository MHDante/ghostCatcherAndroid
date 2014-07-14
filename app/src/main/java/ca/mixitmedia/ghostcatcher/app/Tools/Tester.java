package ca.mixitmedia.ghostcatcher.app.Tools;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.File;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Tester extends ToolFragment {

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

    private BeaconManager beaconManager = new BeaconManager(gcEngine.Access().context);

    final Uri rootUri = gcEngine.Access().root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d("BEACON", "Ranged beacons: " + beacons);
            }
        });
        View view = inflater.inflate(R.layout.tool_tester, container, false);

        ((Button) view.findViewById(R.id.tester_button_1)).setText(tester_button_1);
        ((Button) view.findViewById(R.id.tester_button_2)).setText(tester_button_2);
        ((Button) view.findViewById(R.id.tester_button_3)).setText(tester_button_3);
        ((Button) view.findViewById(R.id.tester_button_4)).setText(tester_button_4);
        ((Button) view.findViewById(R.id.tester_button_5)).setText(tester_button_5);
        ((Button) view.findViewById(R.id.tester_button_6)).setText(tester_button_6);
        ((Button) view.findViewById(R.id.tester_button_7)).setText(tester_button_7);
        ((Button) view.findViewById(R.id.tester_button_8)).setText(tester_button_8);

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
    public void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    public String tester_button_1 = "play";

    //public void tester_button_1(View v) {
    //    gcAudio.play();
    //}

    public String tester_button_2 = "pause";

    //public void tester_button_2(View v) {
    //    gcAudio.pause();
    //}

    public String tester_button_3 = "Stop";

    //public void tester_button_3(View v) {
    //    gcAudio.stop();
    //}

    public String tester_button_4 = "Stop Looping";

    //public void tester_button_4(View v) {
    //    gcAudio.stopLooping();
    //}

    public String tester_button_5 = "Play Track";

    //public void tester_button_5(View v) {
    //    gcAudio.playTrack("main3", false);
    //}

    public String tester_button_6 = "Queue Track";

    //public void tester_button_6(View v) {
    //    gcAudio.queueTrack("main3", false);
    //}

    public String tester_button_7 = "Play Track and loop";

    //public void tester_button_7(View v) {
    //    gcAudio.playTrack("main3", true);
    //}

    public String tester_button_8 = "Queue Track and Loop";

    //public void tester_button_8(View v) {
    //    gcAudio.queueTrack("main3", true);
    //}


    @Override
    public boolean checkClick(View view) {
        Uri testSoundPath = Uri.fromFile(new File(gcEngine.Access().root + "/testsound.mp3"));

        switch (view.getId()) {
            case R.id.tester_button_1:
                gcAudio.play();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_2:
                gcAudio.pause();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_3:
                gcAudio.stop();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_4:
                gcAudio.stopLooping();
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_5:
                gcAudio.playTrack(testSoundPath, false);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_6:
                gcAudio.queueTrack(testSoundPath, false);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_7:
                gcAudio.playTrack(testSoundPath, true);
                //gcMain.swapTo("");
                return true;
            case R.id.tester_button_8:
                gcAudio.queueTrack(testSoundPath, true);
                //gcMain.swapTo("");
                return true;

            default:
                return false;
        }
    }

    @Override
    public Uri getGlyphUri() {
        return (rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_ghost_catcher.png").build());
    }
}
