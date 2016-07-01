package org.micronurse.http;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;

import org.micronurse.R;
import org.micronurse.http.model.result.Result;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class MicronurseAPI {
    private static final String BASE_URL = "http://101.200.144.204:13000/micronurse";
    private static RequestQueue requestQueue = null;
    private Request<Result> request;

    public MicronurseAPI(final Context context, String apiURL, int method, Object requestData, String token, Response.Listener<Result> listener,
                         final APIErrorListener errorListener, Class<? extends Result> resultType){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        request = new JSONRequest(BASE_URL + apiURL, method, token, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if(error.getCause() instanceof JsonSyntaxException) {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    //errorListener.onErrorResponse(error, null);
                }
                else if(error.networkResponse == null) {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    errorListener.onErrorResponse(error, null);
                }else{
                    if(error.networkResponse.statusCode == 500)
                        Toast.makeText(context, R.string.server_internal_error, Toast.LENGTH_SHORT).show();
                    try {
                        Result result = GsonUtil.getGson().fromJson(new String(error.networkResponse.data), Result.class);
                        errorListener.onErrorResponse(error, result);
                    }catch (JsonSyntaxException jse){
                        errorListener.onErrorResponse(error, new Result(-1, context.getString(R.string.unknown_error)));
                    }
                }
            }
        }, requestData, resultType);
    }

    public void startRequest(){
        requestQueue.add(request);
    }

    public void cancelRequest(){
        request.cancel();
    }
}
