package ca.mixitmedia.ghostcatcher.app.Tools;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;


public class LocationMap extends ToolFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    public List<gcLocation> locations;
    public List<Marker> markers = new ArrayList<>();
    GoogleMap map;
    int selectedLocation;
    Map<String, Uri> imageFileLocationMap;

    public LocationMap() {
        createImageURIs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_location_map, container, false);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);


        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        ImageButton right_button = (ImageButton) view.findViewById(R.id.right);
        ImageButton left_button = (ImageButton) view.findViewById(R.id.left);

        overlay.setImageURI(imageFileLocationMap.get("overlay"));
        left_button.setImageURI(imageFileLocationMap.get("arrow"));
        right_button.setImageURI(imageFileLocationMap.get("arrow"));

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
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);

        locations = gcEngine.Access().getCurrentSeqPt().getLocations();
        if (locations.size() <= 0) return;

        for (selectedLocation = 0; selectedLocation < locations.size(); selectedLocation++) {
            gcLocation loc = locations.get(selectedLocation);
            if (gcMain.locationManager.getCurrentGCLocation() == null || !loc.getId().equals(gcMain.locationManager.getCurrentGCLocation().getId())) {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                        .title(loc.getName())));

            } else {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2))
                        .title(loc.getName())));
            }
        }
        setBanner(locations.get(selectedLocation - 1));


        map.setMyLocationEnabled(true);
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
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();
        super.onDestroyView();
    }


    public boolean checkClick(View view) {
        if (markers.size() > 0) {
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
        }
        return false;
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

    public void createImageURIs() {
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String, Uri>() {{
            put("overlay", rootUri.buildUpon().appendPath("skins").appendPath("map").appendPath("map_overlay.png").build());
            put("bullet_check", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("bullet_check.png").build());
            put("arrow", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("btn_playback_play.png").build());
            put("map_button_glyph", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_locationmappng").build());
            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

}
