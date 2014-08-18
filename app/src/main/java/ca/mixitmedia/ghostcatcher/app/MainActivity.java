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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
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
import ca.mixitmedia.ghostcatcher.views.DrawerActivity;

public class MainActivity extends DrawerActivity implements View.OnClickListener {

    public ExperienceManager experienceManager;
    public gcLocationManager locationManager;
    public gcEngine gcEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tools.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            gcEngine = gcParser.parseXML(this);
        } catch (IOException | XmlPullParserException e){
            Log.e("Huge Mistake", e.getMessage());
            Utils.messageDialog(this,"Error", e.getMessage());
        }
        SoundManager.init(this);
        locationManager = new gcLocationManager(this);
        experienceManager = new ExperienceManager(this);
        if (savedInstanceState == null)  //Avoid overlapping fragments.
            //getFragmentManager().beginTransaction().add(R.id.fragment_container, Tools.communicator).commit();
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

            Button close = (Button) dialog.findViewById(R.id.buttonClose);
            Button enableTools = (Button) dialog.findViewById(R.id.enableTools);
            Button Location1 = (Button) dialog.findViewById(R.id.location1);
            Location1.setText("Ryerson Theatre");
            Button Location2 = (Button) dialog.findViewById(R.id.location2);
            Location2.setText("Lake Devo");
            Button Location3 = (Button) dialog.findViewById(R.id.location3);
            Location3.setText("Arch");
            Button Location4 = (Button) dialog.findViewById(R.id.location4);
            Location4.setText("TransMedia Zone");
            // if button is clicked, close the custom dialog
            dialog.setOnDismissListener(new OnDismissListener() {
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
			        ArrayList<gcLocation> locations = new ArrayList<>(gcEngine.getAllLocations().values());
			        gcLocation target = locations.get(0);
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
        if (Tools.Current() == tool ) return;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, tool)
                .commit();
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        swapTo(Tools.All().get(position));
    }

    @Override
    protected ListAdapter getDrawerListAdapter() {
        return new ArrayAdapter<>(
                this,//getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                Tools.All());

        //return new BaseAdapter() {
        //    @Override
        //    public int getCount() {return Tools.All().size();}
        //    @Override
        //    public Object getItem(int position) {Tools.All().get(position);}
        //    @Override
        //    public long getItemId(int position) {return position;}
//
        //    @Override
        //    public View getView(int position, View convertView, ViewGroup parent) {
        //        return null;
        //    }
        //}
    }
}

