package com.flex.sklepik;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

/**
 * Created by Armin on 2017-05-17.
 */

public class ShopsAdapter extends RecyclerView.Adapter<ShopsAdapter.MyViewHolder> {

    Context mContext;
    List<String> shopsList;
    ProgressDialog progressDialog;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public ShopsAdapter(Context mContext, List<String> shopsList) {
        this.mContext = mContext;
        this.shopsList = shopsList;

        progressDialog = new ProgressDialog(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        shopsList.get(position);
        try {
            progressDialog.show();
            progressDialog.setMessage("Czekaj...");
            String stringformat = String.format("http://www.bakusek.zz.mu/shops/%s.png", shopsList.get(position));
            Glide.with(mContext).load(stringformat).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    Log.d("fsfsdf", e.getMessage());
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressDialog.hide();
                    return false;
                }
            }).centerCrop().fitCenter().into(holder.thumbnail);
        } catch (Exception e) {
            Glide.with(mContext).load("http://www.bakusek.zz.mu/shops/Zabka.png").centerCrop().fitCenter().into(holder.thumbnail);
        }

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Log.d("Nazwa sklepu: ", shopsList.get(position));
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra("shopName", shopsList.get(position));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }
}

