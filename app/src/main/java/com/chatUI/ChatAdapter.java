package com.chatUI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.starnetsdkdemo.R;

import java.util.List;

import static com.chatUI.RefreshRecyclerView.REFRESH_HEADER;

public class ChatAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<ChatBean> mDatas;
    public MRefreshHeader mRefreshHeader;

    public ChatAdapter(Context context, List<ChatBean> mDatas) {
        this.mContext = context;
        this.mDatas = mDatas;
        setRefreshView(null);
    }

    public void setRefreshView(MRefreshHeader refreshHeader) {
        if (refreshHeader == null) {
            this.mRefreshHeader = new MRefreshHeader(mContext);
        } else {
            this.mRefreshHeader = refreshHeader;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == REFRESH_HEADER) {
            HeadViewHolder headViewHolder = new HeadViewHolder(mRefreshHeader.getContainer());
            return headViewHolder;
        } else {
            //下面的方法必须有false参数
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat, parent, false);
            ChatViewHolder viewHolder = new ChatViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            ((ChatViewHolder) holder).leftTv.setVisibility(View.GONE);
            ((ChatViewHolder) holder).rightTv.setVisibility(View.GONE);
            if (mDatas.get(position).getSource().equals("s")) {
                ((ChatViewHolder) holder).leftTv.setVisibility(View.VISIBLE);
                ((ChatViewHolder) holder).leftTv.setText(mDatas.get(position).getMessage());
            } else {
                ((ChatViewHolder) holder).rightTv.setVisibility(View.VISIBLE);
                ((ChatViewHolder) holder).rightTv.setText(mDatas.get(position).getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position == 0 ? REFRESH_HEADER : 0;
    }
}