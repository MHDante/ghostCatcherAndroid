package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;

import ca.mixitmedia.ghostcatcher.utils.Debug;

public class Journal extends gcActivity {

    static String[] items = {"lorem", "ipsum", "dolor", "sit", "amet",
            "consectetuer", "adipiscing", "elit", "morbi", "vel", "ligula",
            "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat",
            "placerat", "ante", "porttitor", "sodales", "pellentesque",
            "augue", "purus"};
    AdapterViewFlipper flipper;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_journal);
        setGears();
        flipper = (AdapterViewFlipper) findViewById(R.id.NotesFlipper);
        flipper.setAdapter(new ArrayAdapter<String>(this, R.layout.flip_tester, items));
        flipper.setFlipInterval(2000);
        flipper.startFlipping();
    }
}
