package ca.mixitmedia.ghostcatcher.app.Tools;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.File;
import java.util.List;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.experience.gcAudio;
import ca.mixitmedia.ghostcatcher.experience.gcEngine;

public class Tester extends ToolFragment {

    Uri rootUri = gcEngine.Access().root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tool_tester, null);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public Uri getGlyphUri() {
        return (rootUri.buildUpon().appendPath("skins").appendPath("components").appendPath("icon_ghost_catcher.png").build());
    }

}
