package com.cityvibesgr.cityvibes.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.adobe.creativesdk.aviary.internal.headless.utils.MegaPixels;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.adapter.PlaceOwnerUploadOfficialFileAdapter;
import com.cityvibesgr.cityvibes.bo.ServerFile;
import com.cityvibesgr.cityvibes.utility.UploadOfficialFileService;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;
import com.cityvibesgr.cityvibes.utility.ServiceGenerator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedFile;


public class PlaceOwnerUploadOfficialFileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /* Codes for calling activities */
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static int REQUEST_VIDEO_CAPTURE = 2;
    private static int REQUEST_CREATIVE_SDK = 3;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 4;

    /* Global variables */
    private static final String FILE_PROVIDER_AUTHORITY = "com.cityvibesgr.cityvibes.fileprovider";
    private ArrayList<ServerFile> files = new ArrayList<ServerFile>();
    private ServerFile fileForUpload = new ServerFile();
    private int placeID;
    private String placeType = "";
    private TextView noUploadsYetTextview;
    private Spinner musicSpinner;
    private Context context = this;
    private ProgressDialog progressDialog;
    private PlaceOwnerUploadOfficialFileAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<String> musicGenres=new ArrayList<>();

    /* FireBase analytics */
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_owner_file_upload);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission();
        }
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        placeID = getIntent().getExtras().getInt("place_id");
        placeType = getIntent().getExtras().getString("place_type");

        noUploadsYetTextview = (TextView) findViewById(R.id.textview);
        progressDialog = new ProgressDialog(this);
        recyclerView = (RecyclerView) findViewById(R.id.file_list);

        ImageView takePhotoImageView = (ImageView) findViewById(R.id.takePhoto);
        ImageView takeVideoImageView = (ImageView) findViewById(R.id.takeVideo);
        Glide.with(this).load(R.drawable.take_photo).into(takePhotoImageView);
        Glide.with(this).load(R.drawable.take_video).into(takeVideoImageView);


        /* Set up spinner */

        if (placeType.equals("Club") || placeType.equals("CafeBar")) {

            /* Music Spinner */
            musicSpinner = (Spinner) findViewById(R.id.music_spinner);
            musicSpinner.setOnItemSelectedListener(this);

            musicGenres.add("Music Playing(Προαιρετικό)");
            musicGenres.add("Ελληνικά");
            musicGenres.add("Ξένα");
            musicGenres.add("Απ' όλα");

        }

        /* End of setting up spinners */

        getMyUploadedFiles();

    }

    private void getMyUploadedFiles() {
        String url = "http://www.cityvibes.gr/android/get_my_official_files/";
        final boolean[] updated = {false};
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            noUploadsYetTextview.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject model = (JSONObject) response.get(i);
                                ServerFile file = new ServerFile();
                                file.setUrl(model.getString("file_link"));
                                file.setServerId(model.getInt("id"));
                                file.setType(model.getString("type"));
                                if (file.getType().equals("video/mp4")) {
                                    file.setThumbnailUrl(model.getString("thumbnail_link"));
                                }
                                file.setApproximateTime(model.getString("approximate_time"));
                                file.setActualTime(model.getString("actual_time"));
                                files.add(file);

                                /* Set up music genre arraylist items */
                                if(placeType.equals("Club") || placeType.equals("CafeBar")){
                                    file.setMusicGenre(model.getString("music_genre"));
                                    if(!(musicGenres.contains(file.getMusicGenre()))){
                                        musicGenres.add(file.getMusicGenre());
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(new Exception("getMyUploadedFiles() JSONException."));
                            }

                        }

                        /* Set spinner values */
                        Collections.reverse(files);
                        if (placeType.equals("Club") || placeType.equals("CafeBar")) {
                            musicGenres.add("Άλλο");
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, musicGenres);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            musicSpinner.setAdapter(spinnerArrayAdapter);


                            if (files.size() > 0) {
                                for(int i=0; i<musicGenres.size(); i++){
                                    if(musicGenres.get(i).equals(files.get(0).getMusicGenre())){
                                        musicSpinner.setSelection(i);
                                        break;
                                    }
                                }
                            }
                            musicSpinner.setVisibility(View.VISIBLE);

                        }

                        /* End of setting up spinner values */


                        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                        adapter = new PlaceOwnerUploadOfficialFileAdapter(context, files, placeType);
                        recyclerView.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error while getting files. Please try again.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Keys.DJANGO_AUTHENTICATION_TOKEN);
                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    public void takePhoto(View v) {
        fileForUpload.setType("image/jpeg");
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

    public void takeVideo(View v) {
        fileForUpload.setType("video/mp4");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra("android.intent.extra.durationLimit", 10);
            ((Activity) context).startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        } else {
            Toast.makeText(context, "You need an android device running 4.3 or higher to upload videos to the platform.", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadFile() {
        TypedFile typedFile = new TypedFile("multipart/form-data", fileForUpload.getFile());
        TypedFile thumbnail = null;
        if (fileForUpload.getType().equals("video/mp4")) {
            progressDialog.setTitle("Uploading video");
            progressDialog.setMessage("Uploading video, this will only take a few seconds. Please, do not close the app.");
            thumbnail = new TypedFile("multipart/form-data", fileForUpload.getThumbnail());
        } else {
            progressDialog.setTitle("Uploading photo");
            progressDialog.setMessage("Uploading photo, this will only take a few seconds. Please, do not close the app.");
        }

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        UploadOfficialFileService service = ServiceGenerator.createService(UploadOfficialFileService.class, Keys.DJANGO_AUTHENTICATION_TOKEN);

        service.upload(typedFile, placeID, fileForUpload.getType(), thumbnail, fileForUpload.getMusicGenre(), new Callback<String>() {
            @Override
            public void success(String s, retrofit.client.Response response) {
                Toast.makeText(context, "File uploaded successfully.", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("official_file_uploads",params);
                finish();
                startActivity(getIntent());
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, "Failed to upload file. Please try again.", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                FirebaseCrash.report(new Exception("Retrofit Error while uploading "+fileForUpload.getType()+"   "+error.toString()));
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* REQUEST_IMAGE_CAPTURE */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                uploadFile();
            } else {
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

        /* REQUEST_VIDEO_CAPTURE */
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();

            fileForUpload.setUri(videoUri);
            fileForUpload.setFile(new File(getRealPathFromURI(context, videoUri)));

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(fileForUpload.getFile().getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            fileForUpload.setThumbnail(bitmapToFile(bitmap));

            compressAndUploadFile();
        }

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

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch (NullPointerException e) {
            FirebaseCrash.report(new Exception("getRealPathFromURI() NullPointerException."));
            return ("");
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private File bitmapToFile(Bitmap bm) {
        //create a file to write bitmap data
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "thumbnail_" + timeStamp + ".jpg";
        File file = new File(context.getCacheDir(), imageFileName);
        try {
            file.createNewFile();
            //Convert bitmap to byte array
            Bitmap bitmap = bm;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrash.report(new Exception("bitmapToFile() IOException."));
        }
        return file;
    }

    public void compressAndUploadFile() {
        progressDialog.setTitle("Uploading video");
        progressDialog.setMessage("Uploading video, this will only take a few seconds. Please, do not close the app.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        try {
            File outputDir = new File(context.getExternalFilesDir(null), "outputs");
            outputDir.mkdir();
            fileForUpload.setFile(File.createTempFile("transcode_test", ".mp4", outputDir));
        } catch (IOException e) {
            Toast.makeText(context, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            FirebaseCrash.report(new Exception("compressAndUploadFile() IOException."));
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        final ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = resolver.openFileDescriptor(fileForUpload.getUri(), "r");
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File not found.", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            FirebaseCrash.report(new Exception("compressAndUploadFile() FileNotFoundException."));
            return;
        }

        final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {

            }

            @Override
            public void onTranscodeCompleted() {
                onTranscodeFinished(true, "transcoded file placed on " + fileForUpload, parcelFileDescriptor, fileForUpload);
                Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, fileForUpload.getFile());
                //Toast.makeText(context, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + " ms", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTranscodeCanceled() {
                onTranscodeFinished(false, "Transcoder canceled.", parcelFileDescriptor, fileForUpload);
                Toast.makeText(context, "Transcode canceled bro. ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor, fileForUpload);
                Toast.makeText(context, "Transcode failed bro. ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

        };
        Future<Void> mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, fileForUpload.getFile().getAbsolutePath(),
                MediaFormatStrategyPresets.createAndroid720pStrategy(700 * 1000, 128 * 1000, 1), listener);
        return;
    }

    private void onTranscodeFinished(boolean isSuccess, String toastMessage, ParcelFileDescriptor parcelFileDescriptor, ServerFile file) {
        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            Log.w("Error while closing", e);
            progressDialog.dismiss();
            FirebaseCrash.report(new Exception("onTranscodeFinished() IOException."));
        }
        if (isSuccess) {
            //Toast.makeText(context, "Uploading now...", Toast.LENGTH_SHORT).show();
            uploadFile();
        }
    }

    public void getPermission() {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
            } else {
                finish();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(parent.getItemAtPosition(pos).toString().equals("Άλλο")){
            initiatePopupWindow(view);
        }else if(parent.getItemAtPosition(pos).toString().equals("Music Playing(Προαιρετικό)")){
            fileForUpload.setMusicGenre("Music Playing(Προαιρετικό)");
        }else{
            fileForUpload.setMusicGenre(parent.getItemAtPosition(pos).toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void initiatePopupWindow(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View myView = getLayoutInflater().inflate(R.layout.popup_dialog, null);
        Button button = (Button) myView.findViewById(R.id.button);
        mBuilder.setView(myView);
        AlertDialog dialog = mBuilder.create();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) myView.findViewById(R.id.edit_text);
                musicGenres.remove("Άλλο");
                musicGenres.add(editText.getText().toString());
                musicGenres.add("Άλλο");
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, musicGenres);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                musicSpinner.setAdapter(spinnerArrayAdapter);
                musicSpinner.setSelection(musicGenres.size()-2);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}