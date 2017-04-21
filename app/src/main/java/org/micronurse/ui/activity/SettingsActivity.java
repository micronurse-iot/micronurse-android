package org.micronurse.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

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

        final AlertDialog confirmDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        findPreference(getString(R.string.action_logout_iot))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        confirmDialog.setMessage(getString(R.string.alert_query_logout));
                        confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HttpApi.startRequest(new HttpApiJsonRequest(SettingsActivity.this, HttpApi.getApiUrl(HttpApi.AccountAPI.LOGOUT_IOT), Request.Method.DELETE, GlobalInfo.token,
                                        null, new HttpApiJsonListener<Result>(Result.class) {
                                    @Override
                                    public void onDataResponse(Result data) {
                                        Toast.makeText(SettingsActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }));
                            }
                        });
                        confirmDialog.show();
                        return true;
                    }
                });

        findPreference(getString(R.string.action_logout))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        confirmDialog.setMessage(getString(R.string.alert_query_logout));
                        confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                            }
                        });
                        confirmDialog.show();
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
