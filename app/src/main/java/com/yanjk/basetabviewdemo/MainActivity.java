package com.yanjk.basetabviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yanjk.basetabviewdemo.databinding.ActivityMainBinding;
import com.yanjkcode.ytabview.OnTabCheckedListener;
import com.yanjkcode.ytabview.TabAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        initTab1();
        initTab2();
    }

    private void initTab1() {
        binding.tabLayout.setTabAdapter(new TabAdapter() {
            @Override
            public void setTabView(View tabView, String tab) {
                tabView.<TextView>findViewById(R.id.tv_tab).setText(tab);
            }

            @Override
            public int getTabViewId() {
                return R.layout.item_tab;
            }

            @Override
            public void tabChecked(View tab) {
//                tab.<TextView>findViewById(R.id.tv_tab).setTextSize(26);
            }

            @Override
            public void lastTabChecked(View lastTab) {
//                lastTab.<TextView>findViewById(R.id.tv_tab).setTextSize(24);
            }

            @Override
            public float getTabScale() {
                return 0.15f;
            }
        });
        ArrayList<String> tabs = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            tabs.add("tab" + i);
        }
        binding.tabLayout.addTab(tabs);

        binding.tabLayout.setOnTabCheckedListener(new OnTabCheckedListener() {
            @Override
            public void onTabChecked(int position) {
                binding.tabIndex.setText(position + "");
            }
        });
    }

    private void initTab2() {
        binding.tabLayout2.setTabAdapter(new TabAdapter() {
            @Override
            public void setTabView(View tabView, String tab) {
                tabView.<TextView>findViewById(R.id.tv_tab).setText(tab);
            }

            @Override
            public int getTabViewId() {
                return R.layout.item_tab;
            }

            @Override
            public void tabChecked(View tab) {
            }

            @Override
            public void lastTabChecked(View lastTab) {
            }

            @Override
            public float getTabScale() {
                return 0;
            }
        });
        ArrayList<String> tabs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            tabs.add("tab" + i);
        }
        binding.tabLayout2.addTab(tabs);

        binding.tabLayout2.setOnTabCheckedListener(new OnTabCheckedListener() {
            @Override
            public void onTabChecked(int position) {
                binding.tabIndex2.setText(position + "");
            }
        });
    }


}