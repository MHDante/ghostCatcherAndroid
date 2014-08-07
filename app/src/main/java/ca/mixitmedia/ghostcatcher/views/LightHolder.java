package ca.mixitmedia.ghostcatcher.views;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created  by Dante on 2014-08-05.
 */
public class LightHolder extends Fragment {

    BounceScrollView scrollView;
    ImageView leftGear, rightGear;
    LightButton leftLight, rightLight;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lightholder,container);
        scrollView = (BounceScrollView)v.findViewById(R.id.bounceScrollView);
        leftGear = (ImageView)v.findViewById(R.id.left_gear);
        rightGear = (ImageView)v.findViewById(R.id.right_gear);
        leftLight = (LightButton)v.findViewById(R.id.left_toolLight);
        rightLight = (LightButton)v.findViewById(R.id.right_toolLight);
        scrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(BounceScrollView scrollView, int x, int y, int oldx, int oldy) {
                float xRotation = x/(float)scrollView.getMaxScrollAmount();
                leftGear.setRotation((xRotation)*360);
                rightGear.setRotation((xRotation)*360);
            }
        });
        return v;
    }

    public interface ScrollViewListener {

        void onScrollChanged(BounceScrollView scrollView, int x, int y, int oldx, int oldy);

    }
    private static class BounceScrollView extends HorizontalScrollView {
        private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;

        private Context mContext;
        private int mMaxXOverscrollDistance;
        GestureDetector mGestureDetector;
        private ScrollViewListener scrollViewListener = null;

        public BounceScrollView(Context context) {
            super(context);
            mContext = context;
            initBounceScrollView();
        }

        public BounceScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            initBounceScrollView();
        }

        public BounceScrollView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mContext = context;
            initBounceScrollView();
        }

        public void setScrollViewListener(ScrollViewListener scrollViewListener) {
            this.scrollViewListener = scrollViewListener;
        }

        @Override
        protected void onScrollChanged(int x, int y, int oldx, int oldy) {
            super.onScrollChanged(x, y, oldx, oldy);
            if(scrollViewListener != null) {
                scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
            }
        }

        private void initBounceScrollView() {
            //get the density of the screen and do some maths with it on the max overscroll distance
            //variable so that you get similar behaviors no matter what the screen size
            mGestureDetector = new GestureDetector(getContext(), new MyGestureDetector());
            setOnTouchListener(new SnapListener());

            final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            final float density = metrics.density;

            mMaxXOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
            //This is where the magic happens, we have replaced the incoming maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, mMaxXOverscrollDistance, maxOverScrollY, isTouchEvent);
        }


        class SnapListener implements View.OnTouchListener {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If the user swipes
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = getScrollX();
                    int maxScroll = getMaxScrollAmount();
                    smoothScrollTo(scrollX < maxScroll ? 0 : maxScroll * 2, 0);
                    return true;
                } else {
                    return false;
                }
            }
        }

        private static final int SWIPE_MIN_DISTANCE = 5;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
	                if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		                float distance = e1.getX() - e2.getX();
		                //right to left
			            if (distance > SWIPE_MIN_DISTANCE) {
				            smoothScrollTo(getMaxScrollAmount() * 2, 0);
				            return true;
			            }
			            //left to right
			            else if (-distance > SWIPE_MIN_DISTANCE) {
				            smoothScrollTo(0, 0);
				            return true;
			            }
	                }
                } catch (Exception e) {
                    Log.e("There was an error processing the Fling event:", e.getMessage());
                }
                return false;
            }
        }
    }
}