package com.example.svenu.guldendata;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by svenu on 22-2-2018.
 */

public class JsonGetter {

    private final String TAG = "JsonGetter";

    private JsonGetter jsonGetter = this;
    private DataResponse dataResponse;

    public void getJson(final Context context, String url, final DataResponse dataResponse) {
        this.dataResponse = dataResponse;
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                jsonGetter.dataResponse.onJsonResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonObjectRequest);
    }

    public interface DataResponse {
        void onJsonResponse(JSONObject response);
    }
}
