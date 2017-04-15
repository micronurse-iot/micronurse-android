package org.micronurse.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.preference.Preference;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.Request;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.model.User;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                addPreferencesFromResource(R.xml.older_settings);
                break;
            case User.ACCOUNT_TYPE_GUARDIAN:
                addPreferencesFromResource(R.xml.guradian_settings);
                break;
        }

        findPreference(getResources().getString(R.string.action_logout)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final ProgressDialog pd = new ProgressDialog(SettingsActivity.this);
                        pd.setCancelable(false);
                        pd.setMessage(getString(R.string.action_waiting));
                        pd.show();
                        HttpApi.startRequest(new HttpApiJsonRequest(SettingsActivity.this, HttpApi.getApiUrl(HttpApi.AccountAPI.LOGOUT), Request.Method.DELETE, GlobalInfo.token, null,
                                new HttpApiJsonListener<Result>(Result.class) {
                                    @Override
                                    public void onDataResponse(Result data) {
                                        logout();
                                    }

                                    @Override
                                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                                        logout();
                                        return true;
                                    }

                                    @Override
                                    public boolean onDataCorrupted(Throwable e) {
                                        logout();
                                        return true;
                                    }

                                    @Override
                                    public boolean onNetworkError(Throwable e) {
                                        logout();
                                        return true;
                                    }
                                }
                        ));
                        return true;
                    }
                });
    }

    private void logout(){
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(LoginActivity.BUNDLE_PREFER_PHONE_NUMBER_KEY, GlobalInfo.user.getPhoneNumber());

        LoginUserRecord lur = DatabaseUtil.findLoginUserRecord(GlobalInfo.user.getUserId());
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
