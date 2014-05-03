package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.utils.Utils;


public class gcMap extends ToolFragment implements GoogleMap.OnMarkerClickListener {

    GoogleMap map;
    SeekBar bar;
    List<gcLocation> locations;
    int selectedLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_map, container, false);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
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

    private void setUpMap() {
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);
        LatLngBounds b = new LatLngBounds(new LatLng(43.65486328474458, -79.38564497647212), new LatLng(43.66340903426289, -79.37292076230159));

        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.campus))
                .positionFromBounds(b);
        map.addGroundOverlay(newarkMap);

        locations = gcEngine.getInstance().getCurrentSeqPt().locations;

        for(selectedLocation = 0; selectedLocation < locations.size(); selectedLocation ++){
            map.addMarker(new MarkerOptions()
                    .position(new LatLng( locations.get(selectedLocation).latitude , locations.get(selectedLocation).longitude ))
                    .icon( BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                    .title( locations.get(selectedLocation).name ));

        }

        setBanner( locations.get(selectedLocation-1) );



//        map.moveCamera();
//        map.getMyLocation();
    }

    //private void addMarker(){
    //    map.addMarker()
    //}

    public void setBanner(gcLocation loc){
        TextView tv = (TextView)getView().findViewById(R.id.title);
        tv.setText( loc.name );
        TextView tv2 = (TextView)getView().findViewById(R.id.to_do);
        tv2.setText( loc.description );
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
        return false;
    }

    public static gcMap newInstance(String settings) {
        gcMap fragment = new gcMap();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for( gcLocation l : locations){
            if (l.name.equals(marker.getTitle())){
                setBanner(l);
                selectedLocation = locations.indexOf(l);
                return false;
            }
        }
        throw new RuntimeException("Something really bad happened on this line.");
    }
}
