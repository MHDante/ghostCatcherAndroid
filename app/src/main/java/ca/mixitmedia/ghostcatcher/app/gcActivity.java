package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import ca.mixitmedia.ghostcatcher.utils.Debug;

/**
 * Created by Dante on 2014-03-21.
 */
public class gcActivity extends FragmentActivity {


    float gearsize = 200;
    View backGear;
    View journalGear;

    protected void setGears() {
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);
    }

    protected void setGears(int backGearID, int otherGearID) {
        backGear = findViewById(backGearID);
        journalGear = findViewById(otherGearID);

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

    protected void showGears() {
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

    protected void hideGears() {
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
