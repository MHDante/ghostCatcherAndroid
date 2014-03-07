package ca.mixitmedia.ghostcatcher.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dante on 07/03/14.
 */
public class FileLoader {


    File dir;

    public FileLoader() {
        File sdCard = Environment.getExternalStorageDirectory();
        dir = new File(sdCard.getAbsolutePath() + "/mixitmedia/ghostcatcher");
        dir.mkdirs();
    }

    public void downloadFile(String url) throws Exception {
        URL fileAddress = new URL(from);
        String filename = fileAddress.getFile();
        File target = new File(dir, filename);

        HttpURLConnection conn = (HttpURLConnection) fileAddress.openConnection();
        conn.setDoInput(true);
        conn.setConnectTimeout(10000); // timeout 10 secs
        conn.connect();
        InputStream input = conn.getInputStream();
        FileOutputStream fOut = new FileOutputStream(target);
        int byteCount = 0;
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = input.read(buffer)) != -1) {
            fOut.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        fOut.flush();
        fOut.close();
    }

}
