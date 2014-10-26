package ca.mixitmedia.ghostcatcher.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created  by Dante on 2014-08-05.
 */
public class LightHolder extends Fragment {

    public enum State{Left, Right}

    ImageView leftGear;
    LightButton leftLight, rightLight;

    LightButton arrowLeft;//, arrowRight;
    State state = State.Left;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lightholder,container);
        leftGear = (ImageView)v.findViewById(R.id.left_gear);
        leftLight = (LightButton)v.findViewById(R.id.left_toolLight);
        rightLight = (LightButton)v.findViewById(R.id.right_toolLight);
        arrowLeft = (LightButton)v.findViewById(R.id.left_arrowLight);
        arrowLeft.setGlyphID(R.drawable.arrow_left);
        arrowLeft.setState(LightButton.State.unlit);
        //arrowRight = (LightButton)v.findViewById(R.id.right_arrowLight);
        //arrowRight.setGlyphID(R.drawable.arrow_right);
        //arrowRight.setState(LightButton.State.unlit);
        return v;
    }
}