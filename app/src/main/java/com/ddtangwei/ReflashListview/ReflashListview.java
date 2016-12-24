package com.ddtangwei.ReflashListview;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by ddtangwei on 2016/12/20.
 */

/**
 * 包含下拉刷新的listview
 */
public class ReflashListview extends ListView {

    private View mHeaderView;//头布局
    private int measuredHeight;
    private float downY;
    private float moveY;
    private static final int PULL_TO_REFRESH = 0;//下拉刷新
    private static final int RELEASE_REFRESH = 1;//释放刷新
    private static final int REFRESHING = 2;//刷新中
    private int currentState = PULL_TO_REFRESH;//当前刷新模式
    private RotateAnimation rotateUpAnim;
    private RotateAnimation rotateDownAnim;
    private ImageView iv_arrow;
    private TextView tv_desc_last_refresh;
    private ProgressBar pb;
    private TextView tv_title;
    private int paddingTop;
    private OnRefreshListener mListener;

    public ReflashListview(Context context) {
        super(context);
        init();
    }

    public ReflashListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReflashListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化头布局，脚布局
     * 滚动监听
     */
    private void init() {
        initHeaderView();

        initAnimation();

        initFooterView();
    }

    private void initFooterView() {



    }

    /**
     * 初始化头布局的动画
     */
    private void initAnimation() {

        //向上转，围绕着自己的中心，逆时针180
        rotateUpAnim = new RotateAnimation(
                0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateUpAnim.setDuration(500);
        rotateUpAnim.setFillAfter(true);

        //向下转，围绕着自己的中心，逆时针180
        rotateDownAnim = new RotateAnimation(
                -180f, -360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateUpAnim.setDuration(500);
        rotateUpAnim.setFillAfter(true);
    }

    /**
     * 初始化头布局
     */
    private void initHeaderView() {
        mHeaderView = View.inflate(getContext(), R.layout.layout_header_list, null);
        iv_arrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
        tv_desc_last_refresh = (TextView) mHeaderView.findViewById(R.id.tv_desc_last_refresh);
        tv_title = (TextView) mHeaderView.findViewById(R.id.tv_title);
        pb = (ProgressBar) mHeaderView.findViewById(R.id.pb);

        //提前手动测量宽高
        mHeaderView.measure(0,0);//按照设置的规则测量

        //获取测量后的高度
        measuredHeight = mHeaderView.getMeasuredHeight();

        //设置内边距，可以隐藏当前控件，-自身高度
        mHeaderView.setPadding(0,-measuredHeight,0,0);

        addHeaderView(mHeaderView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        //判断滑动距离，给Header设置paddingTop
        switch (ev.getAction()){

            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY();

                //如果是正在刷新中，就执行父类的处理
                if(currentState == REFRESHING){
                    return super.onTouchEvent(ev);
                }

                float offset = moveY - downY;

                //只有偏移量>0，并且当前第一个可见条目索引是0，才放大头部
                if (offset > 0 && getFirstVisiblePosition() == 0){
                    paddingTop = (int) (-measuredHeight + offset);
                    mHeaderView.setPadding(0, paddingTop,0,0);

                    if (paddingTop >= 0 && currentState != RELEASE_REFRESH){//头布局完全显示
                        //切换成释放刷新模式
                        currentState = RELEASE_REFRESH;

                        //根据最新的状态值更新头布局内容
                        updateHeader();

                    }else if(paddingTop < 0 && currentState != PULL_TO_REFRESH){//头布局不完全显示
                        //切换成下拉刷新模式
                        currentState = PULL_TO_REFRESH;

                        //根据最新的状态值更新头布局内容
                        updateHeader();
                    }

                    return true;//当前事件被我们处理并消费

                }
                break;

            case MotionEvent.ACTION_UP:

                if(paddingTop < 0){
                    mHeaderView.setPadding(0,-measuredHeight,0,0);
                }else {
                    mHeaderView.setPadding(0,0,0,0);
                    currentState = REFRESHING;
                    updateHeader();
                }

                break;

        }
        return super.onTouchEvent(ev);
    }

    private void updateHeader() {

        switch (currentState){
            case PULL_TO_REFRESH:
                // 切换回下拉刷新
                //做动画、改标题
                iv_arrow.startAnimation(rotateDownAnim);
                tv_title.setText("下拉刷新");
                break;

            case RELEASE_REFRESH:
                //切换成释放刷新
                //做动画、改标题
                iv_arrow.startAnimation(rotateUpAnim);
                tv_title.setText("释放刷新");
                break;

            case REFRESHING:

                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(INVISIBLE);
                pb.setVisibility(VISIBLE);
                tv_title.setText("正在刷新中。。。");

                if (mListener != null){
                    //通知调用者，让其到网络加载更多数据
                    mListener.onRefresh();
                }

                break;

            default:
                break;
        }
    }

    /**
     * 刷新结束，恢复界面效果
     */
    public void onRefreshComplete() {

        currentState = PULL_TO_REFRESH;
        tv_title.setText("下拉刷新");
        mHeaderView.setPadding(0,-measuredHeight,0,0);
        pb.setVisibility(INVISIBLE);
        iv_arrow.setVisibility(VISIBLE);
        String time = getTime();
        tv_desc_last_refresh.setText("最后刷新时间："+time);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTime() {

        long timeMillis = System.currentTimeMillis();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        String s = simpleDateFormat.format(timeMillis);
        return s;

    }

    public interface OnRefreshListener{
        void onRefresh();
    }


    public void setRefreshListener(OnRefreshListener mListener){
        this.mListener = mListener;
    }
}
