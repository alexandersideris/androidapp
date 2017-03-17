package com.cityvibesgr.cityvibes.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.DetailSocialVerticalAdapter;
import com.cityvibesgr.cityvibes.bo.SocialFile;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.firebase.crash.FirebaseCrash;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class DetailSocialFragment extends Fragment {

    ArrayList<SocialFile> files = new ArrayList<>();
    private RecyclerView.Adapter verticalAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView myRecyclerView;
    private View v;
    RelativeLayout layout;
    RelativeLayout layout2;
    TextView textView1;

    public DetailSocialFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_social, container, false);

        ImageView imageView = (ImageView) v.findViewById(R.id.instagram_icon);
        Glide.with(this).load(R.drawable.insta).into(imageView);

        ImageView imageView2 = (ImageView) v.findViewById(R.id.instagram_icon2);
        Glide.with(this).load(R.drawable.instagram512).into(imageView2);

        textView1 = (TextView) v.findViewById(R.id.no_file_textview);

        if(!Keys.HAS_BEEN_CLOSED){
            if(Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN.equals("")){
                layout = (RelativeLayout) v.findViewById(R.id.parent_layout);
                layout.setVisibility(View.VISIBLE);
            }else{
                layout2 = (RelativeLayout) v.findViewById(R.id.parent_layout2);
                layout2.setVisibility(View.VISIBLE);
            }
        }

        final int placeID = getActivity().getIntent().getExtras().getInt("place_id");
        final String placeName = getActivity().getIntent().getExtras().getString("place_name");

        getSocialFiles(placeID, placeName);
        return v;
    }

    public void getSocialFiles(int placeID, String placeName){
        String url ="http://www.cityvibes.gr/android/social_files/"+placeID;
        final JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()==0){
                            textView1.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                SocialFile file = new SocialFile();
                                file.setId(((JSONObject) response.get(i)).getInt("id"));
                                file.setActualTime(((JSONObject) response.get(i)).getString("actual_time"));
                                file.setApproximateTime(((JSONObject) response.get(i)).getString("approximate_time"));
                                file.setUrl(((JSONObject) response.get(i)).getString("file_link"));
                                file.setPlaceName(placeName);
                                file.setDay(((JSONObject) response.get(i)).getString("day"));
                                file.setInstagramUsername(((JSONObject) response.get(i)).getJSONObject("instagram_user").getString("instagram_username"));
                                file.setInstagramFullName(((JSONObject) response.get(i)).getJSONObject("instagram_user").getString("instagram_full_name"));
                                file.setInstagramProfilePictureLink(((JSONObject) response.get(i)).getJSONObject("instagram_user").getString("instagram_profile_picture"));
                                file.setInstagramProfileLink(((JSONObject) response.get(i)).getJSONObject("instagram_user").getString("instagram_profile_link"));
                                files.add(file);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(new Exception("getSocialFiles() JSONException."));
                            }
                        }

                        Collections.reverse(files);
                        myRecyclerView = (RecyclerView) v.findViewById(R.id.social_photo_list);
                        mLayoutManager = new LinearLayoutManager(DetailSocialFragment.this.getContext());
                        myRecyclerView.setLayoutManager(mLayoutManager);
                        verticalAdapter = new DetailSocialVerticalAdapter(DetailSocialFragment.this.getContext(), files);
                        myRecyclerView.setAdapter(verticalAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonRequest);
    }

    public void closeFragment(){
        if(Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN.equals("")){
            layout.setVisibility(View.GONE);
            Keys.HAS_BEEN_CLOSED = true;
        }else{
            layout2.setVisibility(View.GONE);
            Keys.HAS_BEEN_CLOSED = true;
        }
    }


}
