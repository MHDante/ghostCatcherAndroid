package ca.mixitmedia.ghostcatcher.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;

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
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        view.setPivotX(0f);
        view.setPivotY(1f);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        flipper = (AdapterViewFlipper) getView().findViewById(R.id.NotesFlipper);
        flipper.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.flip_tester, items));
        flipper.setFlipInterval(2000);
        flipper.startFlipping();
    }

    public boolean checkClick(View view) {
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
