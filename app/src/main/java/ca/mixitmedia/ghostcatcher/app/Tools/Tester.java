package ca.mixitmedia.ghostcatcher.app.Tools;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.mixitmedia.ghostcatcher.app.R;

public class Tester extends ToolFragment {

    //Uri rootUri = gcEngine.Access().root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tool_tester, null);

        return v;
    }
}
