package ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;

/**
 * Created by Dante on 07/03/14
 */
public class gcEngine {

    private HashMap<String, gcCharacter> characters;
    private List<gcSeqPt> seqPts;
    private LinkedHashMap<String, gcLocation> locations;
    private Context context;
    public static  Uri root;

    gcEngine(Context context) {
        characters = new HashMap<>();
        seqPts= new ArrayList<>();
        locations = new LinkedHashMap<>();
        this.context = context;

    }

    public static Bitmap readBitmap(Context context, Uri selectedImage) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; //reduce quality
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }


    public void EndSeqPt() {
        Utils.messageDialog(context, " END SQ PT", "Not Implemented");
        //throw new RuntimeException("Not Implemented");
    }

    public CharSequence getNextToDo() {
        return "You must go defeat the dragon";
    }

    public gcSeqPt getCurrentSeqPt() {
        return seqPts.get(0);
    }

	public HashMap<String, gcCharacter> getCharacters() {
		return characters;
	}

	public List<gcSeqPt> getSeqPts() {
		return seqPts;
	}

	public LinkedHashMap<String, gcLocation> getAllLocations() {
		return locations;
	}

	public Context getContext() {
		return context;
	}
}
