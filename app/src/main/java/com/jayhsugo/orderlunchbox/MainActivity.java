package com.jayhsugo.orderlunchbox;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int TAB_COUNT = 4;
    private MyPagerAdapter myPagerAdapter;
    private ViewPager myViewPager;
    private List<Fragment> fragments = null;

    private MenuFragment menuFragment;
    private CheckoutFragment checkFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        menuFragment = new MenuFragment();
        checkFragment = new CheckoutFragment();

        myViewPager = (ViewPager) findViewById(R.id.myViewPager);

        fragments = new ArrayList<Fragment>();
        fragments.add(new MenuFragment());
        fragments.add(new CheckoutFragment());
        fragments.add(new HistoryFragment());
        fragments.add(new MemberDataFragment());
        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
//        myPagerAdapter.setOnReloadListener(new OnReloadListener() {
//            @Override
//            public void onReload() {
//                fragments = null;
//                List<Fragment> list = new ArrayList<Fragment>();
//                list.add(new MenuFragment());
//                list.add(new CheckoutFragment());
//                list.add(new HistoryFragment());
//                list.add(new MemberDataFragment());
//                myPagerAdapter.setPagerItems(list);
//            }
//        });
        myViewPager.setAdapter(myPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(myViewPager);


    }

    public MyPagerAdapter getAdapter() {
        return myPagerAdapter;
    }



}

