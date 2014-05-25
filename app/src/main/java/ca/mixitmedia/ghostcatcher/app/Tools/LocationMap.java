package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.utils.Utils;


public class LocationMap extends ToolFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    GoogleMap map;
    SeekBar bar;
    public List<gcLocation> locations;
    int selectedLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_map, container, false);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
        return view;


    }

    @Override
    public void onResume() {
        super.onResume();

        if (map != null) setUpMap();

        bar = (SeekBar) getView().findViewById(R.id.seekBar);
        bar.setMax((int) map.getMaxZoomLevel() / 2);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                map.animateCamera(CameraUpdateFactory.zoomTo((float) progress + map.getMaxZoomLevel() / 2));
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                bar.setProgress((int) (cameraPosition.zoom - map.getMaxZoomLevel() / 2));
            }
        });

    }

    public ArrayList<Marker> markers = new ArrayList<Marker>();

    private void setUpMap() {
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);
        LatLngBounds b = new LatLngBounds(new LatLng(43.65486328474458, -79.38564497647212), new LatLng(43.66340903426289, -79.37292076230159));

        //GroundOverlayOptions newarkMap = new GroundOverlayOptions()
        //        .image(BitmapDescriptorFactory.fromResource(R.drawable.campus))
        //        .positionFromBounds(b);
        //map.addGroundOverlay(newarkMap);

        locations = gcEngine.Access().getCurrentSeqPt().locations;

        for (selectedLocation = 0; selectedLocation < locations.size(); selectedLocation++) {
            if (gcMain.getCurrentLocation() == null || locations.get(selectedLocation).id != gcMain.getCurrentLocation().id) {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(locations.get(selectedLocation).latitude, locations.get(selectedLocation).longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                        .title(locations.get(selectedLocation).name)));
                // Google Marker IDs are held as Strings with an m prefix : m1, m2, m3, m4

            } else {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(locations.get(selectedLocation).latitude, locations.get(selectedLocation).longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2))
                        .title(locations.get(selectedLocation).name)));
            }
        }
        setBanner(locations.get(selectedLocation - 1));


        map.setMyLocationEnabled(true);
    }

    //private void addMarker(){
    //    map.addMarker()
    //}

    public void setBanner(gcLocation loc) {
        TextView tv = (TextView) getView().findViewById(R.id.title);
        tv.setText(loc.name);
        TextView tv2 = (TextView) getView().findViewById(R.id.to_do);
        tv2.setText(loc.description);
        ImageView iv = (ImageView) getView().findViewById(R.id.imageThumbnail);
        iv.setImageBitmap(loc.image);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment f = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();
    }

    public boolean checkClick(View view) {
        switch (view.getId()) {
            case R.id.left:
                int mod_result = (selectedLocation + markers.size() - 1) % markers.size();
                Marker m = markers.get(mod_result);
                m.showInfoWindow();
                onMarkerClick(m);
                return true;
            case R.id.right:
                int mod_result2 = (selectedLocation + markers.size() + 1) % markers.size();
                Marker m2 = markers.get(mod_result2);
                m2.showInfoWindow();
                onMarkerClick(m2);
                return true;
        }
        return false;
    }

    public static LocationMap newInstance(String settings) {
        LocationMap fragment = new LocationMap();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        for (gcLocation l : locations) {
            if (l.name.equals(marker.getTitle())) {
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

        //FrameLayout.LayoutParams LLParams
        //=  new WindowManager.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT);

        if (gcMain.getCurrentLocation() != null && Integer.parseInt(marker.getId().substring(1)) == gcMain.getCurrentLocation().id && gcMain.getTool(Communicator.class).bioCalib) {

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
        if (gcMain.getCurrentLocation() != null && Integer.parseInt(marker.getId().substring(1)) == gcMain.getCurrentLocation().id) {
            gcMain.swapTo(Biocalibrate.class, false);
        }
    }
}
