package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class communicator extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicator);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();

        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void backClick(View view) {
        AnimationHandler ah = new AnimationHandler(this, "back");
        hideGears(ah);
    }

    public void journalClick(View view) {
        AnimationHandler ah = new AnimationHandler(this, "journal");
        hideGears(ah);
    }

    public void hideGears(AnimationHandler ah) {
        View backGear = findViewById(R.id.back_gear);
        View journalGear = findViewById(R.id.journal_gear);
        if (ah != null) {
            backGear.animate().setListener(ah).xBy(-200);
            journalGear.animate().setListener(ah).xBy(200);
        } else {
            backGear.animate().xBy(-200);
            journalGear.animate().xBy(200);
        }
    }

    public void startJournal() {
        throw new RuntimeException("NotImplemented");
    }

    class AnimationHandler extends AnimatorListenerAdapter {

        communicator caller;
        String action;

        AnimationHandler(communicator a, String action) {
            this.caller = a;
            this.action = action;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (action.equals("back"))
                caller.onBackPressed();
            if (action.equals("journal"))
                caller.startJournal();
        }
    }


}
