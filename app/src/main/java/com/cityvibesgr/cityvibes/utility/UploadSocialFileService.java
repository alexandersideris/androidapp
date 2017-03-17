package com.cityvibesgr.cityvibes.utility;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import static com.android.volley.Request.Method.POST;

/**
 * Created by alexsideris on 06/3/16.
 */

public interface UploadSocialFileService {

    @Multipart
    @retrofit.http.POST("/upload_social_file")
    void upload(@Part("file") TypedFile file,
                @Part("place_id") int placeId,
                Callback<String> cb);
}