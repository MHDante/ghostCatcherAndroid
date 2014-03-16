package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.utils.Debug;

public class Tester extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((Button) findViewById(R.id.button1)).setText(button1);
        ((Button) findViewById(R.id.button2)).setText(button2);
        ((Button) findViewById(R.id.button3)).setText(button3);
        ((Button) findViewById(R.id.button4)).setText(button4);
        ((Button) findViewById(R.id.button5)).setText(button5);
        ((Button) findViewById(R.id.button6)).setText(button6);
        ((Button) findViewById(R.id.button7)).setText(button7);
        ((Button) findViewById(R.id.button8)).setText(button8);
    }

    public String button1 = "play";

    public void button1(View v) {
        gcAudio.play();
    }

    public String button2 = "pause";

    public void button2(View v) {
        gcAudio.pause();
    }

    public String button3 = "Stop";

    public void button3(View v) {
        gcAudio.stop();
    }

    public String button4 = "Stop Looping";

    public void button4(View v) {
        gcAudio.stopLooping();
    }

    public String button5 = "Play Track";

    public void button5(View v) {
        gcAudio.playTrack("main3", false);
    }

    public String button6 = "Queue Track";

    public void button6(View v) {
        gcAudio.queueTrack("main3", false);
    }

    public String button7 = "Play Track and loop";

    public void button7(View v) {
        gcAudio.playTrack("main3", true);
    }

    public String button8 = "Queue Track and Loop";

    public void button8(View v) {
        gcAudio.queueTrack("main3", true);
    }
}