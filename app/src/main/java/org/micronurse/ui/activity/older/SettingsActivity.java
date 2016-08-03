package org.micronurse.ui.activity.older;

import android.content.Intent;
import android.preference.Preference;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.Result;
import org.micronurse.ui.activity.AppCompatPreferenceActivity;
import org.micronurse.ui.activity.LoginActivity;
import org.micronurse.util.GlobalInfo;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.older_settings);
        findPreference(getResources().getString(R.string.action_logout)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new MicronurseAPI(SettingsActivity.this, "/account/logout", Request.Method.DELETE,
                                null, GlobalInfo.token, new Response.Listener<Result>() {
                            @Override
                            public void onResponse(Result response) {
                                logout();
                            }
                        }, new APIErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError err, Result result) {
                                logout();
                            }
                        }, Result.class).startRequest();
                        return true;
                    }
                });
    }

    private void logout(){
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if(GlobalInfo.loginRecord != null) {
            intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.loginRecord.getPhoneNumber());
            GlobalInfo.loginRecord.setToken(null);
            GlobalInfo.loginRecord.save();
        }
        GlobalInfo.clearLoginUserInfo();

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