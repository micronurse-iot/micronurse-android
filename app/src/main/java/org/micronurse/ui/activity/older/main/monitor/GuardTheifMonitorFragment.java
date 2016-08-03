package org.micronurse.ui.activity.older.main.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.R;

public class GuardTheifMonitorFragment extends Fragment {

    public GuardTheifMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_older_guardtheif_monitor, container, false);
    }
}
