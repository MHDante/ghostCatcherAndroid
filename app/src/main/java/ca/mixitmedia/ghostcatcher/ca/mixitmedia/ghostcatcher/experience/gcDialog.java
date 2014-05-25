package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.util.SparseArray;

import com.google.android.gms.maps.model.BitmapDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dante on 07/03/14.
 */
public class gcDialog {


    String parsed;
    Map<Integer, gcCharacter> portraits = new HashMap<Integer, gcCharacter>();
    String text;

    private gcDialog() {
    }

    ;

    public static gcDialog get(gcSeqPt seqPt, String id) {
        gcDialog result = new gcDialog();

        return result;
    }

}
