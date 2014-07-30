package ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.Tools;

/**
 * Created by Dante on 07/03/14
 */
public class gcEngine {

    public HashMap<String, gcCharacter> characters;
    public List<gcSeqPt> seqPts;
    public HashMap<String, gcLocation> locations;
    public Context context;
    public static  Uri root;
    XmlPullParserFactory pullParserFactory;

    gcEngine(Context context) {
        characters = new HashMap<>();
        seqPts= new ArrayList<>();
        locations = new HashMap<>();
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
        return "You must go defeat the dargon"; //todo: correct spelling of dragon
    }


    public gcSeqPt getCurrentSeqPt() {
        return seqPts.get(0);
    }

    public gcLocation getDestination() {
        return null;
    }
}
