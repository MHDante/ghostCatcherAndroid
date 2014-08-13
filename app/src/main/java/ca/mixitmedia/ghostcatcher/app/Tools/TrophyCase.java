package ca.mixitmedia.ghostcatcher.app.Tools;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Nathalie on 2014-08-12.
 */
public class TrophyCase extends ToolFragment{
    View view;
    GridView gridView;
    ArrayList<Trophy> trophyList = new ArrayList<Trophy>();
    TrophyAdapter gridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tool_trophy_case, null);

        if(trophyList.isEmpty()){
            trophyList.add(new Trophy("Stare Horse","Caught: 18-10-10", Utils.resIdToUri(gcMain, R.drawable.icon_imager)));
            trophyList.add(new Trophy("Star Lord","Caught: 08-08-14",Utils.resIdToUri(gcMain,R.drawable.icon_ghost_catcher)));
            trophyList.add(new Trophy("That One Guy", "Caught: ??",Utils.resIdToUri(gcMain,R.drawable.icon_biocalibrate)));
        }

        gridView = (GridView) view.findViewById(R.id.trophy_grid_view);
        gridAdapter = new TrophyAdapter(trophyList, gcMain);
        gridView.setAdapter(gridAdapter);
        return view;
    }

    public static class TrophyAdapter extends BaseAdapter{
        Context context;
        List<Trophy> trophyList;
        View trophyView;
        Trophy currentTrophy;
        ImageView trophyIcon;
        TextView trophyName;

        public TrophyAdapter(List<Trophy> trophiesList, Context ctxt){
            trophyList = trophiesList;
            context = ctxt;

        }

        @Override
        public int getCount() {
            return trophyList.size();
        }

        @Override
        public Object getItem(int position) {
            return trophyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            trophyView = inflater.inflate(R.layout.trophy_grid_item,null);
            currentTrophy = trophyList.get(position);

            trophyName = (TextView) trophyView.findViewById(R.id.trophy_name);
            trophyName.setText(currentTrophy.name);

            trophyIcon = (ImageView) trophyView.findViewById(R.id.trophy_icon);
            trophyIcon.setImageURI(currentTrophy.trophyImage);

            return trophyView;
        }
    }

    public class Trophy{
        String name, data;
        Uri trophyImage;

        public Trophy(String trophyName, String trophyData, Uri trophyImageUri){
            name = trophyName;
            data = trophyData;
            trophyImage = trophyImageUri;
        }

    }
}
