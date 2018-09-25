package com.chatUI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.starnetsdkdemo.R;

import java.util.ArrayList;

/**
 * Created by zhaichenyang on 2018/9/13.
 */

public class ChatActivity extends Activity {

    private RefreshRecyclerView easyRecyclerView;
    private ArrayList<ChatBean> mData;
    private ArrayList<ChatBean> mAddData;
    private ChatAdapter mAdapter;
    private int cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initData();
        initView();
    }

    private void initData() {
        mData = new ArrayList<>();
        mAddData = new ArrayList<>();
        mData.add(new ChatBean("1111", "s"));
        mData.add(new ChatBean("1111", "s"));
        mData.add(new ChatBean("2222", "c"));
        mData.add(new ChatBean("3333", "s"));
        mData.add(new ChatBean("4444", "c"));
        mData.add(new ChatBean("5555", "s"));
        mData.add(new ChatBean("6666", "c"));
        mData.add(new ChatBean("7777", "s"));
        mData.add(new ChatBean("8888", "s"));
        mData.add(new ChatBean("9999", "c"));
        mData.add(new ChatBean("aaaa", "s"));
        mData.add(new ChatBean("bbbb", "c"));
        mData.add(new ChatBean("cccc", "s"));
        mData.add(new ChatBean("dddd", "s"));
        mData.add(new ChatBean("eeee", "s"));
        mData.add(new ChatBean("ffff", "c"));
        mData.add(new ChatBean("gggg", "s"));
        mData.add(new ChatBean("hhhh", "s"));
        mData.add(new ChatBean("iiii", "c"));
        mData.add(new ChatBean("jjjj", "s"));
        mData.add(new ChatBean("kkkk", "s"));
    }

    private void initView() {
        easyRecyclerView = findViewById(R.id.recyclerView);
        easyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChatAdapter(this, mData);
        easyRecyclerView.setAdapter(mAdapter);
        easyRecyclerView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAddData.clear();
                mAddData.add(new ChatBean("0000", "c"));
                mData.addAll(1, mAddData);
                cur=mAddData.size()+1;
            }

            @Override
            public void refreshComplete() {
                mAdapter.notifyDataSetChanged();
                easyRecyclerView.scrollGoPosition(cur);
            }
        });

    }
}
