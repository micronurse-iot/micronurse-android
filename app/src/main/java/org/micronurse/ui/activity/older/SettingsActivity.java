package org.micronurse.ui.activity.older;

import android.content.Intent;
import android.preference.Preference;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.Result;
import org.micronurse.service.EmergencyCallService;
import org.micronurse.service.LocationService;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.AppCompatPreferenceActivity;
import org.micronurse.ui.activity.LoginActivity;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.older_settings);
        findPreference(getResources().getString(R.string.action_logout)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new MicronurseAPI<Result>(SettingsActivity.this, MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.LOGOUT), Request.Method.DELETE,
                                null, GlobalInfo.token, new Response.Listener<Result>() {
                            @Override
                            public void onResponse(Result response) {
                                logout();
                            }
                        }, new APIErrorListener() {
                            @Override
                            public boolean onErrorResponse(VolleyError err, Result result) {
                                logout();
                                return true;
                            }
                        }, Result.class).startRequest();
                        return true;
                    }
                });
    }

    private void logout(){
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.user.getPhoneNumber());

        LoginUserRecord lur = DatabaseUtil.findLoginUserRecord(GlobalInfo.user.getPhoneNumber());
        if(lur != null) {
            lur.setToken(null);
            lur.save();
        }
        GlobalInfo.exitLoginStatus(this);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
