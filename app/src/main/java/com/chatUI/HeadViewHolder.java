package com.chatUI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.starnetsdkdemo.R;

/**
 * Created by zhaichenyang on 2018/9/15.
 */

public class HeadViewHolder extends RecyclerView.ViewHolder {

    ImageView ivHead;

    public HeadViewHolder(View itemView) {
        super(itemView);
        ivHead = itemView.findViewById(R.id.ivHead);
    }
}
