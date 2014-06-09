package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.content.res.AssetManager;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.utils.Tuple;

/**
 * Created by Dante on 07/03/14.
 */
public class gcDialog {

    public String id;
    public Map<Integer, Uri> portraits = new HashMap<>();
    public Map<Integer, String> parsed = new HashMap<>();
    public Uri audio;

    private gcDialog() {
    }

    public static gcDialog get(gcSeqPt seqPt, String id) throws IOException {
        if (!seqPt.dialogCache.containsKey(id))
            loadDialog(seqPt, id);
        gcDialog ret = seqPt.dialogCache.get(id);
        return ret;
    }

    public static void loadDialog(gcSeqPt seqPt, String id) throws IOException {
        String seqPath = gcEngine.Access().root.getPath() + "/seq" + "/seq"+ seqPt.id;
        String textPath = seqPath + "/text/" + id + ".txt";
        String soundPath = seqPath + "/sounds/" + id + ".mp3";

        InputStream inputStream = new BufferedInputStream(new FileInputStream(textPath));
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder total = new StringBuilder();
        String line;
        int time = 0;
        gcCharacter chr = gcEngine.Access().getCharacter("static");
        String pose = null;

        gcDialog dialog = new gcDialog();
        dialog.audio = Uri.fromFile(new File(soundPath));
        line = r.readLine();

        while ((line = r.readLine()) != null) {
        if (line.equals("")) continue;
            switch (line.charAt(0)) {
                case '#':
                    continue;
                case '>':
                    if (line.charAt(1) == '>') {
                        if (!total.toString().isEmpty()) {
                            dialog.portraits.put(time, chr.getPose(pose));
                            dialog.parsed.put(time, total.toString());
                        }
                        chr = gcEngine.Access().getCharacter(line.substring(2).trim());
                    } break;
                case '<':
                    if (line.charAt(1) == '<') {
                        pose = (line.substring(2).trim());
                    }break;
                case '@':
                    String[] times = line.substring(2).split(":");
                    int minutes = Integer.parseInt(times[0]);
                    int seconds = Integer.parseInt(times[1]);
                    time = minutes * 60 + seconds;
                    break;
                default:
                    total.append(line);
            }

        }

        dialog.portraits.put(time, chr.getPose(pose));
        dialog.parsed.put(time, total.toString());
        dialog.id = id;
        seqPt.dialogCache.put(id, dialog);
    }

}
