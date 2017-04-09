package com.cityvibesgr.cityvibes.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;


import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private int id;
    private String theUrl, type;
    private EditText editText;
    private ProgressDialog progressDialog;
    private Context context = this;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("City Vibes");

        id = getIntent().getExtras().getInt("id");
        theUrl = getIntent().getExtras().getString("url");
        type = getIntent().getExtras().getString("type");
        progressDialog = new ProgressDialog(this);
        editText = (EditText) findViewById(R.id.editext);
        if(!Keys.DJANGO_AUTHENTICATION_TOKEN.equals("")){
            username = "place owner "+Keys.PLACEOWNER_USERNAME;
        }else if(!Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN.equals("")){
            username = "instagram user "+Keys.INSTAGRAM_USERNAME;
        }else{
            finish();
        }


    }

    public void send(View view) {
        String text = editText.getText().toString();
        String url = "http://www.cityvibes.gr/android/send_report/";
        progressDialog.setTitle("Sending report");
        progressDialog.setMessage("Sending your report, this will only take a few seconds. Thank you for helping City Vibes become better!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest jsonRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ReportActivity.this, response, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error sending report. Please try again.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("id", ""+id);
                params.put("url", ""+theUrl);
                params.put("type", ""+type);
                params.put("text", ""+text);
                params.put("username", ""+username);
                return params;
            }

        };
        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }
}
