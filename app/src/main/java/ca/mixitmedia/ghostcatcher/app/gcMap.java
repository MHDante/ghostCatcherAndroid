package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

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
import com.google.android.gms.maps.model.MarkerOptions;

import ca.mixitmedia.ghostcatcher.utils.Utils;


public class gcMap extends Activity {

    GoogleMap map;
    SeekBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ///setGears();
        setUpMapIfNeeded();
        bar = (SeekBar) findViewById(R.id.seekBar);
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.setPadding(Utils.convertDpToPixelInt(105, this), 0, 0, 0);
        LatLngBounds b = new LatLngBounds(new LatLng(43.65486328474458, -79.38564497647212), new LatLng(43.66340903426289, -79.37292076230159));
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.campus))
                .positionFromBounds(b);
        map.addGroundOverlay(newarkMap);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(43.65947, -79.37961))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                .title("Ryerson Theatre"));
//        map.moveCamera();
//        map.getMyLocation();
    }
}
