package org.micronurse.ui.activity.older;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.request.PostFriendMomentRequest;
import org.micronurse.http.model.result.Result;
import org.micronurse.util.GlobalInfo;

public class PostFriendMomentActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_POST_MOMENT = 669;
    public static final int RESULT_CODE_POST_MOMENT_SUCCESSFULLY = 233;

    private EditText editMoment;
    private FloatingActionButton btnPostMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_friend_moment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editMoment = (EditText) findViewById(R.id.edit_moment);
        btnPostMoment = (FloatingActionButton) findViewById(R.id.btn_post_moment);
        btnPostMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMoment();
            }
        });
    }

    private void postMoment(){
        if(TextUtils.isEmpty(editMoment.getText()))
            return;
        PostFriendMomentRequest req = new PostFriendMomentRequest(editMoment.getText().toString());
        new MicronurseAPI<Result>(this, MicronurseAPI.getApiUrl(MicronurseAPI.OlderFriendJuanAPI.POST_FRIEND_MOMENT), Request.Method.POST,
                req, GlobalInfo.token, new Response.Listener<Result>() {
            @Override
            public void onResponse(Result response) {
                Toast.makeText(PostFriendMomentActivity.this, R.string.post_moment_successfully, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CODE_POST_MOMENT_SUCCESSFULLY);
                finish();
            }
        }, new APIErrorListener() {
            @Override
            public boolean onErrorResponse(VolleyError err, Result result) {
                return false;
            }
        }, Result.class, true, getString(R.string.action_posting_moment)).startRequest();
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
