package org.micronurse.ui.activity.older;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.PostFriendMomentRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.util.GlobalInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostFriendMomentActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_POST_MOMENT = 669;
    public static final int RESULT_CODE_POST_MOMENT_SUCCESSFULLY = 233;

    @BindView(R.id.edit_moment)
    EditText editMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_friend_moment);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void postMoment(){
        if(TextUtils.isEmpty(editMoment.getText()))
            return;
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.action_posting_moment));
        pd.show();

        PostFriendMomentRequest req = new PostFriendMomentRequest(editMoment.getText().toString());
        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.OlderFriendJuanAPI.POST_FRIEND_MOMENT), Request.Method.POST,
                GlobalInfo.token, req, new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onResponse() {
                pd.dismiss();
            }

            @Override
            public void onDataResponse(Result data) {
                Toast.makeText(PostFriendMomentActivity.this, R.string.post_moment_successfully, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CODE_POST_MOMENT_SUCCESSFULLY);
                finish();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post_moment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }else if(id == R.id.menu_done){
            postMoment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
