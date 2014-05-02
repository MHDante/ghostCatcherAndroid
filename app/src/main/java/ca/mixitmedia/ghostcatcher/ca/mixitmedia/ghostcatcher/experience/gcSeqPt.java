package ca.mixitmedia.ghostcatcher.ca.mixitmedia.ghostcatcher.experience;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 07/03/14.
 */
public class gcSeqPt {
    int id;
    String name;
    String[] mysteries;
    String[] infos;
    String[] infosPersistent;
    //gcTrigger[] Trigger;

    public List<gcLocation> locations;

    //DUMMY CONSTRUCTOR TO POPULATE
    //public gcSeqPt (){
    //    id = 0 + (int)(Math.random() * ((3-0) + 1));
    //    locations.add ()
    //
    //}

    public gcSeqPt(){
        locations = new ArrayList<gcLocation>();
        locations.add ( new gcLocation(1, "Lake Devo", 43.657527, -79.379790, "") );
        locations.add ( new gcLocation(2, "Theatre School", 43.659768, -79.379752, "the home of the acting, dance, and technical production programs for the Faculty of Communication & Design") );
        locations.add ( new gcLocation(3, "Oakham House", 43.658008, -79.378026, "The house is located at the southwest corner of Gould and Church streets. Gothic.") );
        locations.add ( new gcLocation(4, "International Living/Learning Centre", 43.658586, -79.376090, "an 11-storey, former hotel built in 1987, can accommodate 252 residence students in its extra-large rooms.") );
    }

}
