package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.app.Tools.Tools;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.experience.gcParser;
import ca.mixitmedia.ghostcatcher.views.LightButton;

public class MainActivity extends Activity implements View.OnClickListener {

    public ExperienceManager experienceManager;
    public gcLocationManager locationManager;
    public gcEngine gcEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            gcEngine = gcParser.parseXML(this);
        } catch (IOException | XmlPullParserException e){
            Log.e("Huge Mistake", e.getMessage());
            Utils.messageDialog(this,"Error", e.getMessage());
        }
        Tools.init(this);
        SoundManager.init(this);
        locationManager = new gcLocationManager(this);
        experienceManager = new ExperienceManager(this);
        if (savedInstanceState == null)  //Avoid overlapping fragments.
            getFragmentManager().beginTransaction().add(R.id.fragment_container, Tools.communicator).commit();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            for (Uri uri : Utils.getNdefIntentURIs(intent)) {
                experienceManager.UpdateLocation(uri);
            }
        }
    }

    boolean debugging = true;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int currentpointerCount = ev.getPointerCount();

        if (debugging&& currentpointerCount >=4){
            debugging = false;
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_debug);
            dialog.setTitle("Settings");

            Button close = (Button) dialog.findViewById(R.id.buttonClose);
            Button enableTools = (Button) dialog.findViewById(R.id.enableTools);
            Button Location1 = (Button) dialog.findViewById(R.id.location1);
            Location1.setText("RyeTheatre");
            Button Location2 = (Button) dialog.findViewById(R.id.location2);
            Location2.setText("Lake Devo");
            Button Location3 = (Button) dialog.findViewById(R.id.location3);
            Location3.setText("Arch");
            Button Location4 = (Button) dialog.findViewById(R.id.location4);
            Location4.setText("TMZ");
            // if button is clicked, close the custom dialog
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    debugging = true;
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            enableTools.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(ToolFragment t: Tools.All()) t.setEnabled(true);
                    }
            });
            Location1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gcLocation target = gcEngine.locations.get("rye_theatre");
                    Toast.makeText(MainActivity.this, target.getName(), Toast.LENGTH_LONG).show();
                    experienceManager.UpdateLocation(target);
                }
            });
            Location2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gcLocation target = gcEngine.locations.get("lake_devo");

                    Toast.makeText(MainActivity.this, target.getName(), Toast.LENGTH_LONG).show();
                    experienceManager.UpdateLocation(target);
                }
            });
            Location3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gcLocation target = gcEngine.locations.get("arch");

                    Toast.makeText(MainActivity.this, target.getName(), Toast.LENGTH_LONG).show();
                    experienceManager.UpdateLocation(target);
                }
            });
            Location4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gcLocation target = gcEngine.locations.get("tmz");

                    Toast.makeText(MainActivity.this, target.getName(), Toast.LENGTH_LONG).show();
                    experienceManager.UpdateLocation(target);
                }
            });

            dialog.show();
            }


        return super.dispatchTouchEvent(ev);
    }

    public void onClick(View view) {
        if (Tools.Current().checkClick(view)) return;

        if (view instanceof LightButton) {
            for (ToolFragment tf : Tools.All())
                if (tf.getToolLight() == view && tf.isEnabled())
                    swapTo(tf);
        }
    }

    @Override
    protected void onPause() {
        if (SoundManager.isPlaying()) SoundManager.pause();
        locationManager.removeUpdates(); //stop location updates
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.play();
        locationManager.requestSlowGPSUpdates();
    }

    @Override
    protected void onDestroy() {
        SoundManager.stop();
        super.onDestroy();
    }

    public void swapTo(ToolFragment tool) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, tool)
                .commit();
    }


}

