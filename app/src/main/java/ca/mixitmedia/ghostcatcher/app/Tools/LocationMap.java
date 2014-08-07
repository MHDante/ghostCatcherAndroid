package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;


public class LocationMap extends ToolFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    public List<gcLocation> locations;
    public List<Marker> markers = new ArrayList<>();
    GoogleMap map;
    int selectedLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_location_map, container, false);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
	    map.getUiSettings().setZoomControlsEnabled(false);
	    // map.getUiSettings().setRotateGesturesEnabled(false);
	    //map.getUiSettings().setCompassEnabled(false);


        //ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        ImageButton right_button = (ImageButton) view.findViewById(R.id.right);
        ImageButton left_button = (ImageButton) view.findViewById(R.id.left);

        left_button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        right_button.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) setUpMap();
    }

    private void setUpMap() {
	    map.setMyLocationEnabled(true);
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);

	    Location location = map.getMyLocation();
	    if (location != null) {
		    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f));
	    }


        locations = gcMain.gcEngine.getCurrentSeqPt().getLocations();
        if (locations.size() <= 0) return;
        for (gcLocation loc : locations) {
	        gcLocation currentGCLocation = gcMain.locationManager.getCurrentGCLocation();
	        int pinResource;
            if (currentGCLocation == null || !loc.equalsID(currentGCLocation)){
	            pinResource = R.drawable.map_marker;
            }
	        else {
	            setBanner(loc);
	            pinResource = R.drawable.map_marker2;
            }

            markers.add(map.addMarker(new MarkerOptions()
                    .position(loc.asLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(pinResource))
                    .title(loc.getName())));
        }
    }

    public void setBanner(gcLocation loc) {
        TextView tv = (TextView) getView().findViewById(R.id.title);
        tv.setText(loc.getName());
        TextView tv2 = (TextView) getView().findViewById(R.id.to_do);
        tv2.setText(loc.getDescription());
        ImageView iv = (ImageView) getView().findViewById(R.id.imageThumbnail);
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), loc.getImageUri());
            iv.setImageBitmap(image);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
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


    public boolean checkClick(View view) {
        if (markers.size() <= 0) return false;

        switch (view.getId()) {
            case R.id.left:
	            return changeMarker(-1);
            case R.id.right:
                return changeMarker(+1);
	        default: return false;
        }
    }

	private boolean changeMarker (int delta) {
		Marker m = markers.get((selectedLocation + delta) % markers.size());
		m.showInfoWindow();
		onMarkerClick(m);
		return true;
	}

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (gcLocation l : locations) {
            if (l.getName().equals(marker.getTitle())) {
                setBanner(l);
                selectedLocation = locations.indexOf(l);
                return false;
            }
        }
        throw new RuntimeException("Something really bad happened on this line.");
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LinearLayout lv = new LinearLayout(getActivity());
        lv.setBackgroundColor(Color.WHITE);
        lv.setOrientation(LinearLayout.VERTICAL);

        if (gcMain.locationManager.getCurrentGCLocation() != null
                && marker.getTitle().equals(gcMain.locationManager.getCurrentGCLocation().getName())) {

            ImageView iv = new ImageView(getActivity());
            iv.setImageResource(R.drawable.fingerprint);

            lv.addView(iv);
        }

        TextView tv = new TextView(getActivity());
        tv.setText(marker.getTitle());
        tv.setTextColor(Color.BLACK);

        lv.addView(tv);

        return lv;

        //original text gets reset here
        //return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (gcMain.locationManager.getCurrentGCLocation() != null && marker.getTitle().equals(gcMain.locationManager.getCurrentGCLocation().getName())) { //todo:hacks
            //Todo:alex, handle window clicks here.
        }
    }

    protected int getAnimatorId(boolean enter) {
        if (enter) {
            SoundManager.playSound(SoundManager.Sounds.strangeMetalNoise);
            return R.animator.transition_in_from_top;
        }
        return R.animator.transition_out_from_bottom;
    }


}
