package com.example.draglayputview.weight;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 自定义侧滑控件
 * Created by admin on 2017/1/2.
 */

public class DragLayout extends FrameLayout {
    private static final String TAG = DragLayout.class.getSimpleName();

    private ViewDragHelper mViewDragHelper;


    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;

    private int mWidth;
    private int mHeight;
    private int mRange;
    private Status mStatus = Status.CLOSE;//当前状态

    private OnDragStatusChangeListener mListener;

    public Status getStatus() {
        return mStatus;
    }

    public enum Status {
        CLOSE, OPEN, DRAGING;
    }

    public void setOnDragStatusChangeListener(OnDragStatusChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnDragStatusChangeListener {
        void onOpen();

        void onClose();

        void onDraging(float percent);
    }


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        //初始化，听过静态操作
        mViewDragHelper = ViewDragHelper.create(this, mCallback);
    }


    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        /**
         * 判断当前View是否可以被拖拽
         * @param child 可以被拖拽的view
         * @param pointerId 以被拖拽的view的id
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;//表示所有的子Viewd都可以被拖拽
        }

        /**
         * 根据建议值修正将要移动到的位置(左右滑动)
         * @param child 当前拖拽的view
         * @param left 新的位置的建议值
         * @param dx 位置的变化值
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainContent) {
                left = fixLeft(left);
            }
            return left;
        }

        /**
         * 根据建议值修正将要移动到的位置(上下滑动)
         * @param child
         * @param top
         * @param dy
         * @return
         */
//        @Override
//        public int clampViewPositionVertical(View child, int top, int dy) {
//            return top;
//        }
        @Override
        public int getViewHorizontalDragRange(View child) {
            //设置拖拽的范围，但不对拖拽进行真正的限制，仅仅决定了动画的执行的速度（目前来看可有可无）
            return mRange;
        }

        //当View位置改变时，处理一系列要做的事情（更新状态、动画、重绘界面等等）
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            int newLeft = left;
            Log.i(TAG, "left:" + left + "dx:" + dx);
            if (changedView == mLeftContent) {

                newLeft = mMainContent.getLeft() + dx;
            }
            newLeft = fixLeft(newLeft);
            //更新状态、执行动画
            dispatchDragEvent(newLeft);
            Log.i(TAG, "mMainContent.getLeft():" + mMainContent.getLeft() + "newLeft:" + newLeft);
            if (changedView == mLeftContent) {
                //强制使左边的布局不能被滑动，由于是向左滑动，因此dx为负值
                mLeftContent.layout(0, 0, mWidth, mHeight);
                Log.i(TAG, "mWidth:" + mWidth + "mHeight:" + mHeight);
                mMainContent.layout(newLeft, 0, mWidth + newLeft, mHeight);
            }


            invalidate();//针对2.x版本时无法滑动的问题
        }

        /**
         * 当View被释放时，处理的事情（执行动画）
         * @param releasedChild 被释放的View
         * @param xvel X轴上的速度
         * @param yvel Y轴上的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel == 0 && mMainContent.getLeft() > mRange / 2.0f) {
                open();
            } else if (xvel > 0) {
                open();
            } else {
                close();
            }
        }
    };

    private void dispatchDragEvent(int newLeft) {
        float percent = newLeft * 1.0f / mRange;//获取当前距离的百分比
        if (mListener != null) {
            mListener.onDraging(percent);
        }
        Status status = mStatus;
        mStatus = updateStatus(percent);
        if (mStatus != status) {
            if (mStatus == Status.CLOSE) {
                if (mListener != null) {
                    mListener.onClose();
                }
            } else if (mStatus == Status.OPEN) {
                if (mListener != null) {
                    mListener.onOpen();
                }
            }
        }
        animViews(percent);
    }

    private Status updateStatus(float percent) {
        if (percent == 0f) {
            return Status.CLOSE;
        } else if (percent == 1.0f) {
            return Status.OPEN;
        }
        return Status.DRAGING;
    }

    /**
     * 动画的实现
     *
     * @param percent
     */
    private void animViews(float percent) {
        //左边的布局的动画实现，setScaleX支持android 3.0以上版本，如要兼容3.0以下版本则可以去下载Jake Wharton写的nineoldandroids.jar来进行兼容
        //缩放的实现
        mLeftContent.setScaleX(evaluate(percent, 0.5f, 1.0f));
        mLeftContent.setScaleY(evaluate(percent, 0.5f, 1.0f));
        //平移的是实现
        mLeftContent.setTranslationX(evaluate(percent, -mWidth / 2.0f, 0));
        //透明度
        mLeftContent.setAlpha(evaluate(percent, 0.5f, 1.0f));
        //主布局的动画实现
        //缩放的实现
        mMainContent.setScaleX(evaluate(percent, 1.0f, 0.8f));
        mMainContent.setScaleY(evaluate(percent, 1.0f, 0.8f));
        //背景动画、颜色变化
        getBackground().setColorFilter((Integer) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    private Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 颜色值的过渡变化
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    private Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    public void open() {
        open(true);
    }

    public void close() {
        close(true);
    }

    /**
     * 关闭
     */
    private void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            //触发平滑动画，返回true代表移动到指定的位置，需要刷新界面
            if (mViewDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
                //刷新界面，传递的参数是当前child所在的ViewGroup
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }

    /**
     * 开启
     */

    private void open(boolean isSmooth) {
        int finalLeft = mRange;
        if (isSmooth) {
            //触发平滑动画，返回true代表移动到指定的位置，需要刷新界面
            if (mViewDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
                //刷新界面，传递的参数是当前child所在的ViewGroup
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }


    /**
     * 根据返回值修正主布局移动的距离
     *
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if (left < 0) {
            left = 0;
        } else if (left > mRange) {
            left = mRange;
        }
        return left;
    }


    /**
     *
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        //持续平滑的动画（高频率的调用）
        if (mViewDragHelper.continueSettling(true)) {
            //返回true则继续刷新界面
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //传递给mViewDragHelper来执行触摸操作
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //传递给mViewDragHelper来执行
        mViewDragHelper.processTouchEvent(event);
        //持续接受事件
        return true;
    }

    //表示已经加载完所以得子布局了
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("至少有两个子布局，Your ViewGroup must have 2 children at least.");
        }
        if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("子View必须是ViewGroup得子类，Your children must be an instanceof of ViewGroup");
        }
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();//求出屏幕的宽度
        mHeight = getMeasuredHeight();//求出屏幕的高度

        mRange = (int) (mWidth * 0.6f);
    }
}
