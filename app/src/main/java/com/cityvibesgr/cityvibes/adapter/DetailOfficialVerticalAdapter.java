package com.cityvibesgr.cityvibes.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.activity.GalleryActivity;
import com.cityvibesgr.cityvibes.activity.ReportActivity;
import com.cityvibesgr.cityvibes.bo.MyFile;
import com.cityvibesgr.cityvibes.utility.Keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by alexsideris on 7/15/16.
 */
public class DetailOfficialVerticalAdapter extends RecyclerView.Adapter<DetailOfficialVerticalAdapter.ViewHolder> {
    private final ArrayList<MyFile> files;
    private Context context;
    private Map<Integer, Parcelable> scrollStatePositionsMap = new HashMap<>();
    private MyFile theFile;

    public DetailOfficialVerticalAdapter(Context context, ArrayList<MyFile> files, ArrayList<String> photosessionDetails) {
        this.context = context;
        this.files = files;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.official_vertical_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GalleryActivity.class);
                intent.putExtra("url",files.get(position).getUrl());
                intent.putExtra("type", files.get(position).getType());
                //Toast.makeText(context, files.get(position).getClubName(), Toast.LENGTH_SHORT).show();
                intent.putExtra("club_and_time", "Today at "+files.get(position).getActualTime());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GalleryActivity.class);
                intent.putExtra("url",files.get(position).getUrl());
                intent.putExtra("type", files.get(position).getType());
                intent.putExtra("club_and_time", "Today at "+files.get(position).getActualTime());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        if(files.get(position).getDay().equals("today")){
            //today's files
            holder.approximateTime.setText("  "+files.get(position).getApproximateTime());
            if(!(files.get(position).getMusicGenre().equals("Music Playing(Προαιρετικό)"))){
                holder.musicPlaying.setText("   Music playing: "+files.get(position).getMusicGenre());
                holder.musicPlaying.setVisibility(View.VISIBLE);
            }
            holder.position = position;

            if(files.get(position).getType().equals("image/jpeg")) {
                Glide.with(context).load(files.get(position).getUrl())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.image);
            }else if(files.get(position).getType().equals("video/mp4")){
                Glide.with(context).load(files.get(position).getThumbnailUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
                holder.image.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, context.getResources().getDisplayMetrics());
                holder.image.requestLayout();
                Glide.with(context).load(R.drawable.playbutton).into(holder.playButton);
                holder.playButton.setVisibility(View.VISIBLE);
            }
        }else if(position==files.size()-1){
            //yesterday's files

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(mLayoutManager);
            ArrayList<MyFile> yesterdaysFiles = new ArrayList<>();
            for(int i=0; i<files.size(); i++){
                if(files.get(i).getDay().equals("yesterday")){
                    yesterdaysFiles.add(files.get(i));
                }
            }
            holder.recyclerView.setAdapter(new DetailOfficialHorizontalAdapter(context, yesterdaysFiles));
            holder.approximateTime.setText("  Yesterday");
            holder.approximateTime.setPadding(10, 15, 10, 15);
            holder.dots.setVisibility(View.GONE);
            if (scrollStatePositionsMap.containsKey(position)) {
                holder.recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        holder.recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        holder.recyclerView.getLayoutManager().onRestoreInstanceState(scrollStatePositionsMap.get(position));
                        return false;
                    }
                });
            }
        }else{
            holder.cardView.setVisibility(View.GONE);
        }

        holder.dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                theFile = files.get(position);
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenu().add(1, R.id.report_file, 1, "Report");
                popup.setOnMenuItemClickListener(new MyMenuItemClickListener(context));
                popup.show();
            }
        });

        holder.setPosition(position);


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerView;
        public CardView cardView;
        public TextView approximateTime, musicPlaying;
        public ImageView image, playButton, dots;
        public int position;

        public ViewHolder(final View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.horizontal_list);
            approximateTime = (TextView) itemView.findViewById(R.id.approximate_time);
            musicPlaying = (TextView) itemView.findViewById(R.id.music_playing);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            image = (ImageView) itemView.findViewById(R.id.image);
            dots = (ImageView) itemView.findViewById(R.id.overflow);
            playButton = (ImageView) itemView.findViewById(R.id.play_button);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        scrollStatePositionsMap.put(position, recyclerView.getLayoutManager().onSaveInstanceState());
                    }
                }

            });
            recyclerView.setNestedScrollingEnabled(false);
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        Context context;

        public MyMenuItemClickListener(Context context) {
            this.context = context;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.report_file:
                    if(Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN.equals("") && Keys.DJANGO_AUTHENTICATION_TOKEN.equals("")){
                        Toast.makeText(context, "You have to sign in order to report a photo.", Toast.LENGTH_LONG).show();
                    }else{
                        Intent intent = new Intent(context, ReportActivity.class);
                        intent.putExtra("url",theFile.getUrl());
                        intent.putExtra("id",theFile.getId());
                        intent.putExtra("type","official");
                        context.startActivity(intent);
                    }
                    return true;
                default:
            }
            return false;
        }
    }
}
