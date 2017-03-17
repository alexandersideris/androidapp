package com.cityvibesgr.cityvibes.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.LeaderboardAdapter;
import com.cityvibesgr.cityvibes.bo.Place;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.firebase.crash.FirebaseCrash;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    private TextView textView, title, textView2;
    private ArrayList<Place> places = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter placeAdapter;
    private LinearLayoutManager mLayoutManager;
    private int placeID;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("City Vibes");

        textView = (TextView) findViewById(R.id.textview);
        textView2 = (TextView) findViewById(R.id.textview2);
        title = (TextView) findViewById(R.id.title);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        placeID = getIntent().getExtras().getInt("place_id");

        getPrize();
        getLeaderboard();
    }

    private void getLeaderboard() {
        String url = "http://www.cityvibes.gr/android/get_leaderboard/"+placeID;

        StringRequest jsonRequest = null;
        jsonRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if(jsonArray.length()==0){
                                textView2.setVisibility(View.VISIBLE);
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject placeModel = (JSONObject) jsonArray.get(i);
                                    Place place = new Place();
                                    place.setID(placeModel.getInt("id"));
                                    place.setName(placeModel.getString("name"));
                                    place.setFileUploads(placeModel.getInt("file_uploads"));
                                    places.add(place);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    FirebaseCrash.report(new Exception("getLeaderboard() JSONException."));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(new Exception("getLeaderboard() JSONException."));
                        }
                        mLayoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(mLayoutManager);
                        placeAdapter = new LeaderboardAdapter(LeaderboardActivity.this.getApplicationContext(), places);
                        recyclerView.setAdapter(placeAdapter);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error while leaderboard places. "+error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }

    private void getPrize() {
        String url = "http://www.cityvibes.gr/android/get_prize_text/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] s = response.replace('"',' ').split("/");
                        title.setText(s[0]);
                        textView.setText(s[1]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error while getting leaderboard text. "+error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

}
