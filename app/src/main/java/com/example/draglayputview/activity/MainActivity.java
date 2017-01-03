package com.example.draglayputview.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.draglayputview.R;
import com.example.draglayputview.weight.DragLayout;
import com.example.draglayputview.weight.MyLinearLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DragLayout mDragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView mLeftList = (ListView) findViewById(R.id.lv_left);
        final ListView mMainList = (ListView) findViewById(R.id.lv_main);
        final ImageView mHeaderImage = (ImageView) findViewById(R.id.iv_header);
        MyLinearLayout mLinearLayout = (MyLinearLayout) findViewById(R.id.mll);

        mDragLayout = (DragLayout) this.findViewById(R.id.activity_main);

        // 设置引用
        mLinearLayout.setDraglayout(mDragLayout);
        mDragLayout.setOnDragStatusChangeListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onOpen() {
                Toast.makeText(MainActivity.this, "侧滑打开了", Toast.LENGTH_SHORT).show();
                // 左面板ListView随机设置一个条目
                Random random = new Random();

                int nextInt = random.nextInt(50);
                mLeftList.smoothScrollToPosition(nextInt);
            }

            @Override
            public void onClose() {
                Toast.makeText(MainActivity.this, "侧滑关闭了", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDraging(float percent) {
                Log.i(TAG, "percent:" + percent);
                mHeaderImage.setAlpha(1 - percent);
            }
        });
        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = ((TextView)view);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });

        mMainList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));

    }

}
