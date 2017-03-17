package com.cityvibesgr.cityvibes.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PlaceOwnerActivity extends AppCompatActivity {
    private TextView textView, viewsToday, totalViews;
    SharedPreferences preferences;
    int placeID=-1;
    String placeType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_owner);

        textView = (TextView) findViewById(R.id.the_textview);
        preferences = getApplicationContext().getSharedPreferences("MyPreferences", 0);
        viewsToday = (TextView) findViewById(R.id.views_today);
        totalViews = (TextView) findViewById(R.id.total_views);

        getPlaceInfo();
    }

    public void uploadFiles(View v){
        Intent intent = new Intent(this, PlaceOwnerUploadOfficialFileActivity.class);
        if(placeID!=-1){
            intent.putExtra("place_id", placeID);
            intent.putExtra("place_type", placeType);
            startActivity(intent);
        }
    }

    public void getPlaceInfo(){
        String url = "http://www.cityvibes.gr/android/get_my_place/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject model = (JSONObject) response;
                        try {
                            placeID = model.getInt("id");
                            String placeName = model.getString("name");
                            textView.setText("Welcome back "+placeName+"!");
                            String placePhotoUrl = model.getString("profile_picture_link");
                            String placeLocation = model.getString("location");
                            String placeCity = model.getString("city");
                            final String clubFacebookUrl = model.getString("facebook_account_link");

                            placeType = model.getString("type");

                            double drinkPrice = 0;
                            double bottlePrice = 0;
                            double coffeePrice = 0;
                            double winePrice = 0;
                            double retsinaPrice = 0;
                            if (placeType.equals("Club")) {
                                drinkPrice = model.getDouble("drink_price");
                                bottlePrice = model.getDouble("bottle_price");
                            }else if (placeType.equals("CafeBar")) {
                                coffeePrice = model.getDouble("coffee_price");
                                drinkPrice = model.getDouble("drink_price");
                            }else if (placeType.equals("Tsipouradiko")) {
                                winePrice = model.getDouble("wine_price");
                                retsinaPrice = model.getDouble("retsina_price");
                            }

                            TextView nameTextView = (TextView) findViewById(R.id.club_info_name);
                            nameTextView.setText(placeName);

                            TextView detailsTextView = (TextView) findViewById(R.id.club_info_details);
                            if(placeType.equals("Club")){
                                detailsTextView.setText("Location: " + placeLocation + ", " + placeCity + "\nDrink Price: " + drinkPrice + " €\nBottle Price: " + bottlePrice + " €");
                            }else if(placeType.equals("CafeBar")){
                                detailsTextView.setText("Location: " + placeLocation + ", " + placeCity + "\nCoffee(Esspreso): " + coffeePrice + " €\nDrink Price: " + drinkPrice + " €");
                            }else if(placeType.equals("Tsipouradiko")){
                                detailsTextView.setText("Location: " + placeLocation + ", " + placeCity + "\nΡετσίνα: " + retsinaPrice + " €\nΚρασί Χύμα(λίτρο): " + winePrice + " €");
                            }

                            ImageButton instaLink = (ImageButton) findViewById(R.id.club_info_facebook_icon2);
                            Glide.with(getApplicationContext()).load(R.drawable.instapic).into(instaLink);

                            ImageButton fbLink = (ImageButton) findViewById(R.id.club_info_facebook_icon);
                            Glide.with(getApplicationContext()).load(R.drawable.fb3).into(fbLink);
                            fbLink.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse(clubFacebookUrl));
                                    startActivity(intent);
                                }

                            });

                            ImageView imageView = (ImageView) findViewById(R.id.club_info_photo);
                            Glide.with(getApplicationContext()).load(placePhotoUrl)
                                    .thumbnail(0.5f)
                                    .crossFade()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);

                            ImageView topImage = (ImageView) findViewById(R.id.backdrop);
                            Glide.with(getApplicationContext()).load(R.drawable.toolbar_detail_image).into(topImage);

                            viewsToday.setText("Views today: "+model.getInt("views_today"));
                            totalViews.setText("Total views: "+model.getInt("total_views"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(new Exception("getPlaceInfo() JSONException."));
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Error while getting your place. Please try again.",Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "Token "+ Keys.DJANGO_AUTHENTICATION_TOKEN);
                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }

    public void leaderboards(View v){
        Intent intent = new Intent(this, LeaderboardActivity.class);
        intent.putExtra("place_id", placeID);
        startActivity(intent);
    }


    public void logOut(View view) {
        Keys.DJANGO_AUTHENTICATION_TOKEN = "";
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("django_authentication_token");
        editor.remove("place_owner_username");
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}