package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        int currentPointerCount = ev.getPointerCount();

        if (debugging && currentPointerCount >= 4) {
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
                    debugging = true;
                }
            });

            enableTools.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(ToolFragment t: Tools.All()) t.setEnabled(true);
                    }
            });

	        View.OnClickListener clickListener = new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
			        ArrayList<gcLocation> locations = new ArrayList<>(gcEngine.locations.values());
			        gcLocation target = locations.get(0);
			        switch (v.getId()) {
				        case R.id.location1:
                            target = gcEngine.locations.get("rye_theatre");
					        break;
				        case R.id.location2:
                            target = gcEngine.locations.get("lake_devo");
					        break;
				        case R.id.location3:
                            target = gcEngine.locations.get("arch");
					        break;
				        case R.id.location4:
                            target = gcEngine.locations.get("tmz");
					        break;
			        }
			        Toast.makeText(MainActivity.this, target.getTitle(), Toast.LENGTH_LONG).show();
			        experienceManager.UpdateLocation(target);
		        }
	        };
            Location1.setOnClickListener(clickListener);
	        Location2.setOnClickListener(clickListener);
	        Location3.setOnClickListener(clickListener);
	        Location4.setOnClickListener(clickListener);

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

