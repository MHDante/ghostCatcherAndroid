package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.utils.Debug;


public class communicator extends Activity {

    float gearsize = 200;
    View backGear;
    View journalGear;
    Context ctxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ctxt = this;
        gcEngine.getInstance().init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicator);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGears();
                }
            }, 500);
        }
    }

    public void onClick(View view) {
        String action = "";
        switch (view.getId()) {
            case R.id.back_gear_btn:
                action = "back";
                break;
            case R.id.journal_gear_btn:
                action = "journal";
                break;
            case R.id.tool_button_1:
                action = "map";
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

    public void showGears() {
        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        backGear.animate().translationX(0);
        journalGear.animate().translationX(0);
    }

    public void startJournal() {
        startActivity(new Intent(this, Journal.class));
        overridePendingTransition(R.anim.rotate_in_from_right, R.anim.rotate_out_to_left);
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
            Debug.out("OnEnd");
            if (action.equals("back"))
                caller.onBackPressed();
            if (action.equals("journal"))
                caller.startJournal();
            if (action.equals("map")) {
                startActivity(new Intent(ctxt, Tester.class));
                overridePendingTransition(R.anim.rotate_in_from_right, R.anim.rotate_out_to_left);
            }

        }
    }
}
