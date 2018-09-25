package com.starnetsdkdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zhaichenyang on 2018/9/25.
 */

public class CircleView extends View {

    int mColor;
    float mRaidus;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a =context.obtainStyledAttributes(attrs,R.styleable.CircleView);
        mColor=a.getColor(R.styleable.CircleView_circle_color,Color.RED);
        mRaidus=a.getFloat(R.styleable.CircleView_circle_radius,50);
        a.recycle();

        Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width =getWidth();
        int height=getHeight();
        int raidus =Math.min(width,height)/2;
        Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(width/2,height/2,raidus,mPaint);
    }
}
