package com.chatUI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
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

    public int getMeasureHeight() {
        itemView.measure(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        return itemView.getMeasuredHeight();
    }

    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        lp.height = height;
        itemView.setLayoutParams(lp);

    }

    public int getVisibleHeight() {
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        return lp.height;
    }
}
