package com.cityvibesgr.cityvibes.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.DetailOfficialVerticalAdapter;
import com.cityvibesgr.cityvibes.bo.MyFile;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.firebase.crash.FirebaseCrash;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class DetailOfficialFragment extends Fragment {

    ArrayList<MyFile> files = new ArrayList<MyFile>();
    private RecyclerView.Adapter verticalAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView myRecyclerView;
    private ArrayList<String> photosessionDetails = new ArrayList<String>();
    private View v;
    TextView textView1;

    public DetailOfficialFragment() {
        //one comment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_official, container, false);
        final int placeID = getActivity().getIntent().getExtras().getInt("place_id");
        final String placeName = getActivity().getIntent().getExtras().getString("place_name");
        final String placeType = getActivity().getIntent().getExtras().getString("place_type");

        TextView textView = (TextView) v.findViewById(R.id.the_textview);
        textView.setText("These photos were taken by the Official Team of " + placeName + ".");

        textView1 = (TextView) v.findViewById(R.id.textview);

        getPlaceFiles(placeID, placeName, placeType);
        return v;
    }


    public void getPlaceFiles(int placeID, String placeName, String placeType){
        String url ="http://www.cityvibes.gr/android/official_files/"+placeID;
        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()==0){
                            textView1.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                MyFile file = new MyFile();
                                file.setApproximateTime(((JSONObject) response.get(i)).getString("approximate_time"));
                                file.setActualTime(((JSONObject) response.get(i)).getString("actual_time"));
                                file.setUrl(((JSONObject) response.get(i)).getString("file_link"));
                                file.setId(((JSONObject) response.get(i)).getInt("id"));
                                file.setClubName(placeName);
                                file.setDay(((JSONObject) response.get(i)).getString("day"));
                                if (((JSONObject) response.get(i)).getString("type").equals("image/jpeg")) {
                                    file.setType("image/jpeg");
                                } else {
                                    file.setType("video/mp4");
                                    file.setThumbnailUrl(((JSONObject) response.get(i)).getString("thumbnail_link"));
                                }
                                file.setDay(((JSONObject) response.get(i)).getString("day"));

                                if(placeType.equals("Club") || placeType.equals("CafeBar")){
                                    file.setMusicGenre(((JSONObject) response.get(i)).getString("music_genre"));
                                }

                                files.add(file);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(new Exception("getPlaceFiles() JSONException."));
                            }
                        }

                        Collections.reverse(files);
                        myRecyclerView = (RecyclerView) v.findViewById(R.id.vertical_list);
                        mLayoutManager = new LinearLayoutManager(DetailOfficialFragment.this.getContext());
                        myRecyclerView.setLayoutManager(mLayoutManager);
                        verticalAdapter = new DetailOfficialVerticalAdapter(DetailOfficialFragment.this.getContext(), files, photosessionDetails);
                        myRecyclerView.setAdapter(verticalAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        jsonRequest.setShouldCache(false);
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonRequest);
    }

}
