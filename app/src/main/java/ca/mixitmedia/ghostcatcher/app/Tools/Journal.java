package ca.mixitmedia.ghostcatcher.app.Tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;

import ca.mixitmedia.ghostcatcher.app.R;

public class Journal extends ToolFragment {

    static String[] items = {"lorem", "ipsum", "dolor", "sit", "amet",
            "consectetuer", "adipiscing", "elit", "morbi", "vel", "ligula",
            "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat",
            "placerat", "ante", "porttitor", "sodales", "pellentesque",
            "augue", "purus"};
    AdapterViewFlipper flipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tool_journal, container, false);
        view.setPivotX(0);
        view.setPivotY(view.getMeasuredHeight());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //flipper = (AdapterViewFlipper) getView().findViewById(R.id.NotesFlipper);
        //flipper.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.flip_tester, items));
        //flipper.setFlipInterval(2000);
        //flipper.startFlipping();
    }

    public boolean checkClick(View view) {
        if (view.getId() == R.id.journal_gear_btn) return true;
        return false;
    }

    public static Journal newInstance(String settings) {
        Journal fragment = new Journal();
        Bundle args = new Bundle();
        args.putString("settings", settings);
        fragment.setArguments(args);
        return fragment;
    }
}
