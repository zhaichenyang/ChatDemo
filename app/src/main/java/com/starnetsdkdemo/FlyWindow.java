package com.starnetsdkdemo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by zhaichenyang on 2018/9/27.
 */

public class FlyWindow {
    private Context mContext;

    public FlyWindow(Context context){
        this.mContext=context;
    }

    public void creatWindow(){
        WindowManager wm =(WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        //设置window的类型
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置window的属性，FLAG_NOT_TOUCH_MODAL表示window区域内点击事件自己处理，区域外的向底层传递
        wmParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //调整悬浮窗口位置
        wmParams.gravity = Gravity.RIGHT| Gravity. CENTER_VERTICAL;
        //设置悬浮窗口宽高
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height =WindowManager.LayoutParams.WRAP_CONTENT;
        //设置窗口透明度
        wmParams.alpha=0.5f;

        View view=LayoutInflater.from(mContext).inflate(R.layout.window_view, null);

        wm.addView(view,wmParams);
        wm.removeView(view);

    }
}
