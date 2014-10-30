package ca.mixitmedia.ruhaunted.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.mixitmedia.ruhaunted.Utils;
import ca.mixitmedia.ruhaunted.experience.gcEngine;

public class StartScreen extends Activity {

    String url;
    String unzipLocation;
    String zipFile;
    File fileDir, appDir;
    ProgressBar loadBar;
    TextView loadtext;
    FrameLayout loadScreen;
    int kwame = 5;
    int layer = 0;
    ImageView ad1;
    ImageView ad2;
    ImageView ad3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        loadBar = (ProgressBar) findViewById(R.id.loadbar);
        loadtext = (TextView) findViewById(R.id.loadtext);
        loadScreen = (FrameLayout) findViewById(R.id.loadScreen);

        ad1 = (ImageView)findViewById(R.id.ad1);
        ad2 = (ImageView)findViewById(R.id.ad2);
        ad3 = (ImageView)findViewById(R.id.ad3);
        new AsyncTask<Void,Void,Void>(){

            Bitmap ad1bm;
            Bitmap ad2bm;
            Bitmap ad3bm;
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL ad1url = new URL("http://mixitmedia.ca/ad1.jpg");
                    URL ad2url = new URL("http://mixitmedia.ca/ad2.jpg");
                    URL ad3url = new URL("http://mixitmedia.ca/ad3.jpg");
                    ad1bm= BitmapFactory.decodeStream(ad1url.openConnection().getInputStream());
                    ad2bm= BitmapFactory.decodeStream(ad2url.openConnection().getInputStream());
                    ad3bm= BitmapFactory.decodeStream(ad3url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                if (ad1bm!=null)ad1.setImageBitmap(ad1bm);
                if (ad2bm!=null)ad2.setImageBitmap(ad2bm);
                if (ad3bm!=null)ad3.setImageBitmap(ad3bm);
            }
        }.execute();


        fileDir = new File(getExternalFilesDir("mixitmedia"), "ghostcatcher");
        appDir = fileDir.getParentFile().getParentFile().getParentFile();
        Log.d("APPDIR IS", appDir.getAbsolutePath());
        Log.d("Filepaths 1 :", fileDir.getPath());
        String cacheDir = getExternalCacheDir().getPath();
        unzipLocation = getExternalFilesDir("mixitmedia").getPath();
        zipFile = cacheDir + "/ruhaunted.zip";

        url = "http://mixitmedia.ca/ruhaunted.zip";

        Button continueButton = (Button) findViewById(R.id.continueButton);
        Button startButton = (Button) findViewById(R.id.startButton);

