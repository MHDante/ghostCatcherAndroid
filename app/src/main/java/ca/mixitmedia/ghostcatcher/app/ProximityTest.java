package ca.mixitmedia.ghostcatcher.app;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by Dante on 2014-07-27.
 */
public abstract class ProximityTest extends AsyncTask<String, Void, String> {

    final String BaseURL = "http://mixitmedia.ca/proximity/writetest.php?";
    String ProximityURL;

    public ProximityTest(String action, String location, String status) {
        ProximityURL = BaseURL+"action="+action+"&location="+location+"&status="+status;
    }

    public ProximityTest() {
        this("write", "zacklocation", "activated");
    }

    @Override
    protected final String doInBackground(String... params) {
        try {
            BufferedReader in;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            URI website = new URI(ProximityURL);

            request.setURI(website);
            HttpResponse response = httpclient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            return in.readLine();
        }
        catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
            return null;
        }
    }

    @Override
    protected final void onPostExecute(String s) {
        HandleServerMessage(s);
    }

    public abstract void HandleServerMessage(String s);
}