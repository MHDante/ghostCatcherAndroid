package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import android.content.res.AssetManager;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 07/03/14.
 */
public class gcDialog {


    Map<Integer, gcCharacter> portraits = new HashMap<>();
    Map<Integer, String> parsed = new HashMap<>();
    String text;

    private gcDialog() {
    }

    public static gcDialog get(gcSeqPt seqPt, String id) {
        if (!seqPt.dialogCache.containsKey(id))
            loadDialog(seqPt, id);
        return seqPt.dialogCache.get(id);
    }

    public static void loadDialog(gcSeqPt seqPt, String id) throws IOException {
        String seqPath = gcEngine.Access().root.getPath() + "/seq";
        String textPath = seqPath + "/text/" + id + ".txt";
        String soundPath = seqPath + "/sounds/" + id + ".mp3";

        InputStream inputStream = new BufferedInputStream(new FileInputStream(textPath));
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder total = new StringBuilder();
        String line;
        int time = 0;

        while ((line = r.readLine()) != null) {

            switch (line.charAt(0)) {
                case '#':
                    continue;

            }


            total.append(line);
        }
        currentString = total.toString();

        drawableId = getResources().getIdentifier(file, "drawable", getActivity().getPackageName());
        ImageView imgV = (ImageView) getView().findViewById(R.id.character_portrait);
        imgV.setImageResource(drawableId);

        gcAudio.playTrack(file, false);
    }

}
