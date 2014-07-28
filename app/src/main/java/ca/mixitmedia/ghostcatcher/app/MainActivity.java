package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.Tools.*;
import ca.mixitmedia.ghostcatcher.experience.*;
import ca.mixitmedia.ghostcatcher.views.*;

public class MainActivity extends Activity implements View.OnClickListener {

    public ExperienceManager experienceManager;
    public gcLocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    gcEngine.init(this);
        Tools.init(this);
        locationManager = new gcLocationManager();
        experienceManager = new ExperienceManager(this);
		if (savedInstanceState == null)  //Avoid overlapping fragments.
			getFragmentManager().beginTransaction().add(R.id.fragment_container, Tools.communicator).commit();
    }


	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            for(Uri uri : Utils.getNdefIntentURIs(intent)) {
                gcEngine.Access().UpdateLocation(gcLocation.fromURI(uri));
            }
		}
	}

	public void onClick(View view) {
		if (Tools.Current().checkClick(view)) return;
		if (view instanceof ToolLightButton)swapTo(((ToolLightButton)view).toolFragment);
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
        gcEngine.detatch();
		SoundManager.stop();
		super.onDestroy();
	}

	public void swapTo(ToolFragment tool) {
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, tool)
					.commit();
	}


}

