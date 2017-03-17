package com.cityvibesgr.cityvibes.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.adobe.creativesdk.aviary.internal.headless.utils.MegaPixels;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.DetailViewPagerAdapter;
import com.cityvibesgr.cityvibes.bo.Place;
import com.cityvibesgr.cityvibes.bo.ServerFile;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.cityvibesgr.cityvibes.utility.ServiceGenerator;
import com.cityvibesgr.cityvibes.utility.UploadSocialFileService;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

import static android.R.attr.name;


public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Context context = this;
    private ServerFile fileForUpload = new ServerFile();
    private static final String FILE_PROVIDER_AUTHORITY = "com.cityvibesgr.cityvibes.fileprovider";
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static int REQUEST_CREATIVE_SDK = 3;
    private static int MY_PERMISSIONS_REQUEST_CAMERA = 4;
    private ProgressDialog progressDialog;
    private int thePlaceID=-1;
    private String placeName="";
    private DetailSocialFragment socialFragment;
    private DetailOfficialFragment officialFragment;


    /* Code for location */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest;
    private int REQUEST_CHECK_SETTINGS = 5;
    /* End of code for location */

    /* FireBase analytics */
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initCollapsingToolbar();
        progressDialog = new ProgressDialog(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        final int placeID = getIntent().getExtras().getInt("place_id");
        thePlaceID = placeID;

        getPlaceDetails(placeID);

        addView(placeID);

        /* Code for location */
        if(!Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN.equals("")){
            setUpGoogleClient();
        }

    }

    public void getPlaceDetails(int placeID){
        String url = "http://www.cityvibes.gr/android/place/"+placeID;
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{
                            Place place = new Place();

                            place.setID(response.getInt("id"));
                            place.setName(response.getString("name"));
                            place.setFacebookAccountLink(response.getString("facebook_account_link"));
                            place.setProfilePhotoUrl(response.getString("profile_picture_link"));
                            place.setCity(response.getString("city"));
                            place.setLocation(response.getString("location"));
                            place.setLastUpdate(response.getString("string_last_update"));
                            place.setType(response.getString("type"));

                            if (place.getType().equals("Club")) {
                                place.setDrinkPrice(response.getDouble("drink_price"));
                                place.setBottlePrice(response.getDouble("bottle_price"));
                            } else if (place.getType().equals("Tsipouradiko")) {
                                place.setWinePrice(response.getDouble("wine_price"));
                                place.setRetsinaPrice(response.getDouble("retsina_price"));
                            } else if (place.getType().equals("CafeBar")) {
                                place.setCoffeePrice(response.getDouble("coffee_price"));
                                place.setDrinkPrice(response.getDouble("drink_price"));
                            }

                            TextView nameTextView = (TextView) findViewById(R.id.club_info_name);
                            nameTextView.setText(place.getName());

                            placeName = place.getName();


                            TextView detailsTextView = (TextView) findViewById(R.id.club_info_details);

                            if(place.getType().equals("Club")){
                                detailsTextView.setText("Location: " + place.getLocation() + ", " + place.getCity() + "\nDrink Price: " + place.getDrinkPrice() + " €\nBottle Price: " + place.getBottlePrice() + " €");
                            }else if(place.getType().equals("CafeBar")){
                                detailsTextView.setText("Location: " + place.getLocation() + ", " + place.getCity() + "\nCoffee(Esspresso): " + place.getCoffeePrice() + " €\nDrink Price: " + place.getDrinkPrice() + " €");
                            }else if(place.getType().equals("Tsipouradiko")){
                                detailsTextView.setText("Location: " + place.getLocation() + ", " + place.getCity() + "\nΡετσίνα: " + place.getRetsinaPrice() + " €\nΚρασί Χύμα(λίτρο): " + place.getWinePrice() + " €");
                            }


                            ImageView instaLink = (ImageView) findViewById(R.id.club_info_facebook_icon2);
                            Glide.with(context).load(R.drawable.instapic).into(instaLink);

                            ImageView fbLink = (ImageView) findViewById(R.id.club_info_facebook_icon);
                            Glide.with(context).load(R.drawable.fb3).into(fbLink);
                            fbLink.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse(place.getFacebookAccountLink()));
                                    startActivity(intent);

                                }

                            });

                            ImageView imageView = (ImageView) findViewById(R.id.club_info_photo);
                            Glide.with(context).load(place.getProfilePhotoUrl())
                                    .thumbnail(0.5f)
                                    .crossFade()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);

                            ImageView topImage = (ImageView) findViewById(R.id.backdrop);
                            Glide.with(context).load(R.drawable.toolbar_detail_image).into(topImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(new Exception("getPlaceDetails() JSONException."));
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }

    private void addView(int placeID) {
        String url = "http://www.cityvibes.gr/android/add_view/"+placeID;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(context, "View added.", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(context, "Error in getting next upload time bro..", Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void instagramAuthentication(View v){
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    public void takePhoto(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission();
        }else{
            progressDialog.setTitle("Checking location");
            progressDialog.setMessage("Checking your location, this will only take a few seconds.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            mGoogleApiClient.connect();
        }
    }

    public void canTakePhoto(double longitude, double latitude){
        String url = "http://www.cityvibes.gr/android/can_take_photo/"+thePlaceID;

        StringRequest jsonRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("\"Access Granted\"")){
                            progressDialog.dismiss();
                            takePhoto();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "Try moving closer to "+placeName+".", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error while checking location. Please try again.", Toast.LENGTH_LONG).show();
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
        jsonRequest.setShouldCache(false);
        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }

    public void takePhoto() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                fileForUpload.setFile(photoFile);
            } catch (IOException ex) {
                FirebaseCrash.report(new Exception("takePhoto() IOException."));
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                fileForUpload.setUri(photoURI);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    ClipData clip = ClipData.newUri(context.getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* REQUEST_IMAGE_CAPTURE */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                uploadFile();
            }else{
                Intent imageEditorIntent = new AdobeImageIntent.Builder(context)
                        .setData(fileForUpload.getUri()) // input image source
                        .withOutputFormat(Bitmap.CompressFormat.JPEG) // output format
                        .withOutputSize(MegaPixels.Mp2) // output size
                        .withOutput(fileForUpload.getFile())
                        .build();
                startActivityForResult(imageEditorIntent, REQUEST_CREATIVE_SDK);
            }

        }

        /* REQUEST_CREATIVE_SDK */
        if (requestCode == REQUEST_CREATIVE_SDK && resultCode == RESULT_OK) {

            Uri editedImageUri = data.getParcelableExtra(AdobeImageIntent.EXTRA_OUTPUT_URI);
            fileForUpload.setUri(editedImageUri);

            uploadFile();
        }


    }

    public void uploadFile() {
        TypedFile typedFile = new TypedFile("multipart/form-data", fileForUpload.getFile());

        progressDialog.setTitle("Uploading photo");
        progressDialog.setMessage("Uploading photo, this will only take a few seconds. Please, do not close the app.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();


        UploadSocialFileService service = ServiceGenerator.createService(UploadSocialFileService.class, Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN);
        service.upload(typedFile, thePlaceID, new Callback<String>() {
            @Override
            public void success(String s, retrofit.client.Response response) {
                Toast.makeText(context, "File uploaded successfully. ", Toast.LENGTH_LONG).show();
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("social_file_uploads",params);
                progressDialog.dismiss();
                finish();
                startActivity(getIntent());
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed to upload photo. Please try again.", Toast.LENGTH_LONG).show();
                FirebaseCrash.report(new Exception("Retrofit Error while uploading photo"+"   "+error.toString()));
            }
        });
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public void closeFragment(View v){
        socialFragment.closeFragment();
    }


    public void getPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }else{
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                takePhoto();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect();
            } else {
                //Toast.makeText(context, "We need your location to check whether you actually are at the corresponding place.", Toast.LENGTH_LONG).show();
            }
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
        mGoogleApiClient.disconnect();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)        // 10 seconds, in milliseconds
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
                                    DetailActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
                FirebaseCrash.report(new Exception("onConnectionFailed() IntentSender.SendIntentException"));
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mGoogleApiClient.disconnect();
        canTakePhoto(location.getLongitude(), location.getLatitude());
    }

    private void setupViewPager(ViewPager viewPager) {
        DetailViewPagerAdapter adapter = new DetailViewPagerAdapter(getSupportFragmentManager());
        socialFragment = new DetailSocialFragment();
        officialFragment = new DetailOfficialFragment();
        adapter.addFragment(officialFragment, "Official");
        adapter.addFragment(socialFragment, "Social");
        viewPager.setAdapter(adapter);
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
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
                    //collapsingToolbar.setTitle(getString(R.string.app_name));
                    //textView.setVisibility(View.GONE);
                    isShow = true;

                } else if (isShow) {
                    //textView.setVisibility(View.VISIBLE);
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


}