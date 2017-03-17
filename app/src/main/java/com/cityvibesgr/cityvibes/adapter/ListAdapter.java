package com.cityvibesgr.cityvibes.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.activity.DetailActivity;
import com.cityvibesgr.cityvibes.bo.Place;


import java.util.ArrayList;



/**
 * Created by alexsideris on 7/12/16.
 */


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private ArrayList<Place> places;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView placeName;
        ImageView placeImageView;
        TextView details;

        public ViewHolder(View itemView) {
            super(itemView);
            placeName = (TextView) itemView.findViewById(R.id.club_name);
            placeImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            details = (TextView) itemView.findViewById(R.id.details);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            TextView v = (TextView) view.findViewById(R.id.club_name);
            String clubName = v.getText().toString();
            Place place = new Place();
            for (int i = 0; i < places.size(); i++) {
                if (places.get(i).getName().equals(clubName)) {
                    place = places.get(i);
                    break;
                }
            }

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("place_id", place.getID());
            intent.putExtra("place_name", place.getName());
            intent.putExtra("place_type", place.getType());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    public ListAdapter(Context context, ArrayList<Place> places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.placeName.setText(places.get(position).getName());
        holder.details.setText(places.get(position).getLastUpdate());
        Glide.with(context).load(places.get(position).getProfilePhotoUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.placeImageView);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }



}