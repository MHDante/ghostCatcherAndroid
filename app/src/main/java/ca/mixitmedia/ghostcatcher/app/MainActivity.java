package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.Tools.Amplifier;
import ca.mixitmedia.ghostcatcher.app.Tools.Biocalibrate;
import ca.mixitmedia.ghostcatcher.app.Tools.Communicator;
import ca.mixitmedia.ghostcatcher.app.Tools.Imager;
import ca.mixitmedia.ghostcatcher.app.Tools.Journal;
import ca.mixitmedia.ghostcatcher.app.Tools.LocationMap;
import ca.mixitmedia.ghostcatcher.app.Tools.RFDetector;
import ca.mixitmedia.ghostcatcher.app.Tools.Tester;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.experience.gcActionManager;
import ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.experience.gcTrigger;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;


public class MainActivity extends Activity implements
		LocationListener, View.OnClickListener {

	public Map<String, Uri> imageFileLocationMap;
	static final int GPS_SLOW_MIN_UPDATE_TIME_MS = 60000; //60 seconds
	static final int GPS_SLOW_MIN_UPDATE_DISTANCE_M = 50; //50 meters
	//private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static boolean transitionInProgress;
	public final int SOUND_POOL_MAX_STREAMS = 4;
	private final static int NOTIF_ID = 2013567;
	public Map<Class, ToolLightButton> ToolMap;
	public SoundPool soundPool;
	public Sounds sounds;
	public gcActionManager actionManager = new gcActionManager() {
		@Override
		public void startDialog(String dialogId) {
			getTool(Communicator.class).loadfile(dialogId);
		}

		@Override
		public void enableTool(String toolName) {
			for (Class c : ToolMap.keySet()) {
				if (c.getSimpleName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().equals(toolName.toLowerCase())) {
					ToolMap.get(c).setEnabled(true);
					return;
				}
			}
			throw new RuntimeException("Could not Find Tool: " + toolName);
		}

		@Override
		public void disableTool(String toolName) {
			for (Class c : ToolMap.keySet()) {
				if (c.getSimpleName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().equals(toolName.toLowerCase())) {
					ToolMap.get(c).setEnabled(false);
					return;
				}
			}
			throw new RuntimeException("Could not Find Tool: " + toolName);
		}

		@Override
		public void enableTrigger(String triggerId) {
			gcEngine.Access().getCurrentSeqPt().getTrigger(Integer.parseInt(triggerId)).setEnabled(true);
		}
	};

	//////////////////LifeCycle
	LocationManager locationManager;
	Location currentGPSLocation;
	/**
	 * the minimal GPS update interval, in milliseconds
	 */
	int GPSMinUpdateTimeMS;
	/**
	 * the minimal GPS update interval, in meters.
	 */
	int GPSMinUpdateDistanceM;
	GestureDetector detector;
	boolean toolHolderShown = true;
	private Handler decorViewHandler = new Handler();
	private gcLocation playerLocationInStory;
	private Runnable decor_view_settings = new Runnable() {
		public void run() {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
			);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		soundPool = new SoundPool(SOUND_POOL_MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);

	    sounds = new Sounds(soundPool);
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    super.onCreate(savedInstanceState);
	    gcEngine.init(this);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setContentView(R.layout.activity_main);
	    ToolMap = new HashMap<Class, ToolLightButton>() {{
		    put(Communicator.class, getToolLight(Communicator.class, R.id.tool_light_back));
		    put(Journal.class, getToolLight(Journal.class, R.id.tool_light_journal));
		    put(LocationMap.class, getToolLight(LocationMap.class, R.id.tool_light_1));
		    put(Biocalibrate.class, getToolLight(Biocalibrate.class, R.id.tool_light_2));
		    put(Amplifier.class, getToolLight(Amplifier.class, R.id.tool_light_3));
		    put(Tester.class, getToolLight(Tester.class, R.id.tool_light_4));
		    put(Imager.class, getToolLight(Imager.class, R.id.tool_light_5));
		    put(RFDetector.class, getToolLight(RFDetector.class, R.id.tool_light_6));
	    }};
	    showTool(Communicator.class);
	    showTool(Journal.class);

		if (savedInstanceState == null) {  //Avoid overlapping fragments.
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, getTool(Biocalibrate.class))
					.commit();
		}
		onNewIntent(getIntent());
		onLocationChanged(null);
		detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

			private int swipe_Min_Distance = 100;
			private int swipe_Min_Velocity = 100;

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			                       float velocityY) {

				final float xDistance = Math.abs(e1.getX() - e2.getX());
				final float yDistance = Math.abs(e1.getY() - e2.getY());

				velocityX = Math.abs(velocityX);
				velocityY = Math.abs(velocityY);
				boolean result = false;

				Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int height = size.y;

				float gestureAreaLeft, gestureAreaRight, gestureAreaTop, gestureAreaBottom;

				gestureAreaLeft = width * (15f / 100f);
				gestureAreaRight = width - gestureAreaLeft;
				gestureAreaTop = height * (15f / 100f);
				gestureAreaBottom = height - gestureAreaTop;

				if (velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance) {
					if (getCurrentFragment() instanceof LocationMap) {
						if ((e1.getX() < gestureAreaLeft) || (e1.getX() > gestureAreaRight)) {
							if (e1.getX() > e2.getX()) {
								onClick(ToolMap.get(Journal.class));
							} else {
								onClick(ToolMap.get(Communicator.class));
							}
						}
					} else if (e1.getX() > e2.getX()) {
						if (!(getCurrentFragment() instanceof Journal))
							onClick(ToolMap.get(Journal.class));
					} else {
						if (!(getCurrentFragment() instanceof Communicator))
							onClick(ToolMap.get(Communicator.class));
					}

					result = true;
				} else if (velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance) {
					if(getCurrentFragment() instanceof LocationMap){
						if(toolHolderShown){
							gestureAreaTop = findViewById(R.id.tool_holder).getHeight();
						}
						if((e1.getY() < gestureAreaTop) || (e1.getY() > gestureAreaBottom)){
							if(e1.getY() > e2.getY()) {
								if (toolHolderShown && !transitionInProgress)
									toggleToolMenu();
							} else {
								if (!toolHolderShown && !transitionInProgress)
									toggleToolMenu();
							}
						}
					}
					else if(e1.getY() > e2.getY()) {
						if (toolHolderShown && !transitionInProgress)
							toggleToolMenu();
					} else {
						if (!toolHolderShown && !transitionInProgress)
							toggleToolMenu();
					}
					result = true;
				}
				return result;
			}
		});

		createImageURIs();

        ImageView frame_right = (ImageView) findViewById(R.id.frame_right);
        ImageView frame_left = (ImageView) findViewById(R.id.frame_left);
        ImageView ad_holder = (ImageView) findViewById(R.id.ad_holder);
        ImageView tool_selector = (ImageView) findViewById(R.id.overlay);
        ImageView back_button = (ImageView) findViewById(R.id.back_button);
        ImageView journal_button = (ImageView) findViewById(R.id.journal_button);
        ImageView background = (ImageView) findViewById(R.id.background);

