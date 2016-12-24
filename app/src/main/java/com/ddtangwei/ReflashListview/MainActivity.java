package com.ddtangwei.ReflashListview;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ReflashListview mListView;
    private ArrayList<String> mDatas;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mDatas = new ArrayList<>();
        mListView = (ReflashListview) findViewById(R.id.listview);

        mListView.setRefreshListener(new ReflashListview.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mDatas.add(0,"我是下拉刷新出来的哦");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                mListView.onRefreshComplete();
                            }
                        });
                    }
                }){}.start();
            }
        });

        for (int i = 0;i < 30;i++){
            mDatas.add("这是第"+(i+1)+"条listview数据");
        }
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);


    }


    private class MyAdapter extends BaseAdapter{
            @Override
            public int getCount() {
                return mDatas.size();
            }

            @Override
            public String getItem(int i) {
                return mDatas.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                TextView textView = new TextView(viewGroup.getContext());
                textView.setTextSize(20f);
                textView.setText(mDatas.get(i));
                return textView;
            }
    }
}
