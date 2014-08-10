package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;


public class LocationMap extends ToolFragment implements OnMarkerClickListener, InfoWindowAdapter, OnInfoWindowClickListener {

	ArrayList<gcLocation> sortedLocations;
	ArrayList<Marker> markers = new ArrayList<>();
    GoogleMap map;
    int selectedLocation;

	AnimatorUpdateListener locationBannerAnimatorUpdateListener;
	MarginLayoutParams mlp;

	//Overrides of ToolFragment class
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_map, container, false);

	    sortedLocations = new ArrayList<>(gcMain.gcEngine.getAllLocations().values());
	    Collections.sort(sortedLocations);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
	    map.getUiSettings().setZoomControlsEnabled(false);
	    map.getUiSettings().setRotateGesturesEnabled(false);
	    map.getUiSettings().setCompassEnabled(false);

	    final RelativeLayout locationBanner = (RelativeLayout) view.findViewById(R.id.location_banner);
	    mlp = (MarginLayoutParams) locationBanner.getLayoutParams();
	    locationBannerAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator valueAnimator) {
			    mlp.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
			    locationBanner.requestLayout();
			}
	    };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) setUpMap();
    }

	@Override
	public void onDestroyView() {
		MapFragment f = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		//Todo:Illegal state Exception: Activity has been destroyed.
		if (f != null)
			getFragmentManager().beginTransaction().remove(f).commit();
		super.onDestroyView();
	}

    private void setUpMap() {
	    map.setMyLocationEnabled(true);
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);

	    Location currentUserLocation = map.getMyLocation();
	    if (currentUserLocation != null) {
		    LatLng latLng = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f));
	    }

	    setBanner(sortedLocations.get(0));
        for (gcLocation l : sortedLocations) {
	        int pinResource;
            if (gcMain.gcEngine.getCurrentSeqPt().getActiveLocations().contains(l)) pinResource = R.drawable.map_marker_active;
	        else pinResource = R.drawable.map_marker_inactive;

	        markers.add(map.addMarker(new MarkerOptions()
		            .position(l.asLatLng())
		            .icon(BitmapDescriptorFactory.fromResource(pinResource))
		            .title(l.getTitle())));
        }
    }

    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.map_overlay_left_arrow:
	            System.out.println("left");
	            return changeSelectedLocation(-1);
            case R.id.map_overlay_right_arrow:
	            System.out.println("right");
	            return changeSelectedLocation(+1);
	        case R.id.map_overlay:
		        toogleBannerState();
		        return true;
	        default: return false;
        }
    }

	public boolean changeSelectedLocation(int delta) {
		selectedLocation = (selectedLocation + delta) % markers.size();
		System.out.println("Selected Location: "+selectedLocation+" Size: "+markers.size());
		Marker m = markers.get(selectedLocation);
		m.showInfoWindow();
		onMarkerClick(m);
		return true;
	}

	public void setBanner(gcLocation loc) {
		((TextView) getView().findViewById(R.id.location_title)).setText(loc.getTitle());
		((TextView) getView().findViewById(R.id.location_text)).setText(loc.getDescription());

		ImageView iv = (ImageView) getView().findViewById(R.id.location_thumbnail);
		try {
			Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), loc.getImageUri());
			iv.setImageBitmap(image);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Toggles banner detail
	 */
	public void toogleBannerState() {
		setBannerState(mlp.bottomMargin == -285);
	}

	/**
	 * Shows/hides banner detail
	 * @param state true to show detail, false to hide detail.
	 */
	public void setBannerState(boolean state) {
		ValueAnimator valueAnimator = ValueAnimator.ofInt(mlp.bottomMargin, state?0:-285);
		valueAnimator.addUpdateListener(locationBannerAnimatorUpdateListener);
		valueAnimator.setDuration(500);
		valueAnimator.start();
	}

	protected int getAnimatorId(boolean enter) {
		if (enter) {
			SoundManager.playSound(SoundManager.Sounds.strangeMetalNoise);
			return R.animator.transition_in_from_top;
		}
		return R.animator.transition_out_from_bottom;
	}

	//Implementation of OnMarkerClickListener interface
    @Override
    public boolean onMarkerClick(Marker marker) {
        for (gcLocation l : sortedLocations) {
            if (l.equalsMarkerTitle(marker)) {
                setBanner(l);
                selectedLocation = sortedLocations.indexOf(l);
                return false;
            }
        }
        throw new RuntimeException("Something really bad happened on this line.");
    }

	//Implementation of InfoWindowAdapter interface
    @Override
    public View getInfoWindow(Marker marker) {
	    return null; //Required so that getInfoContents is called
    }

    @Override
    public View getInfoContents(Marker marker) {

        LinearLayout lv = new LinearLayout(getActivity());
        lv.setBackgroundColor(Color.WHITE);
        lv.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(getActivity());
        tv.setText(marker.getTitle());
        tv.setTextColor(Color.BLACK);

        lv.addView(tv);

        return lv;
    }

	//Implementation of OnInfoWindowClickListener
    @Override
    public void onInfoWindowClick(Marker marker) {
        onMarkerClick(marker);
	    setBannerState(true);
    }
}
