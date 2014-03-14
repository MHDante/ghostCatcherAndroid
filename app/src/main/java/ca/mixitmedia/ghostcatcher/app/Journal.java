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

public class Journal extends Activity {

    float gearsize = 200;
    View backGear;
    View journalGear;

    static String[] items = {"lorem", "ipsum", "dolor", "sit", "amet",
            "consectetuer", "adipiscing", "elit", "morbi", "vel", "ligula",
            "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat",
            "placerat", "ante", "porttitor", "sodales", "pellentesque",
            "augue", "purus"};
    AdapterViewFlipper flipper;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_journal);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);

        flipper = (AdapterViewFlipper) findViewById(R.id.NotesFlipper);
        flipper.setAdapter(new ArrayAdapter<String>(this, R.layout.flip_tester, items));
        flipper.setFlipInterval(2000);
        flipper.startFlipping();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGears();
                }
            }, 500);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.rotate_in_from_left, R.anim.rotate_out_to_right);
    }

    public void showGears() {
        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        backGear.animate().translationX(0);
        journalGear.animate().translationX(0);
    }

    public void onClick(View view) {
        String action = "";
        switch (view.getId()) {
            case R.id.back_gear_btn:
                action = "back";
                break;
        }
        AnimationHandler ah = new AnimationHandler(this, action);
        backGear.animate().setListener(ah);
        hideGears();
    }

    public void hideGears() {
        backGear.animate().translationX(-gearsize);
        journalGear.animate().translationX(gearsize);
    }

    class AnimationHandler extends AnimatorListenerAdapter {
        Activity caller;
        String action;

        AnimationHandler(Activity a, String action) {
            this.caller = a;
            this.action = action;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Debug.out("OnEnd");
            if (action.equals("back"))
                caller.onBackPressed();
        }
    }
}
