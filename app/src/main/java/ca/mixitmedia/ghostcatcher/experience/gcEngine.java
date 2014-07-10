package ca.mixitmedia.ghostcatcher.experience;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

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
import java.util.List;

/**
 * Created by Dante on 07/03/14
 */
public class gcEngine {
    private static gcEngine ourInstance;

    public List<gcCharacter> characters;
    public List<gcSeqPt> seqPts;
    public List<gcLocation> locations;

    public Context context;
    public Uri root;// = new File(Environment.getExternalStorageDirectory()+"/Android/data/ca.mixitmedia.ghostcatcher.app/files/mixitmedia/ghostcatcher");

    XmlPullParserFactory pullParserFactory;


    public static gcEngine Access() {
        if (ourInstance == null) throw new RuntimeException("gcEngine not Init'd");
        return ourInstance;
    }

    public gcLocation getLocation(String id) {
        for (gcLocation location : locations) {
            if (location.getId().equals(id))
                return location;
        }
        return null;
    }

    public static void init(Context context) {
        ourInstance = new gcEngine(context);
    }

    private gcEngine(Context context) {
        this.context = context;
        root = Uri.parse(new File(context.getExternalFilesDir("mixitmedia"), "ghostcatcher").getAbsolutePath() );
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            String textPath = root + "/Exp1Chapter1.xml";
            InputStream in_s = new BufferedInputStream(new FileInputStream(textPath));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            parseXML(parser);
        } catch (XmlPullParserException | IOException e) {
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

    public gcCharacter getCharacter(String id) {
        for (gcCharacter c : characters) {
            if (c.getId().equals(id)) return c;
        }
        return null;

    }

    public gcSeqPt getCurrentSeqPt() {
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
