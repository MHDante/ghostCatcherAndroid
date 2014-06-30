package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class StartScreen extends Activity {

    private ProgressDialog mProgressDialog;

    private String url;
    private File fileDir;
    private String cacheDir;
    private String unzipLocation;
    private String zipFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        fileDir = new File(getExternalFilesDir("mixitmedia"), "ghostcatcher");
        Log.d("Filepaths 1 :", fileDir.getPath());
        cacheDir = getExternalCacheDir().getPath();
        Log.d("Filepaths 2 :", cacheDir);
        unzipLocation = getExternalFilesDir("mixitmedia").getPath();
        Log.d("Filepaths 3 :", unzipLocation);
        zipFile = cacheDir+"/exp.zip";
        Log.d("Filepaths 4 :", zipFile);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);


        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi =  connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile =  connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        url = this.getString(R.string.url);
        File file = new File(zipFile);

        Button bCont = (Button) findViewById(R.id.button1);
        Button bNew   =   (Button) findViewById(R.id.button2);
        Button bSettings = (Button) findViewById(R.id.button3);
        Button bCredits = (Button) findViewById(R.id.button4);

        bCont.setEnabled(false);
        bCredits.setEnabled(false);


        if (!fileDir.exists()) {
            if (file.exists()) {
                Log.d("UNZIP", "zipfile md5 is: " + fileToMD5(zipFile));
                if ( fileToMD5(zipFile).equals("e30fa973ee4d9573b907b00d376e67aa") ) {
                    try {
                        Log.d("UNZIP", "NOT CORRUPT FILE. YAAAY");

                        unzip();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.d("UNZIP", "CORRUPT FILE. MAN THE HARPOONS. NOOOOOOO");
                }
            }
            else {
                if (wifi.isAvailable())
                    // Trigger Async Task (onPreExecute method)
                    new DownloadZipFile().execute(url);
                else if (mobile.isAvailable()) {
                    //DownloadAre you sure? If yes, ...
                    //Toast.makeText(this,"ARE YOU SURE?",Toast.LENGTH_LONG).show();
                    try {
                        internetDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                    Toast.makeText(this,"NO INTERNET",Toast.LENGTH_LONG).show();
            }
        }
        else if(fileDir.list().length == 0) {
            try {
                unzip();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
            byte [] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void settingsDialog(View v) throws Exception{


        //Inflate listview
        //Listview inside scrollview inside linear layout


        AlertDialog dialog;

        final String[] items = {" 1 "," 2 "," 3 "," 4 "};

        final ArrayList selectedItems = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    // indexSelected contains the index of item (of which checkbox checked)
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            selectedItems.add(indexSelected);
                        } else if (selectedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            selectedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                });
        builder.setTitle("IS THIS WHAT YOU WANTED DANTE?");

        dialog = builder.create();
        dialog.show();
    }

    //From internet
    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    public void internetDialog() throws Exception
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartScreen.this);

        builder.setMessage("Ghost Catcher needs to download a file. Overage charges may apply. \n\nDo you want to continue?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DownloadZipFile().execute(url);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.show();
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

    //Extract zip calls Asynctask
    public void unzip() throws IOException {
        mProgressDialog = new ProgressDialog(StartScreen.this);
        mProgressDialog.setMessage("Extracting the downloaded file...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        new UnZipTask().execute(zipFile);
    }


    private class UnZipTask extends AsyncTask<String, Void, Boolean> {
        private int isExtracted = 0;

        @Override
        protected Boolean doInBackground(String... params) {

            String filePath = params[0];
            Log.d("FILE PATH IS", filePath);

            Log.d("UNZIP LOCATION IS", unzipLocation);

            File archive = new File(filePath);
            try {
                ZipFile zipfile = new ZipFile(archive);
                int fileCount = zipfile.size();
                mProgressDialog.setMax(zipfile.size());
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    isExtracted++;
                    unzipEntry(zipfile, entry, unzipLocation);
                    mProgressDialog.setProgress((isExtracted * 100) / fileCount);
                }

            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
        }

        private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

            if (entry.isDirectory()) {
                String name = entry.getName();
                createDir(new File(outputDir +'/'+ name));
                return;
            }

            File outputFile = new File(outputDir  +'/'+ entry.getName());


            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        }

        private void createDir(File dir) {
            if (dir.exists()) {
                return;
            }
            if (!dir.mkdirs()) {
                throw new RuntimeException("Can not create dir " + dir);
            }
        }
    }


    public void start(View view){
        Intent myIntent = new Intent(StartScreen.this, MainActivity.class);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}