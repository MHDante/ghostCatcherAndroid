package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Dante on 2014-07-27
 */
public class Typewriter extends ScrollView {

    public TextView textView;

    CharSequence mText;
    int mIndex;
    long mDelay = 50; //Default 200ms delay
    boolean userIsScrolling = false;
    private Handler mHandler = new Handler();

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);

        textView = new TextView(context);
        textView.setText("");
        textView.setPadding(10, 0, 10, 0);
        textView.setGravity(Gravity.CENTER | Gravity.BOTTOM);

	    LinearLayout linearLayout = new LinearLayout(context);
	    linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(textView, params);
        super.addView(linearLayout);

        //super.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            userIsScrolling = true;
        //        } else if (event.getAction() == MotionEvent.ACTION_UP)
        //            userIsScrolling = false;
        //        return false;
        //    }
        //});
    }

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        textView.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            textView.setText(mText.subSequence(0, mIndex++));
            post(new Runnable() {
                @Override
                public void run() {
                    fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            if (mIndex <= mText.length()) mHandler.postDelayed(characterAdder, mDelay);
        }
    };

    public void concatenateText(CharSequence text) {
        mText = TextUtils.concat(textView.getText(), text);
        mIndex = textView.getText().length();
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }


}