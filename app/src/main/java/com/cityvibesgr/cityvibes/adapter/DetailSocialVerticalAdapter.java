package com.cityvibesgr.cityvibes.adapter;

/**
 * Created by alexsideris on 06/03/2017.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.cityvibesgr.cityvibes.activity.DetailActivity;
import com.cityvibesgr.cityvibes.activity.GalleryActivity;
import com.cityvibesgr.cityvibes.activity.ReportActivity;
import com.cityvibesgr.cityvibes.bo.SocialFile;
import com.cityvibesgr.cityvibes.utility.Keys;
import com.cityvibesgr.cityvibes.utility.MySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DetailSocialVerticalAdapter extends RecyclerView.Adapter<DetailSocialVerticalAdapter.ViewHolder> {
    private final ArrayList<SocialFile> files;
    private Context context;
    private Map<Integer, Parcelable> scrollStatePositionsMap = new HashMap<>();
    private SocialFile theSocialFile;

    public DetailSocialVerticalAdapter(Context context, ArrayList<SocialFile> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.social_vertical_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.actualTime.setText(files.get(position).getApproximateTime());
        holder.username.setText(files.get(position).getInstagramUsername());

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/"+files.get(position).getInstagramUsername());
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                likeIng.setPackage("com.instagram.android");

                try {
                    context.startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.instagram.com/"+files.get(position).getInstagramUsername())));
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GalleryActivity.class);
                intent.putExtra("url",files.get(position).getUrl());
                intent.putExtra("type", "image/jpeg");
                intent.putExtra("club_and_time", "Today at "+files.get(position).getActualTime());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.position = position;
        Glide.with(context).load(files.get(position).getInstagramProfilePictureLink())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.profileImage);

        Glide.with(context).load(files.get(position).getUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);

        holder.dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                theSocialFile = files.get(position);
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenu().add(1, R.id.report_file, 1, "Report");
                if(Keys.INSTAGRAM_USERNAME.equals(Keys.ROOT) || Keys.INSTAGRAM_USERNAME.equals(holder.username.getText().toString())){
                    popup.getMenu().add(1, R.id.delete_social_file, 1, "Delete");
                }

                popup.setOnMenuItemClickListener(new MyMenuItemClickListener(context));
                popup.show();
            }
        });

        holder.setPosition(position);


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerView;
        public CardView cardView;
        public TextView actualTime;
        public ImageView image, profileImage;
        public TextView username;
        public int position;
        public ImageView dots;

        public ViewHolder(final View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.horizontal_list);
            actualTime = (TextView) itemView.findViewById(R.id.actual_time);
            username = (TextView) itemView.findViewById(R.id.username);
            profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
            dots = (ImageView) itemView.findViewById(R.id.overflow);

            cardView = (CardView) itemView.findViewById(R.id.card_view);
            image = (ImageView) itemView.findViewById(R.id.image);


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
                        intent.putExtra("url",theSocialFile.getUrl());
                        intent.putExtra("id",theSocialFile.getId());
                        intent.putExtra("type","social");
                        context.startActivity(intent);
                    }
                    return true;
                case R.id.delete_social_file:
                    new AlertDialog.Builder(context)
                            .setTitle("Delete file")
                            .setMessage("Are you sure you want to delete this file?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = "http://www.cityvibes.gr/android/delete_social_file/" + theSocialFile.getId();
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    Toast.makeText(context,"File deleted successfully.", Toast.LENGTH_LONG).show();
                                                    DetailActivity p = (DetailActivity) context;
                                                    p.finish();
                                                    p.startActivity(p.getIntent());
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(context,"Error while deleting file. "+error.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }){
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String>  params = new HashMap<String, String>();
                                            params.put("Authorization", "Token "+ Keys.INSTAGRAM_USER_AUTHENTICATION_TOKEN);
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
                    return true;
                default:
            }
            return false;
        }
    }

}

