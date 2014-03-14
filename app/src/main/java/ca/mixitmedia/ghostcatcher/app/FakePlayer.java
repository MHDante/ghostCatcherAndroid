package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ca.mixitmedia.ghostcatcher.utils.Debug;

public class FakePlayer extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void startPlayer(View v) {
        Intent i = new Intent(this, gcMediaService.class);
        i.setAction(gcMediaService.ACTION_PLAY_TRACK);
        i.putExtra(gcMediaService.EXTRA_TRACK, "main");
        startService(i);
    }

    public void stopPlayer(View v) {
        stopService(new Intent(this, gcMediaService.class));
    }
}