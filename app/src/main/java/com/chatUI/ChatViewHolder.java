package com.chatUI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.starnetsdkdemo.R;

/**
 * Created by zhaichenyang on 2018/9/13.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {
    TextView leftTv;
    TextView rightTv;

    public ChatViewHolder(View view) {
        super(view);
        leftTv=view.findViewById(R.id.leftTv);
        rightTv=view.findViewById(R.id.rightTv);
    }
}