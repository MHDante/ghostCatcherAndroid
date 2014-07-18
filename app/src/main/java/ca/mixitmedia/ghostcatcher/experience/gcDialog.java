package ca.mixitmedia.ghostcatcher.experience;

import android.app.AlertDialog;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dante on 07/03/14
 */
public class gcDialog {

    public String id;
    public Map<Integer, Uri> portraits = new HashMap<>();
    public Map<Integer, String> parsed = new HashMap<>();
    public Uri audio;
    public int duration;

    private gcDialog() {
    }

    public static gcDialog get(gcSeqPt seqPt, String id) throws IOException {
        if (!seqPt.dialogCache.containsKey(id))
            loadDialog(seqPt, id);
        return seqPt.dialogCache.get(id);
    }

    public static void loadDialog(gcSeqPt seqPt, String id) throws IOException {
        String seqPath = gcEngine.Access().root + "/seq" + "/seq" + seqPt.id;
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
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(soundPath);
        dialog.duration = (int) Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        line = r.readLine();

        while ((line = r.readLine()) != null) {
            if (line.equals("")) continue;
            switch (line.charAt(0)) {
                case '#':
                    continue;
                case '>':
                    if (line.charAt(1) == '>') {
                        chr = gcEngine.Access().getCharacter(line.substring(2).trim());
                        if (!total.toString().isEmpty()) {
                            dialog.portraits.put(time, chr.getPose(pose));
                            dialog.parsed.put(time, total.toString());
                        }
                    }
                    break;
                case '<':
                    if (line.charAt(1) == '<') {
                        pose = (line.substring(2).trim());
                    }
                    break;
                case '@':
                    String[] times = line.substring(1).split(":");
                    int minutes = Integer.parseInt(times[0]);
                    int seconds = Integer.parseInt(times[1]);
                    time = minutes * 60 + seconds;
                    break;
                default:
                    total.append(line);
            }

        }

        dialog.portraits.put(time, chr.getPose(pose));
        if (!new File(chr.getPose(pose).getPath()).exists()) {
            new AlertDialog.Builder(gcEngine.Access().context)
                    .setMessage("File doesn't exist for pose " + pose + " for character " + chr.name)
                    .create().show();
            throw new RuntimeException("File doesn't exist for pose " + pose + " for character " + chr.name);
        }
        dialog.parsed.put(time, total.toString());
        dialog.id = id;
        seqPt.dialogCache.put(id, dialog);
    }

}
