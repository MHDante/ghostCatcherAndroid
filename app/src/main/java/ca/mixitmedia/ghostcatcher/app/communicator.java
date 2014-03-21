package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.utils.Debug;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.*;

import static com.google.android.gms.common.GooglePlayServicesUtil.*;


public class communicator extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    float gearsize = 200;
    View backGear;
    View journalGear;
    Context ctxt;
    private GoogleApiClient mGoogleApiClient;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        ctxt = this;
        gcEngine.getInstance().init(this);
        setContentView(R.layout.activity_communicator);
        backGear = findViewById(R.id.back_gear);
        journalGear = findViewById(R.id.journal_gear);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Maps.API)
//                .addScope(Maps.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int isGoogleThere = isGooglePlayServicesAvailable(this);
        if (isGoogleThere != ConnectionResult.SUCCESS) {
            getErrorDialog(isGoogleThere, this, 0);
        }

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

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
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
                startActivity(new Intent(ctxt, gcMap.class));
                overridePendingTransition(R.anim.rotate_in_from_right, R.anim.rotate_out_to_left);
            }

        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            mResolvingError = true;
            Dialog d = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    this, REQUEST_RESOLVE_ERROR);
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mResolvingError = false;
                }
            });
            d.show();
        }
    }
}
