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

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;
import ca.mixitmedia.ghostcatcher.experience.gcLocation;
import ca.mixitmedia.ghostcatcher.utils.Utils;


public class LocationMap extends ToolFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    GoogleMap map;
    public List<gcLocation> locations;
    int selectedLocation;

    Map<String, Uri> imageFileLocationMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_location_map, container, false);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);

        createImageURIs();

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

    public List<Marker> markers = new ArrayList<>();

    private void setUpMap() {
        map.setPadding(Utils.convertDpToPixelInt(105, getActivity()), 0, 0, 0);
        //LatLngBounds b = new LatLngBounds(new LatLng(43.65486328474458, -79.38564497647212), new LatLng(43.66340903426289, -79.37292076230159));
        //GroundOverlayOptions newarkMap = new GroundOverlayOptions()
        //        .image(BitmapDescriptorFactory.fromResource(R.drawable.campus))
        //        .positionFromBounds(b);
        //map.addGroundOverlay(newarkMap);

        locations = gcEngine.Access().getCurrentSeqPt().getLocations();
        if (locations.size() <= 0) return;

        for (selectedLocation = 0; selectedLocation < locations.size(); selectedLocation++) {
            gcLocation loc = locations.get(selectedLocation);
            if (gcMain.getPlayerLocationInStory() == null || !loc.id.equals(gcMain.getPlayerLocationInStory().id)) {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                        .title(loc.name)));
                // Google Marker IDs are held as Strings with an m prefix : m1, m2, m3, m4

            } else {
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker2))
                        .title(loc.name)));
            }
        }
        setBanner(locations.get(selectedLocation - 1));


        map.setMyLocationEnabled(true);
    }

    public void setBanner(gcLocation loc) {
        TextView tv = (TextView) getView().findViewById(R.id.title);
        tv.setText(loc.name);
        TextView tv2 = (TextView) getView().findViewById(R.id.to_do);
        tv2.setText(loc.description);
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
    public int getGlyphID() {
        return (R.drawable.icon_location_map);
    }

    @Override
    public View getInfoContents(Marker marker) {

        LinearLayout lv = new LinearLayout(getActivity());
        lv.setBackgroundColor(Color.WHITE);
        lv.setOrientation(LinearLayout.VERTICAL);

        //FrameLayout.LayoutParams LLParams
        //=  new WindowManager.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT);

        if (gcMain.getPlayerLocationInStory() != null
                && marker.getTitle().equals(gcMain.getPlayerLocationInStory().name) //todo:hacks
                && gcMain.isToolEnabled(Biocalibrate.class)) {

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
        if (gcMain.getPlayerLocationInStory() != null && marker.getTitle().equals(gcMain.getPlayerLocationInStory().name)) { //todo:hacks
            gcMain.swapTo(Biocalibrate.class);
        }
    }

    protected int getAnimatorId(boolean enter) {
        if (enter) gcMain.playSound(gcMain.sounds.strangeMetalNoise);
        return (enter) ? R.animator.transition_in_from_top : R.animator.transition_out_from_bottom;
    }

    public void createImageURIs(){
        final Uri rootUri = Uri.fromFile(gcEngine.Access().root);

        imageFileLocationMap = new HashMap<String,Uri>(){{
            put("overlay", rootUri.buildUpon().appendPath("skins").appendPath("map").appendPath("map_overlay.png").build());
            put("bullet_check", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("bullet_check.png").build());
            put("arrow", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("btn_playback_play.png").build());
            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

}
