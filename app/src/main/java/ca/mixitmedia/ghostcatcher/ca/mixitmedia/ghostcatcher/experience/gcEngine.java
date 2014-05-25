package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.gcMediaService;
import ca.mixitmedia.ghostcatcher.utils.ChapterLoader;

/**
 * Created by Dante on 07/03/14.
 */
public class gcEngine {
    private static gcEngine ourInstance = new gcEngine();

    public List<gcCharacter> characters;
    public List<gcSeqPt> seqPts;
    public List<gcLocation> locations;

    public File root = new File(Environment.getExternalStorageDirectory(), "mixitmedia");

    public Context context;

    ChapterLoader loader;

    XmlPullParserFactory pullParserFactory;


    public static gcEngine Access() {
        return ourInstance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    private gcEngine() {
        loader = new ChapterLoader("mixitmedia");
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in_s = context.getAssets().open("temp.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            parseXML(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    characters = new ArrayList<>();
                    locations = new ArrayList<>();
                    seqPts = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "character":
                            characters.add(gcCharacter.parse(parser));
                            break;
                        case "location":
                            locations.add(gcLocation.parse(parser));
                            break;
                        case "seq_pt":
                            seqPts.add(gcSeqPt.parse(parser));
                            break;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    public Uri getSoundUri(String sound) {
        //    File f = new File(loader.root, sound + ".mp3");
        //    return Uri.fromFile(f);
//
        int soundId = context.getResources().getIdentifier(sound, "raw", context.getPackageName());
        return Uri.parse("android.resource://ca.mixitmedia.ghostcatcher.app/" + soundId);
    }

    public CharSequence getNextToDo() {
        return "You must go defeat the dargon"; //todo: correct spelling of dragon
    }

    public gcSeqPt getCurrentSeqPt(){
        return seqPts.get(0);
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
}
