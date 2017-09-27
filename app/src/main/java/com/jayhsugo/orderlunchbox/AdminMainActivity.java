package com.jayhsugo.orderlunchbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class AdminMainActivity extends AppCompatActivity {
    boolean logon;
    private ViewPager myViewPager;
    //    private View page1, page2;
    private MyPagerAdapter myPagerAdapter;
//    private List<View> pageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        SharedPreferences memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        String memberDataIsExist = memberData.getString("MEMBER_USERID", "0"); // 如果沒會員檔案則取的字串0
        //Toast.makeText(MainActivity.this, memberDataIsExist, Toast.LENGTH_SHORT).show();

        Intent isMember = getIntent(); // 建立Intent物件取得使用者是否已是會員的身份
        logon = isMember.getBooleanExtra("isMember", false); // 儲存是否已是會員身份的變數

        // 如果未建立過會員資料則開啟登入介面
        if (memberDataIsExist.equals("0")) {
            if (!logon) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        }

        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        myViewPager = (ViewPager) findViewById(R.id.myViewPager);
        myViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(myViewPager);

//        LayoutInflater inflater = getLayoutInflater();



    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
//            pageList = new ArrayList<>();
//            pageList.add(page1);
//            pageList.add(page2);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new MemberDataFragment();
                    break;
                case 1:
                    fragment = new MenuFragment();
                    break;
                case 2:
                    fragment = new CheckoutFragment();
                    break;
                case 3:
                    fragment = new HistoryFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "負責人";
                case 1:
                    return "菜單安排";
                case 2:
                    return "點餐統計";
                case 3:
                    return "店家編輯";
                default:
                    return null;
            }
        }
    }
}
