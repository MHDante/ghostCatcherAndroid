package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.Tools.*;
import ca.mixitmedia.ghostcatcher.experience.gcActionManager;
import ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.experience.gcSeqPt;
import ca.mixitmedia.ghostcatcher.experience.gcTrigger;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;


public class MainActivity extends Activity implements
        LocationListener, View.OnClickListener {

    public Map<String, Uri> imageFileLocationMap;
	static final int GPS_SLOW_MIN_UPDATE_TIME_MS = 60000; //60 seconds
	static final int GPS_SLOW_MIN_UPDATE_DISTANCE_M = 50; //50 meters
	private final static int
			CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static boolean transitionInProgress;
    public final int SOUND_POOL_MAX_STREAMS = 4;
	private final int NOTIF_ID = 2013567;
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
	private boolean useDecorView;
	private gcLocation playerLocationInStory;
	private Runnable decor_view_settings = new Runnable() {
		public void run() {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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

        ToolLightButton back_button_light = (ToolLightButton)findViewById(R.id.tool_light_back);
        ToolLightButton journal_button_light = (ToolLightButton)findViewById(R.id.tool_light_journal);

        ToolLightButton button1 = (ToolLightButton)findViewById(R.id.tool_light_1);
        ToolLightButton button2 = (ToolLightButton)findViewById(R.id.tool_light_2);
        ToolLightButton button3 = (ToolLightButton)findViewById(R.id.tool_light_3);
        ToolLightButton button4 = (ToolLightButton)findViewById(R.id.tool_light_4);
        ToolLightButton button5 = (ToolLightButton)findViewById(R.id.tool_light_5);
        ToolLightButton button6 = (ToolLightButton)findViewById(R.id.tool_light_6);
        ToolLightButton button7 = (ToolLightButton)findViewById(R.id.tool_light_7);

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
        /*
        * TODO: So I'm getting a NullPointerException which shouldn't come from the URI path builder but I
        * tried the toString method and other getPath methods so I think it's the decodeFile method but I can't really
        * think of another way to get a bitmap from a URI.
        */
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

					    if (uri != null) {
						    if (uri.getScheme().equals("troubadour") && uri.getHost().equals("ghostcatcher.mixitmedia.ca")) {

							    String path = uri.getLastPathSegment();
							    String[] tokens = path.split("\\.");
							    String type = tokens[1];
							    String id = tokens[0];
							    if (type.equals("location")) {
								    gcLocation loc = gcEngine.Access().getLocation(id);
								    Toast.makeText(this, "Location: " + id + " was not found", Toast.LENGTH_LONG);
								    onLocationChanged(loc.asAndroidLocation());
							    }
						    }
					    }
				    }
			    }
		    }
	    }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if (hasFocus && useDecorView) {
		    decorViewHandler.post(decor_view_settings);
	    }
    }

    public void onClick(View view) {
	    //get current fragment
	    if (transitionInProgress) return; //todo:hack
	    ToolFragment tf = (ToolFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
	    //todo: abstract
	    if (tf.checkClick(view)) return;

	    if (view instanceof ToolLightButton) {

		    final ToolLightButton button = (ToolLightButton) view;
		    if (tf.getClass() == Communicator.class && button.getToolFragment() == tf) {
			    finish();
			    return;
		    }
		    if (button.isEnabled() && !button.isSelected()) {
			    if (toolHolderShown) {
				    toggleToolMenu();
				    new Handler().postDelayed(new Runnable() {
					    @Override
					    public void run() {
						    swapTo(button.getToolFragment().getClass());
					    }
				    }, 400);
			    } else {
				    swapTo(button.getToolFragment().getClass());
			    }

		    }
	    }

	    switch (view.getId()) {
		    case R.id.tool_holder_tab:
			    toggleToolMenu();
			    break;

	    }
    }

    private void toggleToolMenu() {

	    View toolHolder = findViewById(R.id.tool_holder);
	    if (toolHolderShown) {
		    toolHolder.animate().translationY((toolHolder.getMeasuredHeight() / 7) * -6);
		    findViewById(R.id.journal_gear).animate().rotationBy(360);
		    findViewById(R.id.back_gear).animate().rotationBy(-360);
	    } else {
		    toolHolder.animate().translationY(0);
		    findViewById(R.id.journal_gear).animate().rotationBy(-360);
		    findViewById(R.id.back_gear).animate().rotationBy(360);
	    }
	    toolHolderShown = !toolHolderShown;
    }

    @Override
    public void onBackPressed() {
	    Log.d("Main", "OnBackPressed");
	    Fragment f = getCurrentFragment();
	    if (f instanceof Communicator) {
		    super.onBackPressed();
	    } else {
		    onClick(ToolMap.get(Communicator.class));
	    }
    }

    @Override
    protected void onPause() {
	    if (gcAudio.isPlaying()) gcAudio.pause();
	    locationManager.removeUpdates(this); //stop location updates
	    super.onPause();
    }

	@Override
	protected void onResume() {
		super.onResume();

		gcAudio.play();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (toolHolderShown) toggleToolMenu();
			}
		}, 500);
		requestSlowGPSUpdates();
	}

	@Override
	protected void onDestroy() {
		gcAudio.stop();
		super.onDestroy();
	}

	public void swapTo(Class toolType) {
		if (ToolMap.containsKey(toolType)) {
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, getTool(toolType))
					.commit();
		} else throw new RuntimeException("That Class is not a Tool, You Tool!");

    }

    public void startDialog(String dialog) {

    }

    //GOOGLE SERVICES CODE

	public void prepareLocation() {

	}

	public void triggerLocation() {
		gcTrigger trigger = gcEngine.Access().getCurrentSeqPt().getAutoTrigger();
		if (trigger == null) gcEngine.Access().getCurrentSeqPt().getTrigger(playerLocationInStory);
		if (trigger != null) {
			trigger.activate(actionManager);
		}

    }

    public void hideGears(boolean back, boolean journal) {
        View backGear = findViewById(R.id.back_gear);
        View journalGear = findViewById(R.id.journal_gear);
        backGear.animate().setListener(null);
        journalGear.animate().setListener(null);
        if (back) backGear.animate().translationX(-(backGear.getWidth()));
        if (journal) journalGear.animate().translationX(backGear.getWidth());
    }

	public void showGears() {
		View backGear = findViewById(R.id.back_gear);
		View journalGear = findViewById(R.id.journal_gear);

		backGear.animate().setListener(null);
		journalGear.animate().setListener(null);
		backGear.animate().translationX(0);
		journalGear.animate().translationX(0);
	}

    public void hideFrame(Boolean isLeftFrameShowing, Boolean isRightFrameShowing, Boolean isAdHolderShowing){
        View left_frame, right_frame, ad_holder;

        left_frame = findViewById(R.id.frame_left);
        right_frame = findViewById(R.id.frame_right);
        ad_holder = findViewById(R.id.ad_holder);

        left_frame.animate().translationX(-(left_frame.getWidth())).setDuration(1000);
        right_frame.animate().translationX(right_frame.getWidth()).setDuration(1000);
        ad_holder.animate().translationY(ad_holder.getHeight()).setDuration(1000);

        isAdHolderShowing = false;
        isLeftFrameShowing = false;
        isRightFrameShowing = false;

    }

    public void showFrame(Boolean isLeftFrameShowing, Boolean isRightFrameShowing, Boolean isAdHolderShowing){
        ImageView left_frame, right_frame, ad_holder;

        left_frame = (ImageView) findViewById(R.id.frame_left);
        right_frame = (ImageView) findViewById(R.id.frame_right);
        ad_holder = (ImageView) findViewById(R.id.ad_holder);

        left_frame.setScaleType(ImageView.ScaleType.FIT_START);
        right_frame.setScaleType(ImageView.ScaleType.FIT_END);

        left_frame.animate().translationX(0).setDuration(1000);
        right_frame.animate().translationX(0).setDuration(1000);
        ad_holder.animate().translationY(0).setDuration(1000);

        isAdHolderShowing = true;
        isLeftFrameShowing = true;
        isRightFrameShowing = true;

    }

    //GOOGLE SERVICES CODE

	/*
	 * Handle results returned to the FragmentActivity
	 * by Google Play services
	 */
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		//TODO: Error Handling
		return;
	}

    public gcLocation getPlayerLocationInStory() {
	    return playerLocationInStory;
    }

	public void showTool(Class tool) {
		ToolMap.get(tool).setEnabled(true);
	}

	public void hideTool(Class tool) {
		ToolMap.get(tool).setEnabled(false);
	}

    public void showLocationNotification() {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ghost)
                        .setContentTitle("Ghost Catcher")
                        .setContentText("You have arrived to your next location!");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID, mBuilder.getNotification());      //Oh come on, google, one is depreciated, the other is unsupported. :(
    }

	@Override
	public void onLocationChanged(Location location) {
		ToolFragment tf = (ToolFragment) getCurrentFragment();

		if (tf instanceof RFDetector) {
			((RFDetector) tf).onLocationChanged(location);
		}

		if (location == null) {
			playerLocationInStory = null;
			return;
		}

		List<gcLocation> storyLocations = gcEngine.Access().getCurrentSeqPt().getLocations();
		boolean hit = false;
		float accuracy = location.getAccuracy();

		for (gcLocation l : storyLocations) {
			float distance[] = new float[3]; // ugh, ref parameters.
			Location.distanceBetween(l.getLatitude(), l.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
			if (distance[0] <= accuracy) {
				playerLocationInStory = l;
				gcTrigger trigger = gcEngine.Access().getCurrentSeqPt().getTrigger(l);
				if (trigger.isEnabled())
					ToolMap.get(Biocalibrate.class).setEnabled(true);
				hit = true;
			}
		}

		if (hit) {
			if (tf instanceof LocationMap) {
				LocationMap m = (LocationMap) tf;
				for (gcLocation l : m.locations) {
					if (l == playerLocationInStory) {
						m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2));
					} else {
						m.markers.get(m.locations.indexOf(l)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
					}
				}
			}
		}
	}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //todo:implement
    }

    @Override
    public void onProviderEnabled(String provider) {
        //todo:implement
    }

    @Override
    public void onProviderDisabled(String provider) {
        //todo:implement
    }

	/**
	 * reconfigures GPS updates to occur at the requested minimum time interval
	 *
	 * @param GPSMinUpdateTimeMS the minimal GPS update interval, in milliseconds
	 */
	public void setGPSMinimumTimeInterval(int GPSMinUpdateTimeMS) {
		setGPSUpdates(GPSMinUpdateTimeMS, this.GPSMinUpdateDistanceM);
	}

	/**
	 * reconfigures GPS updates to occur at the requested minimum distance interval
	 *
	 * @param GPSMinUpdateDistanceM the minimal GPS update interval, in meters.
	 */
	public void setGPSMinimumDistanceInterval(int GPSMinUpdateDistanceM) {
		setGPSUpdates(this.GPSMinUpdateTimeMS, GPSMinUpdateDistanceM);
	}

	/**
	 * reconfigures GPS updates to occur at the requested minimum time and distance intervals
	 *
	 * @param GPSMinUpdateTimeMS    the minimal GPS update interval, in milliseconds
	 * @param GPSMinUpdateDistanceM the minimal GPS update interval, in meters.
	 */
	public void setGPSUpdates(int GPSMinUpdateTimeMS, int GPSMinUpdateDistanceM) {
		if (GPSMinUpdateTimeMS < 0 || GPSMinUpdateDistanceM < 0) {
			throw new IllegalArgumentException("GPSMinUpdateTimeMS and GPSMinUpdateDistanceM  cannot be negative");
		}

		this.GPSMinUpdateTimeMS = GPSMinUpdateTimeMS;
		this.GPSMinUpdateDistanceM = GPSMinUpdateDistanceM;

		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSMinUpdateTimeMS, GPSMinUpdateDistanceM, this);
	}

	/**
	 * reconfigures GPS updates to occur at the minumum every 6s and 50 meters
	 */
	public void requestSlowGPSUpdates() {
		setGPSUpdates(GPS_SLOW_MIN_UPDATE_TIME_MS, GPS_SLOW_MIN_UPDATE_DISTANCE_M);
	}

	/**
	 * returns the most recent known location of the user.
	 *
	 * @return the most recent known location of the user.
	 */
	public Location getCurrentGPSLocation() {
		return currentGPSLocation;
	}

	public Fragment getCurrentFragment() {
		return getFragmentManager().findFragmentById(R.id.fragment_container);
	}

    public <T extends ToolFragment> T getCurrentToolFragment(Class<T> cls) {
        Fragment tf = getCurrentFragment();
        if (tf.getClass().equals(cls)) {
            return cls.cast(tf);
        }
        return null;
    }

    public <T extends ToolFragment> boolean isToolEnabled(Class<T> cls) {
        if (ToolMap.containsKey(cls)) {
            return ToolMap.get(cls).isEnabled();
        } else throw new RuntimeException("Tool not Found");
    }

    public <T extends ToolFragment> T getTool(Class<T> cls) {
        if (ToolMap.containsKey(cls)) {
            return cls.cast(ToolMap.get(cls).getToolFragment());
        } else return null;
    }


    ///////////////////////////DECOR VIEW CODE

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (useDecorView && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			decorViewHandler.postDelayed(decor_view_settings, 500);
		}
		return super.onKeyDown(keyCode, event);
	}

    public int playSound(int soundName) {
        return soundPool.play(soundName, 0.3f, 0.3f, 1, 0, 1);
    }

    // Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
    }

    public class Sounds {
        public int metalClick, leverRoll, strangeMetalNoise, creepyChains, testSoundClip, calibrateSoundClip;

        public Sounds(SoundPool soundPool) {
            testSoundClip = soundPool.load(MainActivity.this, R.raw.gc_audio_amplifier, 1);
            metalClick = soundPool.load(MainActivity.this, R.raw.metal_click, 1);
            leverRoll = soundPool.load(MainActivity.this, R.raw.lever_roll, 1);
            strangeMetalNoise = soundPool.load(MainActivity.this, R.raw.strange_mechanical_noise, 1);
            creepyChains = soundPool.load(MainActivity.this, R.raw.creepy_chains, 1);
            calibrateSoundClip = soundPool.load(MainActivity.this, R.raw.gc_audio_amplifier, 1);
        }
    }

    public void createImageURIs(){
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String,Uri>(){{
            put("overlay", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("main_screen2.png").build());
            put("background", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("background.png").build());
            put("frame_left", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("frame_left.png").build());
            put("frame_right", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("frame_right.png").build());
            put("ad_holder", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("ad_holder.png").build());
            put("tool_selector", rootUri.buildUpon().appendPath("skins").appendPath("main_frame").appendPath("toolselector.png").build());
            put("gear_button", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("back_gear.png").build());

            put("button_lit", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("button_lit.png").build());
            put("button_unlit", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("button_unlit.png").build());
            put("button_disabled", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("button_disabled.png").build());

            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

}

