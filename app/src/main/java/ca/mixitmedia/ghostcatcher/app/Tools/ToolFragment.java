package ca.mixitmedia.ghostcatcher.app.Tools;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.MainActivity;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.SoundManager;
import ca.mixitmedia.ghostcatcher.experience.gcAction;
import ca.mixitmedia.ghostcatcher.views.LightButton;

public abstract class ToolFragment extends Fragment {

    public LightButton getToolLight() {
        return toolLight;
    }

    public void setToolLight(LightButton toolLight) {
        this.toolLight = toolLight;
        String name = ((Object) this).getClass().getSimpleName().toLowerCase();
        int id = Utils.findDrawableIDByName("icon_" + name);
        if (id != 0) {
            toolLight.setGlyphID(id);
        }
    }

   public gcAction recievedAction;

    private LightButton toolLight;
    /**
     * the parent MainActivity which is to hold this ToolFragment
     */
    protected MainActivity gcMain;
    protected Queue<ToolMessage> pendingMessages = new LinkedList<>();
    private boolean enabled = false;


    /**
     * Checks whether the click is to be handled by the parent MainActivity, or will be handled
     * solely by this ToolFragment
     *
     * @param view the clicked view
     * @return true if touch needs to be handles by the parent MainActivity, otherwise false
     */
    public boolean checkClick(View view) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
    }

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

    @Override
    public void onResume() {
        super.onResume();
        if(pendingMessages.size()>0 && pendingMessages.peek().action.getType() == gcAction.Type.ENABLE_TOOL){
            completeAction();
        }
        toolLight.setState(LightButton.State.lit);
    }

    protected void completeAction(){
        if (recievedAction != null) {
            gcAction action = pendingMessages.remove().action;
            if (action != recievedAction)
                throw new RuntimeException("Toolfragment RecievedAction out of sync.");
            recievedAction = null;
            gcMain.experienceManager.unLock(action);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        toolLight.setState(LightButton.State.unlit);
    }

    protected int getAnimatorId(boolean enter) {
        if (enter) {
	        SoundManager.playSound(SoundManager.Sounds.metalClick);
	        return R.animator.transition_in_from_bottom;
        }
        return R.animator.transition_out_from_bottom;
    }

    public boolean isSelected() {
        return Tools.Current() == this;
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

        int width = Utils.GetScreenWidth(getActivity());
        int height = Utils.GetScreenHeight(getActivity());

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
        if(enabled)toolLight.setState(LightButton.State.flashing);
        else toolLight.setState(LightButton.State.disabled);

    }

    public boolean hasNotification() {
        return pendingMessages.size() != 0;
    }

    public void sendMessage(ToolMessage toolMessage) {
        if (recievedAction != null) throw new RuntimeException("ToolFragment Already has a lock");
        if (toolMessage.lock) recievedAction = toolMessage.action;
        pendingMessages.add(toolMessage);

    }

    protected enum pivotOrientation {
        LEFT,   //Pivot from the left
        RIGHT,  //Pivot from the right
        TOP,    //Pivot from the top
        BOTTOM  //Pivot from the bottom
    }

    public static class ToolMessage {
        public final gcAction action;
        public final boolean lock;

        public ToolMessage(gcAction action, boolean lock) {
            this.action = action;
            this.lock = lock;
        }
    }
}
