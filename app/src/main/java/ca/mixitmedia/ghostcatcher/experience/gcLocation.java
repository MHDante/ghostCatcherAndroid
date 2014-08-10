package ca.mixitmedia.ghostcatcher.experience;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;

/**
 * Created by Dante on 07/03/14
 */
public class gcLocation extends Location implements Comparable {
    String id;
    String name;
    String description;
    gcEngine engine;
    gcLocation(gcEngine engine, double latitude, double longitude) {
        super("gcLocation Provider");
        this.engine = engine;
	    setLatitude(latitude);
	    setLongitude(longitude);
    }

    public Uri getImageUri() {
        return Uri.fromFile(new File(gcEngine.root.getPath()+"/locations/"+id+".png"));
    }

	public boolean equalsMarkerTitle (Marker marker) {
		return this.getTitle().equals(marker.getTitle());
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public LatLng asLatLng() {
		return new LatLng(this.getLatitude(), this.getLongitude());
	}

	@Override
	public int compareTo(Object o) {
		return this.getId().compareTo(((gcLocation) o).getTitle());
	}
}

