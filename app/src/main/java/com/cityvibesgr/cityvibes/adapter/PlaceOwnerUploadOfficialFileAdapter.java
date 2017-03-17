package com.cityvibesgr.cityvibes.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.activity.GalleryActivity;
import com.cityvibesgr.cityvibes.activity.PlaceOwnerUploadOfficialFileActivity;
import com.cityvibesgr.cityvibes.bo.ServerFile;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PlaceOwnerUploadOfficialFileAdapter extends RecyclerView.Adapter<PlaceOwnerUploadOfficialFileAdapter.ViewHolder> {
    private ArrayList<ServerFile> files;
    private Context context;
    private String placeType;

    public PlaceOwnerUploadOfficialFileAdapter(Context context, ArrayList<ServerFile> files, String placeType) {
        this.context = context;
        this.files = files;
        this.placeType = placeType;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView, playButtonImageView;
        RelativeLayout deleteTextView;
        TextView approximateTime, musicPlaying;
        int position;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = (ImageView) itemView.findViewById(R.id.image);
            deleteTextView = (RelativeLayout) itemView.findViewById(R.id.delete);
            playButtonImageView = (ImageView) itemView.findViewById(R.id.play_button);
            approximateTime = (TextView) itemView.findViewById(R.id.approximate_time);
            musicPlaying = (TextView) itemView.findViewById(R.id.music_playing);

            photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GalleryActivity.class);
                    String url = files.get(position).getUrl();
                    String type = files.get(position).getType();
                    intent.putExtra("url",url);
                    intent.putExtra("type", type);
                    intent.putExtra("club_and_time", files.get(position).getActualTime());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            playButtonImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, GalleryActivity.class);
                    String url = files.get(position).getUrl();
                    String type = files.get(position).getType();

                    intent.putExtra("url",url);
                    intent.putExtra("type", type);
                    intent.putExtra("club_and_time", files.get(position).getActualTime());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(context)
                            .setTitle("Delete file")
                            .setMessage("Are you sure you want to delete this file?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = "http://www.cityvibes.gr/android/delete_official_file/" + files.get(position).getServerId();
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    Toast.makeText(context,"File deleted successfully.", Toast.LENGTH_LONG).show();
                                                    PlaceOwnerUploadOfficialFileActivity p = (PlaceOwnerUploadOfficialFileActivity) context;
                                                    p.finish();
                                                    p.startActivity(p.getIntent());
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(context,"Error in deleting file. "+error.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }){
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String>  params = new HashMap<String, String>();
                                            params.put("Authorization", "Token "+ Keys.DJANGO_AUTHENTICATION_TOKEN);
                                            return params;
                                        }
                                    };
                                    MySingleton.getInstance(context).addToRequestQueue(stringRequest);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
            });

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_file_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.approximateTime.setText("  "+files.get(position).getApproximateTime());
        if(placeType.equals("Club") || placeType.equals("CafeBar")){
            if(!(files.get(position).getMusicGenre().equals("Music Playing(Προαιρετικό)"))){
                holder.musicPlaying.setText("   Music playing: "+files.get(position).getMusicGenre());
                holder.musicPlaying.setVisibility(View.VISIBLE);
            }
        }
        if(files.get(position).getType().equals("image/jpeg")){
            Glide.with(context).load(files.get(position).getUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.photoImageView);

        }else if(files.get(position).getType().equals("video/mp4")){
            Glide.with(context).load(files.get(position).getThumbnailUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.photoImageView);
            holder.photoImageView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, context.getResources().getDisplayMetrics());
            holder.photoImageView.requestLayout();
            Glide.with(context).load(R.drawable.playbutton).into(holder.playButtonImageView);
            holder.playButtonImageView.setVisibility(View.VISIBLE);

        }

        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

}
