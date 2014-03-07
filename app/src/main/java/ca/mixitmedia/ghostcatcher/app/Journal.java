package ca.mixitmedia.ghostcatcher.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;

public class Journal extends Activity {

    static String[] items = {"lorem", "ipsum", "dolor", "sit", "amet",
            "consectetuer", "adipiscing", "elit", "morbi", "vel", "ligula",
            "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat",
            "placerat", "ante", "porttitor", "sodales", "pellentesque",
            "augue", "purus"};
    AdapterViewFlipper flipper;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_journal);

        flipper = (AdapterViewFlipper) findViewById(R.id.NotesFlipper);
        flipper.setAdapter(new ArrayAdapter<String>(this, R.layout.flip_tester, items));
        flipper.setFlipInterval(2000);
        flipper.startFlipping();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.rotate_in_from_left, R.anim.rotate_out_to_right);
    }
}
