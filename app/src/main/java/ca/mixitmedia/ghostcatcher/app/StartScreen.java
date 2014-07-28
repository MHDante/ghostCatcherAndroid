package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StartScreen extends Activity {

    private ProgressDialog mProgressDialog;

    private String url;
    private String unzipLocation;
    private String zipFile;
    private File fileDir, appDir;

    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        zipFile = cacheDir + "/exp.zip";
        Log.d("Filepaths 4 :", zipFile);

        setContentView(R.layout.activity_start_screen);

        url = getString(R.string.url);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        Button creditsButton = (Button) findViewById(R.id.creditsButton);

        continueButton.setEnabled(false);
        creditsButton.setEnabled(false);


		if (!fileDir.exists() || fileDir.list().length == 0) {
            if ((new File(zipFile)).exists()) unzipFile();
			else attemptDownloadZipFile();
        }
    }

	public void unzipFile() {
		Log.d("UNZIP", "zipfile md5 is: " + fileToMD5(zipFile));
		if (fileToMD5(zipFile).equals("c95917caae58436218600f063c3ef9cf")) {
			try {
				Log.d("UNZIP", "NOT CORRUPT FILE. YAAAY");
				unzip();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.d("UNZIP", "CORRUPT FILE. MAN THE HARPOONS. NOOOOOOO");
		}
	}

	public void attemptDownloadZipFile() {
		final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable()) new DownloadZipFile().execute(url); // Trigger Async Task (onPreExecute method)
		else if (mobile.isAvailable()) {
			try { internetDialog(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show();
	}

    public void settingsDialog(View v) throws Exception {

        final Dialog dialog = new Dialog(StartScreen.this);
        dialog.setContentView(R.layout.dialog_view);
        dialog.setTitle("Settings");

        Button close = (Button) dialog.findViewById(R.id.buttonClose);
        Button delete = (Button) dialog.findViewById(R.id.buttonDelete);
        // if button is clicked, close the custom dialog
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileDir.exists()) {
                    clearApplicationData();

                    Button newGame = (Button) findViewById(R.id.startButton);
                    newGame.setEnabled(false);

                    Toast.makeText(StartScreen.this, "DELETED", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
    }

    public void clearApplicationData() {


        Log.d("FILEDIR IS ", fileDir.getAbsolutePath());
        Log.d("APPDIR IS", appDir.getAbsolutePath());

        if (fileDir.exists()) {
            String[] children = fileDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(fileDir, s));
                    Log.d("", "deleted");
                }
            }
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public void internetDialog() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartScreen.this);

        builder.setMessage("Ghost Catcher needs to download a file. Data charges may apply. \n\nDo you want to continue?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DownloadZipFile().execute(url);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Button startButton = (Button) findViewById(R.id.startButton);
                //startButton.setEnabled(false);
                dialog.dismiss();

                finish();
            }
        });

        builder.show();
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

    public void start(View view) {
        Intent myIntent = new Intent(StartScreen.this, MainActivity.class);
        startActivity(myIntent);
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
            int count;

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
                while ((count = input.read(data)) != -1) {
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

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            if (result) {
                try {
                    unzip();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UnZipTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            String filePath = params[0];
            Log.d("FILE PATH IS", filePath);

            Log.d("UNZIP LOCATION IS", unzipLocation);

            //File archive = new File(filePath);
            try {
                UnzipUtil d = new UnzipUtil(zipFile, unzipLocation);
                d.unzip();

            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();

            //Now delete the zipfile since it takes up 360000000 bits
			(new File(zipFile)).delete();
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

                    if (ze.isDirectory()) {
                        dirChecker(ze.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(new File(location + "/" + ze.getName()));
                        //Log.e("uz", location+ "/"+ ze.getName());
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
            } catch (Exception e) {
                Log.e("Decompress", "unzip", e);
            }
        }

        private boolean dirChecker(String dir) {
            File f = new File(location + "/" + dir);
			return !f.isDirectory() && f.mkdirs();
		}
    }
}