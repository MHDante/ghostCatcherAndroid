package ca.mixitmedia.ghostcatcher.views;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created  by Dante on 2014-08-05.
 */
public class LightHolder extends Fragment {

    public enum State{Left, Right}

    BounceScrollView scrollView;
    ImageView leftGear, rightGear;
    LightButton leftLight, rightLight;

    LightButton arrowLeft, arrowRight;
    State state = State.Left;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lightholder,container);
        scrollView = (BounceScrollView)v.findViewById(R.id.bounceScrollView);
        leftGear = (ImageView)v.findViewById(R.id.left_gear);
        rightGear = (ImageView)v.findViewById(R.id.right_gear);
        leftLight = (LightButton)v.findViewById(R.id.left_toolLight);
        rightLight = (LightButton)v.findViewById(R.id.right_toolLight);
        arrowLeft = (LightButton)v.findViewById(R.id.left_arrowLight);
        arrowLeft.setGlyphID(R.drawable.arrow_left);
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(0, 0);
            }
        });
        arrowLeft.setState(LightButton.State.unlit);
        arrowRight = (LightButton)v.findViewById(R.id.right_arrowLight);
        arrowRight.setGlyphID(R.drawable.arrow_right);
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(scrollView.getMaxScrollAmount() * 2, 0);
            }
        });
        arrowRight.setState(LightButton.State.unlit);
        scrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(BounceScrollView scrollView, int x, int y, int oldx, int oldy) {
                float maxScroll = scrollView.getMaxScrollAmount();
                if (state == State.Left && x > maxScroll){
                    state = State.Right;
                    arrowRight.setState(LightButton.State.lit);
                    arrowLeft.setState(LightButton.State.unlit);
                    leftLight.setVisibility(View.GONE);
                    rightLight.setVisibility(View.VISIBLE);

                }
                else if (state == State.Right && x < maxScroll){
                    arrowRight.setState(LightButton.State.unlit);
                    arrowLeft.setState(LightButton.State.lit);
                    state = State.Left;
                    rightLight.setVisibility(View.GONE);
                    leftLight.setVisibility(View.VISIBLE);
                }
                float alpha  = 1f - Utils.Triangle(x, maxScroll)/(maxScroll);
                float xRotation = x / (maxScroll * 2);
                leftLight.setAlpha(x > maxScroll ? 0 : alpha);
                rightLight.setAlpha(x < maxScroll ? 0 : alpha);
                leftGear.setRotation((xRotation)*360);
                rightGear.setRotation((xRotation)*360);

                if (x == 0f || x == maxScroll * 2) {
                    arrowLeft.setState(LightButton.State.unlit);
                    arrowRight.setState(LightButton.State.unlit);
                }
            }

            @Override
            public void onFling(boolean rightToLeft) {
                if (rightToLeft) {
                    arrowRight.setState(LightButton.State.unlit);
                    arrowLeft.setState(LightButton.State.lit);
                }
                else {
                    arrowRight.setState(LightButton.State.lit);
                    arrowLeft.setState(LightButton.State.unlit);
                }
            }
        });
        return v;
    }

    public interface ScrollViewListener {
        void onScrollChanged(BounceScrollView scrollView, int x, int y, int oldx, int oldy);
        void onFling(boolean rightToLeft);
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
            if (scrollViewListener != null) scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
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
                }
                else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int maxScroll = getMaxScrollAmount();
                    smoothScrollTo(getScrollX() < maxScroll ? 0 : maxScroll * 2, 0);
                    return true;
                }
                else return false;
            }
        }

        private static final int SWIPE_MIN_DISTANCE = 5;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    //right to left
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        smoothScrollTo(getMaxScrollAmount() * 2, 0);
                        scrollViewListener.onFling(true);
                        return true;
                    }
                    //left to right
                    else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        smoothScrollTo(0, 0);
                        scrollViewListener.onFling(false);
                        return true;
                    }
                }
                catch (Exception e) {
                //Log.e("There was an error processing the Fling event:", e.getMessage());
                }
                return false;
            }
        }
    }
}