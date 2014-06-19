package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;

public abstract class ToolFragment extends Fragment {


    protected MainActivity gcMain;

    public abstract boolean checkClick(View view);

    public abstract int getGlyphID();

    protected enum pivotOrientation{
        LEFT,   //Pivot from the left
        RIGHT,  //Pivot from the right
        TOP,    //Pivot from the top
        BOTTOM; //Pivot from the bottom
    }

    @Override
    public Animator onCreateAnimator(int transit, final boolean enter, int nextAnim) {


        int animatorId = getAnimatorId(enter);
        setupAnimator(enter);
        Animator anim = AnimatorInflater.loadAnimator(getActivity(), animatorId);
        ;
        if (anim != null) getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                MainActivity.transitionInProgress = true;
                if (enter) {
                    //gcMain.gearsBackground.start();
                }
            }
//dtfdt
            @Override
            public void onAnimationEnd(Animator animation) {
                MainActivity.transitionInProgress = false;
                if (getView() != null) getView().setLayerType(View.LAYER_TYPE_NONE, null);

                if (enter) {
                    //gcMain.gearsBackground.stop();
                }

                afterAnimation(enter);

            }
        });


        return anim;
    }

    protected int getAnimatorId(boolean enter) {
        return ((enter) ? R.animator.rotate_in_from_left : R.animator.rotate_out_to_right);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            gcMain = (MainActivity) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " is not a GhostCatcher Activity");
        }
    }

    @Override
    public void onPause() {
        gcMain.ToolMap.get(this.getClass()).setSelected(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        gcMain.ToolMap.get(this.getClass()).setSelected(true);
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gcMain = null;

    }

    public pivotOrientation getPivotOrientation(boolean enter){
        return pivotOrientation.BOTTOM;
    }

    public void setupAnimator(boolean enter) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        pivotOrientation orientation = getPivotOrientation(enter);

        switch(orientation){
            case TOP: getView().setPivotX(width / 2);
                getView().setPivotY(-(width / 2));
                break;

            case BOTTOM: getView().setPivotX(width / 2);
                getView().setPivotY(height + width / 2);
                break;

            case LEFT:getView().setPivotX(-(width / 2));
                getView().setPivotY(height / 2);
                break;

            case RIGHT:getView().setPivotX(width + (width / 2));
                getView().setPivotY(height / 2);
                break;

        }

        //Log.d("Pivot", "enter: " + enter + "PivotY:" + getView().getPivotY() + "PivotX:" + getView().getPivotX());
    }

    public void afterAnimation(boolean enter) {
    }


}
