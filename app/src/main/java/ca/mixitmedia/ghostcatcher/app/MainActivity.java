package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
import java.util.Arrays;

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

        if (debugging && ev.getPointerCount() >= 4) {
            debugging = false;
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_debug);
            dialog.setTitle("Settings");

	        ArrayList<Button> LocationButtons = new ArrayList<>(Arrays.asList(
					(Button) dialog.findViewById(R.id.location1),
			        (Button) dialog.findViewById(R.id.location2),
			        (Button) dialog.findViewById(R.id.location3),
			        (Button) dialog.findViewById(R.id.location4)));
	        LocationButtons.get(0).setText("Ryerson Theatre");
	        LocationButtons.get(1).setText("Lake Devo");
	        LocationButtons.get(2).setText("Arch");
	        LocationButtons.get(3).setText("TransMedia Zone");

            // if button is clicked, close the custom dialog
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    debugging = true;
                }
            });
	        dialog.findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    debugging = true;
                }
            });

	        View.OnClickListener clickListener = new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
			        gcLocation target = (new ArrayList<>(gcEngine.getAllLocations().values())).get(0);
			        switch (v.getId()) {
				        case R.id.location1:
                            target = gcEngine.getAllLocations().get("rye_theatre");
					        break;
				        case R.id.location2:
                            target = gcEngine.getAllLocations().get("lake_devo");
					        break;
				        case R.id.location3:
                            target = gcEngine.getAllLocations().get("arch");
					        break;
				        case R.id.location4:
                            target = gcEngine.getAllLocations().get("tmz");
					        break;
				        case R.id.enableTools:
					        for (ToolFragment t: Tools.All()) t.setEnabled(true);
					        return;
			        }
			        Toast.makeText(MainActivity.this, target.getTitle(), Toast.LENGTH_LONG).show();
			        experienceManager.UpdateLocation(target);
		        }
	        };
	        dialog.findViewById(R.id.enableTools).setOnClickListener(clickListener);
	        for (Button b : LocationButtons) b.setOnClickListener(clickListener);

            dialog.show();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClick(View view) {
        if (Tools.Current().checkClick(view)) return;

        if (view instanceof LightButton) {
            for (ToolFragment tf : Tools.All()) {
	            if (tf.getToolLight() == view && tf.isEnabled()) swapTo(tf);
            }
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
        if (Tools.Current() == tool) return;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, tool)
                .commit();
    }
}

