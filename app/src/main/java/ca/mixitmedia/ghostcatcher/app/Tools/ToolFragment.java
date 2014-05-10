package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.reflect.ParameterizedType;

import android.os.Handler;

import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience.gcLocation;

public abstract class ToolFragment extends Fragment {


    protected ToolInteractionListener gcMain;

    public abstract boolean checkClick(View view);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Animator onCreateAnimator(int transit, final boolean enter, int nextAnim) {


        Animator anim = setupAnimator(enter);

        //if (anim != null)
        //    getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);


        anim.addListener(new AnimatorListenerAdapter() {


            @Override
            public void onAnimationStart(Animator animation) {
                MainActivity.inProgress = true;
                //AnimationDrawable ad = (AnimationDrawable) (getActivity().findViewById(R.id.activity_bg).getBackground());
                //ad.setOneShot(false);
                //ad.start();

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                MainActivity.inProgress = false;
                //if (getView()!=null)getView().setLayerType(View.LAYER_TYPE_NONE, null);
                //AnimationDrawable ad = (AnimationDrawable) (getActivity().findViewById(R.id.activity_bg).getBackground());
                //ad.stop();

                afterAnimation(enter);

            }
        });


        return anim;
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x;
                        int height = size.y;

                        View view = getView();
                        ViewGroup.LayoutParams layout = view.getLayoutParams();

                        float maxWidth = view.getWidth();
                        float maxHeight = view.getHeight();

                        if (height > maxHeight || width > maxWidth) {
                            float ratio = Math.min(maxWidth / width, maxHeight / height);
                            layout.width = (int) (width * ratio);
                            layout.height = (int) (height * ratio);
                        }
                        view.setLayoutParams(layout);
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
        );
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            gcMain = (ToolInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gcMain = null;
    }


    public Animator setupAnimator(boolean enter) {
        final int animatorId = (enter) ? R.animator.rotate_in_from_left : R.animator.rotate_out_to_right;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        final Animator anim = AnimatorInflater.loadAnimator(getActivity(), animatorId);
        //getView().setPivotX(width / 2);
        //getView().setPivotY(height + width / 2);
        //Log.d("Pivot", "enter: " + enter + "PivotY:" + getView().getPivotY() + "PivotX:" + getView().getPivotX());
        return anim;
    }

    public void afterAnimation(boolean enter) {
    }

    public interface ToolInteractionListener {
        // TODO: Update argument type and name


        public void swapTo(Class fragment, boolean addToBackStack);



        void startDialogByLocation(String dialog);

        gcLocation getCurrentLocation();
    }

}
