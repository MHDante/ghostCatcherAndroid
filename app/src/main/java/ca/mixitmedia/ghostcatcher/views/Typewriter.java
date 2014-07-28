package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Dante on 2014-07-27.
 */
public class Typewriter extends ScrollView {

    public TextView textView;

    CharSequence mText;
    int mIndex;
    long mDelay = 200; //Default 200ms delay
    boolean userIsScrolling = false;

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        textView = new TextView(context);
        linearLayout.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        super.addView(linearLayout);
        super.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    userIsScrolling = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    userIsScrolling = false;
                return false;
            }
        });
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            textView.setText(mText.subSequence(0, mIndex++));
            if (!userIsScrolling) Typewriter.super.fullScroll(View.FOCUS_DOWN);
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        textView.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }
    public void concatenateText(CharSequence text){
        mText = TextUtils.concat(textView.getText(), text);
        mIndex = textView.getText().length();
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}