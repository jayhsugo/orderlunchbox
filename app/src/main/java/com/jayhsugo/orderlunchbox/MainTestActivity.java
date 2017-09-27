package com.jayhsugo.orderlunchbox;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainTestActivity extends AppCompatActivity implements Page1Fragment.CallbackInterface {
    private static final int TAB_COUNT = 4;
    private MyPagerAdapter myPagerAdapter;
    private ViewPager myViewPager;
    private List<Fragment> fragments = null;

    private MenuFragment menuFragment;
    private CheckoutFragment checkFragment;
    private TextView tvCheckout2;

    public Fragment fragResult;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


        myViewPager = (ViewPager) findViewById(R.id.myViewPager);

        fragments = new ArrayList<Fragment>();
        fragments.add(new Page1Fragment());
        fragments.add(new Page2Fragment());
        fragments.add(new HistoryFragment());
        fragments.add(new MemberDataFragment());
        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);

        myViewPager.setAdapter(myPagerAdapter);
        myPagerAdapter.notifyDataSetChanged();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(myViewPager);





    }

    public MyPagerAdapter getAdapter() {

        return myPagerAdapter;

    }


    @Override
    public void updateOrderItem(String aaa) {

        ((Page2Fragment) fragResult).updateOrderItem(aaa);
        myPagerAdapter.notifyDataSetChanged();
    }

}