//TODO: URIify the ToolLightButtons
//		ToolLightButton back_button_light = (ToolLightButton)findViewById(R.id.tool_light_back);
//     	ToolLightButton journal_button_light = (ToolLightButton)findViewById(R.id.tool_light_journal);
//        ToolLightButton button1 = (ToolLightButton)findViewById(R.id.tool_light_1);
//        ToolLightButton button2 = (ToolLightButton)findViewById(R.id.tool_light_2);
//        ToolLightButton button3 = (ToolLightButton)findViewById(R.id.tool_light_3);
//        ToolLightButton button4 = (ToolLightButton)findViewById(R.id.tool_light_4);
//        ToolLightButton button5 = (ToolLightButton)findViewById(R.id.tool_light_5);
//        ToolLightButton button6 = (ToolLightButton)findViewById(R.id.tool_light_6);
//        ToolLightButton button7 = (ToolLightButton)findViewById(R.id.tool_light_7);

        background.setImageURI(imageFileLocationMap.get("background"));
        frame_left.setImageURI(imageFileLocationMap.get("frame_left"));
        frame_right.setImageURI(imageFileLocationMap.get("frame_right"));
        ad_holder.setImageURI(imageFileLocationMap.get("ad_holder"));
        tool_selector.setImageURI(imageFileLocationMap.get("tool_selector"));
        back_button.setImageURI(imageFileLocationMap.get("gear_button"));
        journal_button.setImageURI(imageFileLocationMap.get("gear_button"));

		frame_left.setScaleType(ImageView.ScaleType.FIT_START);
		frame_right.setScaleType(ImageView.ScaleType.FIT_END);


    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		// Call onTouchEvent of SimpleGestureFilter class
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

    private <T extends ToolFragment> ToolLightButton getToolLight(Class<T> cls, int resID) {
	    ToolLightButton ret = (ToolLightButton) findViewById(resID);
	    try {
		    ret.setToolFragment(cls.newInstance());
	    } catch (InstantiationException | IllegalAccessException e) {
		    throw new RuntimeException(e);
        }
        ret.setSrc(BitmapFactory.decodeFile(ret.getToolFragment().getGlyphUri().getEncodedPath()));
        ret.setEnabled(true);
        ret.setOnClickListener(this);
        return ret;
    }

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				for (NdefMessage message : msgs) {
					for (NdefRecord record : message.getRecords()) {
						Uri uri = record.toUri(); //Ignore the api warning, this is for demo, during which we will have api 16 at least
						if (uri != null && uri.getScheme().equals("troubadour") && uri.getHost().equals("ghostcatcher.mixitmedia.ca")) {
							String path = uri.getLastPathSegment();
							String[] tokens = path.split("\\.");
							String type = tokens[1];
							String id = tokens[0];
							if (type.equals("location")) {
								gcLocation loc = gcEngine.Access().getLocation(id);
                                if (loc == null)
								    Toast.makeText(this, "Location: " + id + " was not found", Toast.LENGTH_LONG).show();
								else onLocationChanged(loc);
							}
						}
					}
				}
			}
		}
	}

	public void onClick(View view) {
		//get current fragment
		if (transitionInProgress) return; //todo:hack
		ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
		//todo: abstract
		if (tf.checkClick(view)) return;

		if (view instanceof ToolLightButton) {

    public void swapTo(ToolFragment tool) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, tool)
                .commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LightButton.RefreshAll();
            }
        }, 200);
    }

	public void openButtonClicked(View view) {
		((RFDetector) getCurrentFragment()).setLidState(true, false);
	}

	public void closeButtonClicked(View view) {
		((RFDetector) getCurrentFragment()).setLidState(false, false);
	}
}

