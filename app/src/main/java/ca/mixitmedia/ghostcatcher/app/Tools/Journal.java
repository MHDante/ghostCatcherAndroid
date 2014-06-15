package ca.mixitmedia.ghostcatcher.app.Tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

import ca.mixitmedia.ghostcatcher.app.R;

public class Journal extends ToolFragment {

    ViewSwitcher viewSwitcher;
    ImageButton bio;
    ImageButton todo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_journal, container, false);
        view.setPivotX(0);
        view.setPivotY(view.getMeasuredHeight());
        viewSwitcher = (ViewSwitcher) view.findViewById(R.id.journal_switcher);
        bio = (ImageButton) view.findViewById(R.id.arrow_bio);
        todo = (ImageButton) view.findViewById(R.id.arrow_to_do);

        bio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //Toast.makeText(getApplicationContext(), "Showing previous view..", Toast.LENGTH_SHORT).show();
                viewSwitcher.showPrevious();
            }
        });
        todo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //Toast.makeText(getApplicationContext(), "Showing previous view..", Toast.LENGTH_SHORT).show();
                viewSwitcher.showNext();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        gcMain.hideGears(false, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        gcMain.showGears();
    }

    @Override
    public int getGlyphID() {
        return (R.drawable.icon_journal);
    }

    public boolean checkClick(View view) {
        return false;
    }

    @Override
    protected int getAnimatorId(boolean enter) {
        return (enter) ? R.animator.rotate_in_from_right : R.animator.rotate_out_to_left;
    }


}
