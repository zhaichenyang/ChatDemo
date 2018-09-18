package com.chatUI;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.reflect.Field;

/**
 * Created by zhaichenyang on 2018/9/15.
 */

public class RefreshRecyclerView extends RecyclerView {

    private Context mContext;
    private int startY;
    private MRefreshHeader mRefreshHeader;
    private OnRefreshListener onRefreshListener;
    private boolean canRefreshing = false;
    private long lastTime=0;


    public static int REFRESH_HEADER = 95519;

    public RefreshRecyclerView(Context context) {
        super(context);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }


    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.mRefreshHeader = (((ChatAdapter) adapter).mRefreshHeader);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(canRefresh()){
                    int tempY = (int) e.getY();
                    int space = tempY - startY;
                    int topPadding = space - mRefreshHeader.getMeasuredHeight();
                    if (space > 0) {
                        mRefreshHeader.onMove(topPadding);
                        if (topPadding > 0) {
                            canRefreshing = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                long currentTime=System.currentTimeMillis();
                if (canRefreshing) {
                    if(currentTime-lastTime>600){
                        lastTime=currentTime;
                        startY = -1;
                        mRefreshHeader.setRefresh(true);
                        onRefreshListener.onRefresh();
                        mRefreshHeader.smoothScrollTo(0, new MRefreshHeader.MRefreshListener() {
                            @Override
                            public void mRefreshComplete() {
                                onRefreshListener.refreshComplete();
                            }
                        });
                        canRefreshing = false;
                    }

                } else {
                    mRefreshHeader.setRefresh(false);
                    mRefreshHeader.smoothScrollTo(-mRefreshHeader.getMeasuredHeight(), null);
                }
                break;
        }

        return super.onTouchEvent(e);
    }


    public interface OnRefreshListener {

        void onRefresh();

        void refreshComplete();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    private boolean canRefresh() {
        LinearLayoutManager mLayoutManager =
                (LinearLayoutManager) this.getLayoutManager();
        int pos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        if (pos == 0) {
            return true;
        } else {
            return false;
        }

    }

    public void scrollGoPosition(int cur){
        LinearLayoutManager mLayoutManager =
                (LinearLayoutManager) this.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(cur, 0);
        mLayoutManager.setStackFromEnd(true);
    }

    public void setMaxFlingVelocity(int velocity){
        try {
            Field field =this.getClass().getDeclaredField("mMaxFlingVelocity");
            field.setAccessible(true);
            field.set(this,velocity);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
}