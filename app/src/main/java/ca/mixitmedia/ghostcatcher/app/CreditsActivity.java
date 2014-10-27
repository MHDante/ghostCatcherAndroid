package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import ca.mixitmedia.ghostcatcher.experience.gcEngine;


public class CreditsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        final ImageView ad1 = (ImageView) findViewById(R.id.creds);
//        new AsyncTask<Void,Void,Void>(){
//
//            Bitmap ad1bm;
//            @Override
//            protected Void doInBackground(Void... voids) {
//                try {
//                    URL ad1url = new URL("http://mixitmedia.ca/credits.jpg");
//                    ad1bm= BitmapFactory.decodeStream(ad1url.openConnection().getInputStream());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                if (ad1bm!=null)ad1.setImageBitmap(ad1bm);
//                ad1.animate().setDuration(300).alpha(1).setStartDelay(1000).start();
//            }
//        }.execute();

        Bitmap bmp = BitmapFactory.decodeFile(gcEngine.root+"/credits.jpg");
        ad1.setImageBitmap(bmp);

    }

}
