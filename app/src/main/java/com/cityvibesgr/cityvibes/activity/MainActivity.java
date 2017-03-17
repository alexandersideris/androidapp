package com.cityvibesgr.cityvibes.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button feelTheVibesButton;
    private TextView textView, signInTextView, myProfileTextView;
    private ImageView imageView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        feelTheVibesButton = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.main_textview);
        imageView = (ImageView) findViewById(R.id.logo);
        signInTextView = (TextView) findViewById(R.id.sign_in_textview);
        myProfileTextView = (TextView) findViewById(R.id.my_profile_textView);

        preferences = getApplicationContext().getSharedPreferences("MyPreferences", 0);

        String s = preferences.getString("django_authentication_token", "");
        if(!s.equals("")){
            Keys.DJANGO_AUTHENTICATION_TOKEN = s;
            signInTextView.setVisibility(View.GONE);
            myProfileTextView.setVisibility(View.VISIBLE);
        }

        String str = preferences.getString("instagram_authentication_token", "");
        if(!str.equals("")){
            Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN = str;
        }

        String string = preferences.getString("instagram_username", "");
        if(!string.equals("")){
            Keys.INSTAGRAM_USERNAME = string;
        }

        String string2 = preferences.getString("place_owner_username", "");
        if(!string2.equals("")){
            Keys.PLACEOWNER_USERNAME = string2;
        }

        initBackgroundVideo();


    }

    private void initBackgroundVideo(){
        VideoView background = (VideoView) findViewById(R.id.background_video);
        String path = "android.resource://com.cityvibesgr.cityvibes/"+R.raw.bright_cityvibes;
        Uri uri = Uri.parse(path);
        background.setVideoURI(uri);
        background.setOnPreparedListener(mp -> {
            mp.setLooping(true);
        });
        background.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        initBackgroundVideo();

    }

    public void feelTheVibes(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void signIn(View v){
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.sign_in);
        layout.setVisibility(View.VISIBLE);

        feelTheVibesButton.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        signInTextView.setVisibility(View.INVISIBLE);
    }

    public void myProfile(View v){
        Intent intent = new Intent(getApplicationContext(), PlaceOwnerActivity.class);
        startActivity(intent);
    }

    public void authenticate(View v){
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.sign_in);
        TextView usernameTextView = (TextView) layout.findViewById(R.id.username);
        TextView passwordTextView = (TextView) layout.findViewById(R.id.password);
        final String username = usernameTextView.getText().toString();
        final String password = passwordTextView.getText().toString();

        String url = "http://www.cityvibes.gr/android/token/";

        Map<String, String>  params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing in...");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        JsonObjectRequest postRequest = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(getApplicationContext(), "Response: " + response.toString(), Toast.LENGTH_LONG).show();
                        try {
                            String s = (String) response.get("token");
                            Keys.DJANGO_AUTHENTICATION_TOKEN = s;
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("django_authentication_token", s);
                            editor.putString("place_owner_username", username);
                            editor.apply();
                            Intent intent = new Intent(MainActivity.this.getApplicationContext(), PlaceOwnerActivity.class);

                            signInTextView.setVisibility(View.GONE);
                            layout.setVisibility(View.GONE);
                            feelTheVibesButton.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            myProfileTextView.setVisibility(View.VISIBLE);

                            MainActivity.this.startActivity(intent);

                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            FirebaseCrash.report(new Exception("authenticate() JSONException."));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "Error while signing in. Please try again.", Toast.LENGTH_LONG).show();
                TextView p = (TextView) MainActivity.this.findViewById(R.id.password);
                p.setText("");
                TextView err = (TextView) MainActivity.this.findViewById(R.id.err);
                err.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
            }
        });
        postRequest.setShouldCache(false);
        MySingleton.getInstance(this).addToRequestQueue(postRequest);

    }

    public void cancelLogin(View v){
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.sign_in);
        TextView u = (TextView) layout.findViewById(R.id.username);
        u.setText("");
        TextView p = (TextView) layout.findViewById(R.id.password);
        p.setText("");
        layout.setVisibility(View.INVISIBLE);
        TextView err = (TextView) layout.findViewById(R.id.err);
        err.setVisibility(View.INVISIBLE);

        feelTheVibesButton.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        signInTextView.setVisibility(View.VISIBLE);
    }

}
