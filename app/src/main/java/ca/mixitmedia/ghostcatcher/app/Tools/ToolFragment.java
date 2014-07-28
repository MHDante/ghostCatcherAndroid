package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.views.ToolLightButton;

public abstract class ToolFragment extends Fragment {

	/**
	 * the parent MainActivity which is to hold this ToolFragment
	 */
	protected MainActivity gcMain;
    public ToolLightButton toolLight;
    private boolean enabled;

    /**
	 * Checks whether the click is to be handled by the parent MainActivity, or will be handled
	 * solely by this ToolFragment
	 * @param view the clicked view
	 * @return true if touch needs to be handles by the parent MainActivity, otherwise false
	 */
	public boolean checkClick(View view) {
		return false;
	}

	/**
	 * Sets the toolbar icon of the ToolFragment
	 * @return the resource ID of the toolbar icon
	 */
	public int getGlyphId(){
        return Utils.findDrawableIDByName("icon_"+ this.getClass().getName());
    };

	@Override
	public Animator onCreateAnimator(int transit, final boolean enter, int nextAnim) {


		int animatorId = getAnimatorId(enter);
		setupAnimator(enter);
		Animator anim = AnimatorInflater.loadAnimator(getActivity(), animatorId);

		if (anim != null) getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
		anim.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				//MainActivity.transitionInProgress = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				//MainActivity.transitionInProgress = false;
				if (getView() != null) getView().setLayerType(View.LAYER_TYPE_NONE, null);
				afterAnimation(enter);

			}
		});


		return anim;
	}

	protected int getAnimatorId(boolean enter) {
		if (enter) SoundManager.playSound(SoundManager.Sounds.metalClick);
		return ((enter) ? R.animator.transition_in_from_bottom : R.animator.transition_out_from_bottom);
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
	public void onDetach() {
		super.onDetach();
		gcMain = null;

	}

	public pivotOrientation getPivotOrientation(boolean enter) {
		return pivotOrientation.BOTTOM;
	}

	public void setupAnimator(boolean enter) {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		pivotOrientation orientation = getPivotOrientation(enter);

		switch (orientation) {
			case TOP:
				getView().setPivotX(width / 2);
				getView().setPivotY(-(width / 2));
				break;

			case BOTTOM:
				getView().setPivotX(width / 2);
				getView().setPivotY(height + width / 2);
				break;

			case LEFT:
				getView().setPivotX(-(width / 2));
				getView().setPivotY(height / 2);
				break;

			case RIGHT:
				getView().setPivotX(width + (width / 2));
				getView().setPivotY(height / 2);
				break;

		}

		//Log.d("Pivot", "enter: " + enter + "PivotY:" +
		// getView().getPivotY() + "PivotX:" + getView().getPivotX());
	}

	public void afterAnimation(boolean enter) {

	}

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected Queue<ToolMessage> pendingMessages = new LinkedList<>();
    public boolean hasNotification() {
        return pendingMessages.size()!=0;
    }

    protected enum pivotOrientation {
		LEFT,   //Pivot from the left
		RIGHT,  //Pivot from the right
		TOP,    //Pivot from the top
		BOTTOM //Pivot from the bottom
	}

    public static class ToolMessage{
        public Object data;
        public boolean lock;
    }
}
