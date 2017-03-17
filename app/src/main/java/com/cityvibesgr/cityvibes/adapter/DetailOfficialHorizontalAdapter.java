package com.cityvibesgr.cityvibes.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.activity.GalleryActivity;
import com.cityvibesgr.cityvibes.bo.MyFile;


import java.util.ArrayList;


/**
 * Created by alexsideris on 7/19/16.
 */

public class DetailOfficialHorizontalAdapter extends RecyclerView.Adapter<DetailOfficialHorizontalAdapter.MyViewHolder> {
    private ArrayList<MyFile> horizontalFiles;
    private Context context;

    public DetailOfficialHorizontalAdapter(Context context, ArrayList<MyFile> files) {
        this.context = context;
        this.horizontalFiles = files;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.official_horizontal_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return horizontalFiles.size();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final MyFile file = horizontalFiles.get(position);
        holder.position = position;

        if(file.getType().equals("image/jpeg")) {
            Glide.with(context).load(file.getUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.horizontalFile);
        }else if(file.getType().equals("video/mp4")){
            Glide.with(context).load(file.getThumbnailUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.horizontalFile);
            //holder.horizontalFile.setBackgroundColor(BLACK);
            Glide.with(context).load(R.drawable.playbutton).into(holder.playButtonImage);
            holder.playButtonImage.setVisibility(View.VISIBLE);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView horizontalFile;
        int position;
        public ImageView playButtonImage;

        public MyViewHolder(View v) {
            super(v);
            horizontalFile = (ImageView) v.findViewById(R.id.horizontal_list_file);
            playButtonImage = (ImageView) v.findViewById(R.id.play_button);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, GalleryActivity.class);
            intent.putExtra("url",horizontalFiles.get(position).getUrl());
            intent.putExtra("club_and_time", "Yesterday at " + horizontalFiles.get(0).getActualTime());
            intent.putExtra("type", horizontalFiles.get(position).getType());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
