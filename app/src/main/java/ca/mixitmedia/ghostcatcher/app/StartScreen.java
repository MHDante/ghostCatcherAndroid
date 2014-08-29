package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.mixitmedia.ghostcatcher.Utils;

public class StartScreen extends Activity {

    ProgressDialog mProgressDialog;

    String url;
    String unzipLocation;
    String zipFile;
    File fileDir, appDir;

    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) digest.update(buffer, 0, numRead);
            }

            return convertHashToString(digest.digest());
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    //From internet
    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (byte md5Byte : md5Bytes) {
            returnVal += Integer.toString((md5Byte) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileDir = new File(getExternalFilesDir("mixitmedia"), "ghostcatcher");

        appDir = fileDir.getParentFile().getParentFile().getParentFile();

        Log.d("APPDIR IS", appDir.getAbsolutePath());
        //APPDIR IS﹕ /storage/emulated/0/Android/data/ca.mixitmedia.ghostcatcher.app

        Log.d("Filepaths 1 :", fileDir.getPath());
        //Filepaths 1 :﹕ /storage/emulated/0/Android/data/ca.mixitmedia.ghostcatcher.app/files/mixitmedia/ghostcatcher

        String cacheDir = getExternalCacheDir().getPath();
        Log.d("Filepaths 2 :", cacheDir);
        //Filepaths 2 :﹕ /storage/emulated/0/Android/data/ca.mixitmedia.ghostcatcher.app/cache

        unzipLocation = getExternalFilesDir("mixitmedia").getPath();
        Log.d("Filepaths 3 :", unzipLocation);
        zipFile = cacheDir + "/demo.zip";
        Log.d("Filepaths 4 :", zipFile);

        setContentView(R.layout.activity_start_screen);
        //

        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        url = getString(R.string.url);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        Button creditsButton = (Button) findViewById(R.id.creditsButton);

        continueButton.setEnabled(false);
        creditsButton.setEnabled(false);

        if (!fileDir.exists() || fileDir.list().length == 0) {

            Log.e("FILEDIR IS", fileDir.getAbsolutePath());
            if (wifi.isAvailable())
                // Trigger Async Task (onPreExecute method)
                new DownloadZipFile().execute(url);
            else if (mobile.isAvailable()) {
                try {
                    internetDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try { digest = MessageDigest.getInstance("MD5"); }
        catch (NoSuchAlgorithmException e) {
            Log.e("TAG", "Exception while getting digest", e);
            return null;
        }

        InputStream inputStream;
        try { inputStream = new FileInputStream(updateFile); }
        catch (FileNotFoundException e) {
            Log.e("TAG", "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = inputStream.read(buffer)) > 0) digest.update(buffer, 0, read);

            BigInteger bigInt = new BigInteger(1, digest.digest());
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        }
        catch (IOException e) { throw new RuntimeException("Unable to process file for MD5", e); }
        finally {
            try { inputStream.close(); }
            catch (IOException e) { Log.e("TAG", "Exception on closing MD5 input stream", e); }
        }
    }


    public void settingsDialog(View v) throws Exception {

	    final Dialog dialog = new Dialog(StartScreen.this);

	    View.OnClickListener listener = new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    switch (v.getId()) {
				    case R.id.buttonClose:
					    dialog.dismiss();
					    return;
				    case R.id.buttonDelete:
					    if (!fileDir.exists()) return;
						clearApplicationData();
						findViewById(R.id.startButton).setEnabled(false);
						Toast.makeText(StartScreen.this, "DELETED", Toast.LENGTH_LONG).show();
					    return;
			    }
		    }
	    };

	    dialog.findViewById(R.id.buttonClose).setOnClickListener(listener);
	    dialog.findViewById(R.id.buttonDelete).setOnClickListener(listener);
	    dialog.setContentView(R.layout.dialog_view);
	    dialog.setTitle("Settings");
        dialog.show();
    }

    public void clearApplicationData() {

        Log.d("FILEDIR IS ", fileDir.getAbsolutePath());
        Log.d("APPDIR IS", appDir.getAbsolutePath());

        if (!fileDir.exists()) return;
        String[] children = fileDir.list();
        for (String s : children) {
            if (!s.equals("lib")) {
                deleteDir(new File(fileDir, s));
                Log.d("clearApplicationData()", "deleted: "+s);
            }
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) return false;
            }
        }
        return dir.delete();
    }

    public void internetDialog() throws Exception {
        new AlertDialog.Builder(StartScreen.this)
		    .setMessage("Ghost Catcher needs to download a file. Data charges may apply. \n\nDo you want to continue?")
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
					@Override
	            public void onClick(DialogInterface dialog, int which) {
	                new DownloadZipFile().execute(url);
	                dialog.dismiss();
	            }
	        })
			.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                //Button startButton = (Button) findViewById(R.id.startButton);
	                //startButton.setEnabled(false);
	                dialog.dismiss();

	                finish();
	            }
	        })
		    .show();
    }

    //Extract zip calls Asynctask
    public void unzip() throws IOException {
        mProgressDialog = new ProgressDialog(StartScreen.this);
	    mProgressDialog.setMessage("Extracting the downloaded file...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();
        new UnZipTask().execute(zipFile);
    }

    boolean starting;
    public void start(View view) {
        if (!starting) {
            starting = true;
            Utils.checkNetworkAvailability(this, new Utils.Callback<Boolean>() {
                @Override
                public void Run(Boolean connected) {
                    starting = false;
                    if (!connected) {
	                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		                    @Override
		                    public void onClick(DialogInterface dialog, int which) {
			                    switch (which) {
				                    case DialogInterface.BUTTON_POSITIVE:
					                    Intent myIntent = new Intent(StartScreen.this, MainActivity.class);
					                    startActivity(myIntent);
					                    break;

				                    case DialogInterface.BUTTON_NEGATIVE:
					                    finish();
					                    break;
			                    }
		                    }
	                    };

                        new AlertDialog.Builder(StartScreen.this)
		                        .setMessage("You are not connected to the internet. You will not participate in Out-of-app Experiences. Continue Anyway?")
		                        .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener)
		                        .show();
                    }
                    else {
                        Intent myIntent = new Intent(StartScreen.this, MainActivity.class);
                        startActivity(myIntent);
                    }
                }
            });
        }

    }

    public void credits(View view) {
        Intent intent = new Intent(StartScreen.this, Credits.class);
        startActivity(intent);

    }

    //-This is method is used for Download Zip file from server and store in Desire location.
    class DownloadZipFile extends AsyncTask<String, String, String> {
        boolean result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(StartScreen.this);
            mProgressDialog.setMessage("Downloading file. Please wait...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {

            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // Get Music file length
                int lengthOfFile = connection.getContentLength();
                // input stream to read file - with 8k buffer

                InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);

                // Output stream to write file in SD card
                OutputStream output = new FileOutputStream(zipFile);

                byte data[] = new byte[1024];
                long total = 0;
	            for (int count; (count = input.read(data)) != -1;)  {
                    total += count;
                    // Publish the progress which triggers onProgressUpdate method
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // Write data to file
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();
                // Close streams
                output.close();
                input.close();

                //Update flag when done
                result = true;

            }
            catch (Exception e) { Log.e("Error: ", e.getMessage()); }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            if (!result) return;
            Log.d("UNZIP", "zipfile md5 is: " + calculateMD5(new File(zipFile)));

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            StartUnzip();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                    }
                }
            };

            if (!calculateMD5(new File(zipFile)).equals("c95917caae58436218600f063c3ef9cf")) {
                Log.d("UNZIP", "CORRUPT FILE. MAN THE HARPOONS. NOOOOOOO");
                new AlertDialog.Builder(StartScreen.this)
		                .setMessage("File did not match the Authentication Signature, Continue anyway?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
		                .show();
            }
            else StartUnzip();
        }
    }

    public void StartUnzip() {
        try {
            Log.d("UNZIP", "NOT CORRUPT FILE. YAAAY");
            unzip();
        }
        catch (IOException e) {  e.printStackTrace(); }
    }

    private class UnZipTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            String filePath = params[0];
            Log.d("FILE PATH IS", filePath);

            Log.d("UNZIP LOCATION IS", unzipLocation);

            //File archive = new File(filePath);
            try { new UnzipUtil(zipFile, unzipLocation).unzip(); }
            catch (Exception e) { return false; }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();

            //Now delete the zip file since it takes up 360000000 bits
            new File(zipFile) .delete();
        }
    }

    public class UnzipUtil {
        private String zipFile;
        private String location;

        public UnzipUtil(String zipFile, String location) {
            this.zipFile = zipFile;
            this.location = location;

            dirChecker("");
        }

        public void unzip() {
            try {
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);

                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    if (ze.isDirectory()) dirChecker(ze.getName());
                    else {
                        FileOutputStream fout = new FileOutputStream(new File(location + "/" + ze.getName()));
                        //Log.e("uz", location+ "/"+ ze.getTitle());
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                        fout.close();
                        zin.closeEntry();
                    }
                }
                zin.close();
            }
            catch (Exception e) { Log.e("Decompress", "unzip", e); }
        }

        private boolean dirChecker(String dir) {
            File f = new File(location + "/" + dir);
            if (!f.isDirectory()) {
                //Log.e("DirChecker", dir);
                return f.mkdirs();
            }
            return false;
        }
    }
}