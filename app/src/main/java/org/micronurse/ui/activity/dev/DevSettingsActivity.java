package org.micronurse.ui.activity.dev;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.micronurse.R;
import org.micronurse.ui.activity.AppCompatPreferenceActivity;
import org.micronurse.util.SharedPreferenceUtil;

@SuppressWarnings("deprecation")
public class DevSettingsActivity extends AppCompatPreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferenceUtil.PREFERENCE_DEV);
        addPreferencesFromResource(R.xml.dev_settings);
    }
}
