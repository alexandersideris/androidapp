package com.cityvibesgr.cityvibes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.google.firebase.analytics.FirebaseAnalytics;


public class WebViewActivity extends AppCompatActivity {
    private Context context = this;
    /* FireBase analytics */
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        setContentView(webview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("com.sideware.cityvibes.gourbasi.token.instagram")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN = url.split("=")[1];
                    Keys.INSTAGRAM_USERNAME = url.split("=")[2];
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("instagram_authentication_token", Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN);
                    editor.putString("instagram_username", Keys.INSTAGRAM_USERNAME);
                    editor.apply();
                    Bundle params = new Bundle();
                    mFirebaseAnalytics.logEvent("instagram_sign_ups",params);
                    Toast.makeText(context, "Thank you for signing up with Instagram. You can now upload photos freely!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        });
        webview.loadUrl("https://api.instagram.com/oauth/authorize/?client_id="+ Keys.INSTAGRAM_CLIENT_ID+"&redirect_uri="+Keys.INSTAGRAM_REDIRECT_URI+"&response_type=code");
    }

}
