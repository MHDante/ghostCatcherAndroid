package ca.mixitmedia.ghostcatcher.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class ToolFragment extends Fragment {


    protected ToolInteractionListener gcMain;

    public abstract boolean checkClick(View view);

    @Override
    public Animator onCreateAnimator(int transit, final boolean enter, int nextAnim) {
        final int animatorId = (enter) ? R.animator.rotate_in_from_right : R.animator.rotate_out_to_right;
        final Animator anim = AnimatorInflater.loadAnimator(getActivity(), animatorId);
        getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (enter) getView().setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        return anim;
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

    public interface ToolInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);

        public void hideGears(String s);

        public void swapTo(String fragment);
    }

}
