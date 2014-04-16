package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.utils.Debug;


public class MainActivity extends FragmentActivity implements ToolFragment.ToolInteractionListener {
    float gearsize = 200;
    View backGear;
    View journalGear;
    Context ctxt; //TODO:REMOVE.
    View fragmentContainer;
    CommunicatorFragment communicator;
    JournalFragment journal;
    gcMap map;
    BiocalibrateFragment biocalib;
    AmplifierFragment amplifier;
    TesterFragment tester;
    ImagerFragment imager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctxt = this;
        super.onCreate(savedInstanceState);
        gcEngine.getInstance().init(this);
        setContentView(R.layout.activity_main);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);
        //TODO: Implement settings, you lazy fool.
        communicator = CommunicatorFragment.newInstance("Settings");
        journal = JournalFragment.newInstance("Settings");
        map = gcMap.newInstance("Settings");
        biocalib = BiocalibrateFragment.newInstance("Settings");
        amplifier = AmplifierFragment.newInstance("Settings");
        tester = TesterFragment.newInstance("Settings");
        imager = ImagerFragment.newInstance("Settings");
        if (savedInstanceState != null) {
            return;//Avoid overlapping fragments.
        }
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, communicator)
                        //.addToBackStack(null)
                .commit();

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
        if (tf.checkClick(view)) return;
        switch (view.getId()) {
            case R.id.back_gear_btn:
                //hideGears("back");
                onBackPressed();
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

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
            //startActivity(new Intent(this, ImagerFragment.class));
        }
    }

    public void swapTo(String fragment) {
        if (fragment.equals("journal")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, journal)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("map")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, map)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("imager")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, imager)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("communicator")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, communicator)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("biocalibrate")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, biocalib)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("tester")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, tester)
                    .addToBackStack(null)
                    .commit();
        } else if (fragment.equals("amplifier")) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, amplifier)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void startDialog(String dialog) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, communicator)
                .addToBackStack(null)
                .commit();
        communicator.loadfile(dialog);
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
        startActivity(new Intent(this, JournalFragment.class));
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
            else if (action.equals("journal"))
                caller.startJournal();
            else if (action.equals("map")) {
                startActivity(new Intent(ctxt, gcMap.class));
                overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            }
            //else if (action.equals("tester")) {
            //    startActivity(new Intent(ctxt, TesterFragment.class));
            //    overridePendingTransition(R.animator.rotate_in_from_right, R.animator.rotate_out_to_left);
            //}
            else
                throw new RuntimeException("This system is being depreciated, and you passed an incorrect parameter");
        }
    }


}