        final Calendar c = Calendar.getInstance();
        final Calendar start = new GregorianCalendar(2014,9,31,19,0);
        final Calendar end = new GregorianCalendar(2014,9,31,23,59);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(kwame<5 && c.before(start)){
                    Utils.messageDialog(StartScreen.this,"Too early!", "The Event starts on " + start.getTime());
                    return;
                }
                if(kwame<5 && c.after(end)){
                    Utils.messageDialog(StartScreen.this,"Too Late!", "The Event ended on " + end.getTime());
                    return;
                }
                continueGame();

            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(kwame<5 && c.before(start)){
                    Utils.messageDialog(StartScreen.this,"Too early!", "The Event starts on " + start.getTime());
                    return;
                }
                if(kwame<5 && c.after(end)){
                    Utils.messageDialog(StartScreen.this,"Too Late!", "The Event ended on " + end.getTime());
                    return;
                }
                start();
            }
        });
        if (!fileDir.exists() || fileDir.list().length == 0) {
            continueButton.setEnabled(false);
        }
        findViewById(R.id.transmedia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (kwame >4){
                    Toast.makeText(StartScreen.this,"HI KWAME.", Toast.LENGTH_LONG).show();
                }
                kwame++;
            }
        });

    }

    private void start() {
        if (MainActivity.gcMain!=null) MainActivity.gcMain.finish();
        clearApplicationData();

        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        starting = true;
        Log.e("FILEDIR IS", fileDir.getAbsolutePath());
        if (wifi.isAvailable() && wifi.isConnected()) {
            // Trigger Async Task (onPreExecute method)
            new DownloadZipFile().execute(url);
            loadScreen.setVisibility(View.VISIBLE);
        }
        else if (mobile.isAvailable() && mobile.isConnected()) {
                internetDialog(new Runnable() {
                    @Override
                    public void run() {
                        new DownloadZipFile().execute(url);
                        loadScreen.setVisibility(View.VISIBLE);
                    }
                });
        } else {
            Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show();
        }
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

    boolean working= false;
    public void internetDialog(final Runnable onAccept){
        AlertDialog d =new AlertDialog.Builder(StartScreen.this)
		    .setMessage("RU Haunted needs to download a file. Data charges may apply. \n\nDo you want to continue?")
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onAccept.run();
                    dialog.dismiss();
                }
            })
			.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Button startButton = (Button) findViewById(R.id.startButton);
                    //startButton.setEnabled(false);
                    dialog.dismiss();
                }
            }).create();
        d.show();
    }

    static boolean pendingExperience = false;
    public void BeginExperience(){
        Uri root = Uri.parse(new File(this.getExternalFilesDir("mixitmedia"), "ghostcatcher").getAbsolutePath());
        File f = new File(root + "/Exp1Chapter1.xml");
        if (!f.exists()){
            Utils.messageDialog(this,"Download error", "The current network might be blocking the data download, Please try a different network.", new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
        if(this.hasWindowFocus()) {
            Intent myIntent = new Intent(StartScreen.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }else {
            pendingExperience = true;
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (pendingExperience) {
            BeginExperience();
        }
    }

    boolean starting;
    public void continueGame() {
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
                                        BeginExperience();
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
                        BeginExperience();
                    }
                }
            });
        }

    }

    //-This is method is used for Download Zip file from server and store in Desire location.
    class DownloadZipFile extends AsyncTask<String, Integer, String> {
        boolean result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadtext.setText("Downloading file. Please wait...");
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
                    publishProgress((int) ((total * 100) / lengthOfFile));

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
            catch (Exception e) { Log.e("Errorzzzz" +
                    ": ", e.getMessage()); }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            loadBar.setProgress(progress[0]);
            if(progress[0]>60 && layer ==0){
                ad1.animate().setDuration(300).alpha(0).start();
                layer++;
            }
        }

        @Override
        protected void onPostExecute(String unused) {
            if (!result) return;

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            new UnZipTask().execute(zipFile);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                    }
                }
            };
            new UnZipTask().execute(zipFile);
        }
    }



    private class UnZipTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            loadtext.setText("Extracting the downloaded file...");
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String filePath = params[0];
            Log.d("FILE PATH IS", filePath);

            Log.d("UNZIP LOCATION IS", unzipLocation);

            //File archive = new File(filePath);
            try {
                dirChecker("");
                unzip();
            }
            catch (Exception e) { return false; }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            loadBar.setProgress(progress[0]);
            if(progress[0]>30 && layer ==1){
                ad2.animate().setDuration(300).alpha(0).start();
                layer++;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            //Now delete the zip file since it takes up 360000000 bits
            new File(zipFile) .delete();
            BeginExperience();

        }

        public void unzip() {
            try {
                long lengthOfFile = new File(zipFile).length();
                double total = 0;
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);

                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    total += (int) ze.getSize();
                    publishProgress((int) ((total * 100) / lengthOfFile));
                    if (ze.isDirectory()) dirChecker(ze.getName());
                    else {
                        FileOutputStream fout = new FileOutputStream(new File(unzipLocation + "/" + ze.getName()));
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
            File f = new File(unzipLocation + "/" + dir);
            if (!f.isDirectory()) {
                //Log.e("DirChecker", dir);
                return f.mkdirs();
            }
            return false;
        }
    }
    }