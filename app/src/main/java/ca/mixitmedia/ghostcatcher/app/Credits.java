package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class Credits extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.credits, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
	    switch (item.getItemId()) {
	        case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}