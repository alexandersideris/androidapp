package com.cityvibesgr.cityvibes.utility;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import static com.android.volley.Request.Method.POST;

/**
 * Created by alexsideris on 9/9/16.
 */

public interface UploadOfficialFileService {

    @Multipart
    @retrofit.http.POST("/upload_official_file")
    void upload(@Part("file") TypedFile file,
                @Part("place_id") int placeId,
                @Part("type") String type,
                @Part("thumbnail") TypedFile thumbnail,
                @Part("music_genre") String musicGenre,
                Callback<String> cb);
}