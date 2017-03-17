package com.cityvibesgr.cityvibes.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        VideoView videoView = (VideoView) findViewById(R.id.video);
        String type = getIntent().getExtras().getString("type");
        String url = getIntent().getExtras().getString("url");
        String title = getIntent().getStringExtra("club_and_time");

        TextView clubAndTime = (TextView) findViewById(R.id.club_and_time);
        clubAndTime.setText(title);

        if(type.equals("image/jpeg")){
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(url)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }else if(type.equals("video/mp4")){
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(url));
            videoView.start();
        }

    }

}
