package com.cityvibesgr.cityvibes.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.bo.Place;

import java.util.ArrayList;


/**
 * Created by alexsideris on 7/12/16.
 */


public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private ArrayList<Place> places;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        TextView details;

        public ViewHolder(View itemView) {
            super(itemView);
            placeName = (TextView) itemView.findViewById(R.id.place_name);
            details = (TextView) itemView.findViewById(R.id.place_info);
        }
    }

    public LeaderboardAdapter(Context context, ArrayList<Place> places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.placeName.setText(places.get(position).getName());
        holder.details.setText("Uploads: "+places.get(position).getFileUploads());
    }

    @Override
    public int getItemCount() {
        return places.size();
    }



}