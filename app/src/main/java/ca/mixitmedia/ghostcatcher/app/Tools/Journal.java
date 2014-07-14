package ca.mixitmedia.ghostcatcher.app.Tools;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.JournalFragment.ToDoList;
import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Journal extends ToolFragment {

    Map<String, Uri> imageFileLocationMap;

    ImageButton bio;
    ImageButton todo;

    ToDoList toDoList;
    ToDoList toDoList2;
    ToDoList toDoList3;
    ToDoList toDoList4;

    public Journal(){createImageURIs();};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_journal, container, false);
        view.setPivotX(0);
        view.setPivotY(view.getMeasuredHeight());
        //bio = (ImageButton) view.findViewById(R.id.arrow_bio);
        todo = (ImageButton) view.findViewById(R.id.arrow_to_do);

        toDoList = new ToDoList();
        toDoList2 = new ToDoList();
        toDoList3 = new ToDoList();
        toDoList4 = new ToDoList();


        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        ImageView bullet1 = (ImageView) view.findViewById(R.id.bullet_check1);
        ImageView bullet2 = (ImageView) view.findViewById(R.id.bullet_check2);
        ImageView bullet3 = (ImageView) view.findViewById(R.id.bullet_check3);
        ImageView bullet4 = (ImageView) view.findViewById(R.id.bullet_check4);

        if (savedInstanceState == null) {  //Avoid overlapping fragments.
            getFragmentManager().beginTransaction()
                    .add(R.id.journal_container, toDoList)
                    .commit();
        }
        /*overlay.setImageURI(imageFileLocationMap.get("overlay"));
        bullet1.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet2.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet3.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet4.setImageURI(imageFileLocationMap.get("bullet_check"));
        todo.setImageURI(imageFileLocationMap.get("arrow_right"));
        bio.setImageURI(imageFileLocationMap.get("arrow_right"));*/

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
    public Uri getGlyphUri() {
        return (imageFileLocationMap.get("journal_button_glyph"));
    }

    @Override
    public boolean checkClick(View view) {
        switch (view.getId()) {

            case R.id.tab1:
                getFragmentManager().beginTransaction()
                        .replace(R.id.journal_container, toDoList)
                        .commit();
                return true;
            case R.id.tab2:
                getFragmentManager().beginTransaction()
                        .replace(R.id.journal_container, toDoList2)
                        .commit();
                return true;
            case R.id.tab3:
                getFragmentManager().beginTransaction()
                        .replace(R.id.journal_container, toDoList3)
                        .commit();
                return true;
            case R.id.tab4:
                getFragmentManager().beginTransaction()
                        .replace(R.id.journal_container, toDoList4)
                        .commit();
                return true;
            default:
                return false;
        }
    }

    @Override
    public pivotOrientation getPivotOrientation(boolean enter) {
        return pivotOrientation.BOTTOM;
    }

    @Override
    protected int getAnimatorId(boolean enter) {
        if (enter) {
            gcMain.playSound(gcMain.sounds.metalClick);
            gcMain.hideFrame(true,true,true);
			return R.animator.rotate_in_from_right;
        }

        gcMain.showFrame(false,false,false);
		return R.animator.rotate_out_to_right;
    }

    public void createImageURIs(){
        final Uri rootUri = gcEngine.Access().root;
        imageFileLocationMap = new HashMap<String,Uri>(){{
            put("overlay", rootUri.buildUpon().appendPath("skins").appendPath("journal").appendPath("journal.png").build());
            put("bullet_check", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("bullet_check.png").build());
            put("arrow_right", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("arrow_right.png").build());
            put("journal_button_glyph", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_journal.png").build());
            put("test", rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("error_default.png").build());
        }};
    }

}
