package ca.mixitmedia.ghostcatcher.app.Tools;

import android.net.Uri;
import android.app.ActionBar;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Journal extends ToolFragment {

    Map<String, Uri> imageFileLocationMap;

    ViewSwitcher viewSwitcher;
    ImageButton bio;
    ImageButton todo;

    public Journal(){createImageURIs();};

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

        RelativeLayout.LayoutParams fragmentContainerParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
        fragmentContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        fragmentContainerParams.addRule(RelativeLayout.ABOVE, 0);

        container.setLayoutParams(fragmentContainerParams);
        container.invalidate();

        ImageView overlay = (ImageView) view.findViewById(R.id.overlay);
        ImageView bullet1 = (ImageView) view.findViewById(R.id.bullet_check1);
        ImageView bullet2 = (ImageView) view.findViewById(R.id.bullet_check2);
        ImageView bullet3 = (ImageView) view.findViewById(R.id.bullet_check3);
        ImageView bullet4 = (ImageView) view.findViewById(R.id.bullet_check4);


        overlay.setImageURI(imageFileLocationMap.get("overlay"));
        bullet1.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet2.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet3.setImageURI(imageFileLocationMap.get("bullet_check"));
        bullet4.setImageURI(imageFileLocationMap.get("bullet_check"));
        todo.setImageURI(imageFileLocationMap.get("arrow_right"));
        bio.setImageURI(imageFileLocationMap.get("arrow_right"));

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

    public boolean checkClick(View view) {
        return false;
    }

    @Override
    public pivotOrientation getPivotOrientation(boolean enter) {
        return pivotOrientation.BOTTOM;
    }

    @Override
    protected int getAnimatorId(boolean enter) {
        if(enter){
            gcMain.playSound(gcMain.sounds.metalClick);
            gcMain.hideFrame(true,true,true);
        }
        else
            gcMain.showFrame(false,false,false);

        return (enter) ? R.animator.rotate_in_from_right : R.animator.rotate_out_to_right;
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
