package ca.mixitmedia.ghostcatcher.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcEngine;

/**
 * Created by Dante on 07/03/14.
 */
public class ChapterLoader {


    public File root;

    public ChapterLoader(String appPath) {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            root = new File(sdCard, appPath);
            if (!root.exists()) {
                if (!root.mkdirs() || root.isDirectory()) throw new RuntimeException("WTF, mate");
                File gpxfile = new File(root, "readme");
                FileWriter writer = new FileWriter(gpxfile);
                writer.append("This is the directory where we create the experience.");
                writer.flush();
                writer.close();
                Toast.makeText(gcEngine.Access().context, "Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException when creating directory: " + e.getMessage());
        }


    }

    public void downloadFile(String url) throws Exception {
        URL fileAddress = new URL(url);
        String filename = fileAddress.getFile();
        File target = new File(root, filename);

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

    private boolean unpackZip(String zipname) {
        String path = root.getPath();
        String subDir = Utils.removeExtension(zipname);
        InputStream is;
        ZipInputStream zis;
        try {
            File file = new File(path + zipname);
            is = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;

                String filename = ze.getName();
                FileOutputStream fout = new FileOutputStream(path + filename);

                // reading and writing
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                    byte[] bytes = baos.toByteArray();
                    fout.write(bytes);
                    baos.reset();
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


}
