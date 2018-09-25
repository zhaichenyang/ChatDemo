package com.chatUI;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.starnetsdkdemo.R;


/**
 * Created by xiasuhuei321 on 2017/1/3.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 * <p>
 * 一个RefreshHeader接口的实现类
 */

public class MRefreshHeader {
    private final String TAG = getClass().getSimpleName();

    private final LinearLayout mContainer;
    private final int mMeasuredHeight;
    private View rootView;
    private boolean isRefresh;

    public MRefreshHeader(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.item_chathead, new LinearLayout(context), false);
        mContainer = (LinearLayout) rootView.findViewById(R.id.ll_header_content);
        //对View进行measure，然后才能获取到其高度
        measureView();
        mMeasuredHeight = rootView.getMeasuredHeight();
        Log.e(TAG, "mMeasuredHeight:" + mMeasuredHeight);
        onMove(-mMeasuredHeight);
    }

    public void smoothScrollTo(int destHeight, final MRefreshListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(getPadding(), destHeight);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                onMove((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(isRefresh){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            smoothScrollTo(-mMeasuredHeight,null);
                            if(listener!=null){
                                listener.mRefreshComplete();
                            }
                        }
                    }, 300);
                    isRefresh=false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    private void measureView(){
        ViewGroup.LayoutParams p=rootView.getLayoutParams();
        if(p==null){
            p=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width=ViewGroup.getChildMeasureSpec(0,0,p.width);
        int height;
        int tempHeight=p.height;
        if(tempHeight>0){
            height= View.MeasureSpec.makeMeasureSpec(tempHeight, View.MeasureSpec.EXACTLY);
        }else{
            height= View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        rootView.measure(width,height);
    }

    public void onMove(int topPadding) {
        rootView.setPadding(rootView.getPaddingLeft(),topPadding,rootView.getPaddingRight(),rootView.getPaddingBottom());
        rootView.invalidate();
    }

    public View getContainer() {
        return mContainer;
    }

    private int getPadding(){
        return rootView.getPaddingTop();
    }

    public void setRefresh(boolean isRefresh){
        this.isRefresh=isRefresh;
    }

    public interface MRefreshListener{
        void mRefreshComplete();
    }
}
