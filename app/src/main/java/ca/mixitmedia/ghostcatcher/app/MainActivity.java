package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.utils.Debug;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.*;

import static com.google.android.gms.common.GooglePlayServicesUtil.*;


public class MainActivity extends FragmentActivity implements ToolFragment.ToolInteractionListener {
    float gearsize = 200;
    View backGear;
    View journalGear;
    Context ctxt; //TODO:REMOVE.
    View fragmentContainer;
    CommunicatorFragment communicator = CommunicatorFragment.newInstance("Settings");
    Journal journal = Journal.newInstance("Settings");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctxt = this;
        super.onCreate(savedInstanceState);
        gcEngine.getInstance().init(this);
        setContentView(R.layout.activity_main);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);

        if (savedInstanceState != null) {
            return;//Avoid overlapping fragments.
        }
        getFragmentManager().beginTransaction().add(R.id.fragment_container, communicator).commit();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGears();
                }
            }, 500);
        }
    }

    public void onClick(View view) {
        ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        tf.checkClick(view);
        switch (view.getId()) {
            case R.id.back_gear_btn:
                hideGears("back");
                break;
            case R.id.journal_gear_btn:
                swapTo("journal");
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO: doStuff
    }

    public void swapTo(String fragment) {
        if (fragment.equals("journal")) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left)
                    .replace(R.id.fragment_container, journal)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("communicator")) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left)
                    .replace(R.id.fragment_container, journal)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void hideGears(String action) {
        AnimationHandler ah = new AnimationHandler(this, action);
        backGear.animate().setListener(ah);
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
        overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
    }

    class AnimationHandler extends AnimatorListenerAdapter {
        MainActivity caller;
        String action;

        AnimationHandler(MainActivity a, String action) {
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
                startActivity(new Intent(ctxt, gcMap.class));
                overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            }
            if (action.equals("tester")) {
                startActivity(new Intent(ctxt, Tester.class));
                overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            }
        }
    }

}
