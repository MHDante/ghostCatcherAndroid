package ca.mixitmedia.ghostcatcher.experience;

import android.app.AlertDialog;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;

/**
 * Created by Dante on 07/03/14
 */
public class gcDialog {

    public String id;
    public List<Integer> intervals = new ArrayList<>();
    public SparseArray<Uri> portraits = new SparseArray<>();
    public SparseArray<String> parsed = new SparseArray<>();
    public Uri audio;
    public int duration;

    private gcDialog() {
    }

    public static gcDialog get(gcSeqPt seqPt, String id){
        try {
            if (!seqPt.dialogCache.containsKey(id))
                loadDialog(seqPt, id);
        }catch (IOException e){
            Utils.messageDialog(seqPt.engine.getContext(), "Dialog IOError:", "Could not load Dialog: "+id+ " in seq: " +seqPt.id);
        }
        return seqPt.dialogCache.get(id);
    }

    public static void loadDialog(gcSeqPt seqPt, String id) throws IOException {
        String seqPath = gcEngine.root + "/seq" + "/seq" + seqPt.id;
        String textPath = seqPath + "/text/" + id + ".txt";
        String soundPath = seqPath + "/sounds/" + id + ".mp3";

        InputStream inputStream = new BufferedInputStream(new FileInputStream(textPath));
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder total = new StringBuilder();
        String line;
        int time = 0;
        gcCharacter chr = seqPt.engine.getCharacters().get("static");
        String pose = null;

        gcDialog dialog = new gcDialog();
        dialog.audio = Uri.fromFile(new File(soundPath));
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(soundPath);
        dialog.duration = (int) Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        line = r.readLine();//ommit first line because of reasons.
        int lines = 1;
        while ((line = r.readLine()) != null) {
            lines++;
            if (line.equals("")) continue;
            switch (line.charAt(0)) {
                case '#':
                    continue;
                case '>':
                    if (line.charAt(1) == '>') {
                        
                        if (!total.toString().isEmpty()) {
                            dialog.intervals.add(time);
                            Uri poseUri = chr.getPose(pose);
                            if (poseUri == null)
                                Utils.messageDialog(seqPt.engine.getContext(),"error", "Line : " + lines + " SeqPt: " + seqPt.id);
                            dialog.portraits.put(time, poseUri);

                            dialog.parsed.put(time, total.toString()+"\n");
                            total = new StringBuilder();
                        }
                        chr = seqPt.engine.getCharacters().get(line.substring(2).trim());
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

        if (chr.getPose(pose) == null){
            new AlertDialog.Builder(seqPt.engine.getContext())
                    .setMessage("File doesn't exist for pose " + pose + " for character " + chr.name)
                    .create().show();
        }
        dialog.portraits.put(time, chr.getPose(pose));
        String filename = chr.getPose(pose).getPath();
        if (!new File(filename).exists()) {
            throw new IOException("File "+filename+" doesn't exist for pose " + pose + " for character " + chr.name);
        }
        dialog.parsed.put(time, total.toString());
        dialog.intervals.add(time);
        dialog.id = id;
        seqPt.dialogCache.put(id, dialog);
    }

    public int getDuration() {
        return (int)Utils.getMediaDuration(audio)/1000;

    }
}
