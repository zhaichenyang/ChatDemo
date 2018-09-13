package com.starnetsdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by zhaichenyang on 2018/9/5.
 */

public class RengongActivity extends Activity {

    private Button startBtn;
    private Button sendBtn;
    private Button closeBtn;
    //服务起地址
    private String host = "http://10.31.61.3:8080";
    private ManualCustomService mcs;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cs);
        intiView();
        mcs=new ManualCustomService(this);
        mcs.initManualCustomService(host);
        mcs.setUser("111","2222");
        mcs.setOnChatUIChangedListener(mOnChatUIChangedListener);
    }

    //初始化控件
    private void intiView() {
        startBtn = findViewById(R.id.btn_startCS);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcs.startManualCustomService();
            }
        });
        sendBtn=findViewById(R.id.btn_sendMessage);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcs.cancelQueue();
            }
        });
        closeBtn=findViewById(R.id.btn_finishCS);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcs.closeManualCustomService();
            }
        });
    }

    private OnChatUIChangedListener mOnChatUIChangedListener=new OnChatUIChangedListener() {
        @Override
        public void addChatItem() {
            Log.e("OnChatUIChangedListener","y");

        }
    };
}
