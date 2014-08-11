package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Nathalie on 2014-08-05.
 */
public class Achievements extends ToolFragment{
    View view;
    ExpandableListView listView;
    BaseExpandableListAdapter adapter;
    List<Badge> badgeList = new ArrayList<Badge>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tool_achievements, null);
        listView = (ExpandableListView) view.findViewById(R.id.expandableListView);

        badgeList.add(new Badge("Badger",
                "Badger, badger, badger, mushroom! Mushroom! Badger, badges, badger....",
                Utils.resIdToUri(gcMain,R.drawable.icon_journal)));
        badgeList.add(new Badge("aThing",
                "Blah, this is a whole bunch of filler text to make the space larger cause we need to the relative layout as it's a wild child from the lands Ooo that's come in and smash up the place like whim-bam what the sam.",
                Utils.resIdToUri(gcMain, R.drawable.icon_imager)));
        badgeList.add(new Badge("EVERYTHING IS AWESOME!",
                "Everything is cool when you're part of a team! Everything is awesomeeeee, when we're living our dream!",
                Utils.resIdToUri(gcMain, R.drawable.icon_imager)));
        badgeList.add(new Badge("Magic Man",
                "You're a jerk from Mars, this requires a reward.",
                Utils.resIdToUri(gcMain,R.drawable.icon_journal)));
        badgeList.add(new Badge("Dark Sided",
                "Killed enough Ewoks to please the Emperor and everyone else.",
                Utils.resIdToUri(gcMain, R.drawable.icon_imager)));
        adapter = new AchievementsAdapter(badgeList, gcMain);

        listView.setAdapter(adapter);

        return view;
    }

    public static class AchievementsAdapter extends BaseExpandableListAdapter{
        List<Badge> badgeList;
        Context context;
        View badgeView;
        View badgeDescriptionView;
        TextView badgeName;
        TextView badgeBlurb;
        ImageView badgeIcon;

        public AchievementsAdapter(List<Badge> badgesList, Context ctxt){
            badgeList = badgesList;
            context = ctxt;
        }

        @Override
        public int getGroupCount() {
            return badgeList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return badgeList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return badgeList.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            Badge currentBadge = badgeList.get(groupPosition);
            LayoutInflater inflater = (LayoutInflater)(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

            badgeView = inflater.inflate(R.layout.badge_list_item, null);

            badgeName = (TextView) badgeView.findViewById(R.id.badge_name);
            badgeName.setText(currentBadge.name);

            badgeIcon = (ImageView) badgeView.findViewById(R.id.badge_icon);
            badgeIcon.setImageURI(currentBadge.badgeImage);

            return badgeView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Badge currentBadge = badgeList.get(groupPosition);
            LayoutInflater inflater = (LayoutInflater)(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

            badgeDescriptionView = inflater.inflate(R.layout.description_view, null);

            badgeName = (TextView) badgeDescriptionView.findViewById(R.id.title);
            badgeName.setText(currentBadge.name);

            badgeBlurb = (TextView) badgeDescriptionView.findViewById(R.id.description);
            badgeBlurb.setText(currentBadge.description);

            badgeIcon = (ImageView) badgeDescriptionView.findViewById(R.id.badge_image_large);
            badgeIcon.setImageURI(currentBadge.badgeImage);

            return badgeDescriptionView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    public class Badge{
        String name, description;
        Uri badgeImage;

        public Badge(String badgeName, String badgeDescription, Uri image){
            name = badgeName;
            description = badgeDescription;
            badgeImage = image;

        }
    }
}
