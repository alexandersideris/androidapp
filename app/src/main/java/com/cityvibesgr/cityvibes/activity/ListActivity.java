package com.cityvibesgr.cityvibes.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.ListAdapter;
import com.cityvibesgr.cityvibes.bo.Place;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener, com.google.android.gms.location.LocationListener {

    ArrayList<Place> places = new ArrayList<>();
    private Context context = this;

    private RecyclerView clubRecyclerView;
    private RecyclerView.Adapter placeAdapter, clubAdapter, cafebarAdapter, tsipouradikoAdapter;
    private ArrayList<Place> clubs = new ArrayList<>();
    private ArrayList<Place> cafebars = new ArrayList<>();
    private ArrayList<Place> tsipouradika = new ArrayList<>();
    private StaggeredGridLayoutManager mLayoutManager;

    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest;
    private int REQUEST_CHECK_SETTINGS = 5;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initCollapsingToolbar();

        clubRecyclerView = (RecyclerView) findViewById(R.id.list);
        textView = (TextView) findViewById(R.id.textview);

        ImageView topImage = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(R.drawable.toolbar_list_image).into(topImage);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.types_array, R.layout.spinner_text);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        isGooglePlayServicesAvailable(this);
        setUpGoogleClient();

    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void setUpGoogleClient(){
        /* Google Client */
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        /* This is to check if location settings are enabled */
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates= result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    ListActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            FirebaseCrash.report(new Exception("onResult() IntentSender.SendIntentException."));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void requestPlaces(double longitude, double latitude) {
        String url = "http://www.cityvibes.gr/android/places/";

        StringRequest jsonRequest = null;
        if(places.size()!=0){
            return;
        }
        if(Keys.INSTAGRAM_USERNAME.equals(Keys.ROOT)){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                if(jsonArray.length()==0){
                                    textView.setVisibility(View.VISIBLE);
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        JSONObject placeModel = (JSONObject) jsonArray.get(i);
                                        Place place = new Place();
                                        place.setID(placeModel.getInt("id"));
                                        place.setName(placeModel.getString("name"));
                                        place.setProfilePhotoUrl(placeModel.getString("profile_picture_link"));
                                        place.setLastUpdate(placeModel.getString("string_last_update"));
                                        place.setType(placeModel.getString("type"));

                                        places.add(place);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        FirebaseCrash.report(new Exception("requestPlaces() JSONException."));

                                    }
                                    textView.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(new Exception("requestPlaces() JSONException."));
                            }
                            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                            clubRecyclerView.setLayoutManager(mLayoutManager);
                            placeAdapter = new ListAdapter(ListActivity.this.getApplicationContext(), places);
                            clubRecyclerView.setAdapter(placeAdapter);
                            createAdapters();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Error while getting places. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }else{
            jsonRequest = new StringRequest
                    (Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                if(jsonArray.length()==0){
                                    textView.setVisibility(View.VISIBLE);
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        JSONObject placeModel = (JSONObject) jsonArray.get(i);
                                        Place place = new Place();
                                        place.setID(placeModel.getInt("id"));
                                        place.setName(placeModel.getString("name"));
                                        place.setProfilePhotoUrl(placeModel.getString("profile_picture_link"));
                                        place.setLastUpdate(placeModel.getString("string_last_update"));
                                        place.setType(placeModel.getString("type"));

                                        places.add(place);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        FirebaseCrash.report(new Exception("requestPlaces() JSONException."));
                                    }
                                    textView.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(new Exception("requestPlaces() JSONException."));
                            }
                            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                            clubRecyclerView.setLayoutManager(mLayoutManager);
                            placeAdapter = new ListAdapter(ListActivity.this.getApplicationContext(), places);
                            clubRecyclerView.setAdapter(placeAdapter);
                            createAdapters();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "Error while getting places. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("longitude", ""+longitude);
                    params.put("latitude", ""+latitude);
                    return params;
                }

            };
            //jsonRequest.setShouldCache(false);
            MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        }


    }

    private void createAdapters() {
        for(int i=0; i<places.size(); i++){
            if(places.get(i).getType().equals("Club")){
                clubs.add(places.get(i));
            }else if(places.get(i).getType().equals("CafeBar")){
                cafebars.add(places.get(i));
            }else if(places.get(i).getType().equals("Tsipouradiko")){
                tsipouradika.add(places.get(i));
            }
        }
        clubAdapter = new ListAdapter(ListActivity.this.getApplicationContext(), clubs);
        cafebarAdapter = new ListAdapter(ListActivity.this.getApplicationContext(), cafebars);
        tsipouradikoAdapter = new ListAdapter(ListActivity.this.getApplicationContext(), tsipouradika);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
                return;
            }
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(lastLocation!=null){
            requestPlaces(lastLocation.getLongitude(), lastLocation.getLatitude());
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                FirebaseCrash.report(new Exception("onConnectionFailed() IntentSender.SendIntentException."));
            }
        } else {
            Toast.makeText(context, "Connection failed, try updating google play services and try again.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        requestPlaces(location.getLongitude(), location.getLatitude());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 10) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                finish();
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(parent.getItemAtPosition(pos).toString().equals("All")){
            if(places.size()==0){
                textView.setText("There are no registered places in your area.");
                textView.setVisibility(View.VISIBLE);
            }else{
                textView.setVisibility(View.GONE);
            }
            clubRecyclerView.swapAdapter(placeAdapter, false);
        }else if(parent.getItemAtPosition(pos).toString().equals("Clubs")){
            if(clubs.size()==0){
                textView.setText("There are no registered clubs in your area.");
                textView.setVisibility(View.VISIBLE);
            }else{
                textView.setVisibility(View.GONE);
            }
            clubRecyclerView.swapAdapter(clubAdapter, false);
        }else if(parent.getItemAtPosition(pos).toString().equals("CafeBars")){
            if(cafebars.size()==0){
                textView.setText("There are no registered cafebars in your area.");
                textView.setVisibility(View.VISIBLE);
            }else{
                textView.setVisibility(View.GONE);
            }
            clubRecyclerView.swapAdapter(cafebarAdapter, false);
        }else if(parent.getItemAtPosition(pos).toString().equals("Tsipouradika")){
            if(tsipouradika.size()==0){
                textView.setText("There are no registered tsipouradika in your area.");
                textView.setVisibility(View.VISIBLE);
            }else{
                textView.setVisibility(View.GONE);
            }
            clubRecyclerView.swapAdapter(tsipouradikoAdapter, false);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }
}
