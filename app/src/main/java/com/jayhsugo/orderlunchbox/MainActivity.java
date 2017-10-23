package com.jayhsugo.orderlunchbox;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {

    private TabLayout.Tab tab0, tab1, tab2, tab3;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActBarDrawerToggle;
    private SharedPreferences memberData;
    private String isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        isAdmin = memberData.getString("MEMBER_USERADMIN", "0"); // 0代表一般會員，1代表管理員

        if (isAdmin.equals("1")) {
            // 設定側開式選單。
            ActionBar actBar = getSupportActionBar();
            actBar.setDisplayHomeAsUpEnabled(true);
            actBar.setHomeButtonEnabled(true);

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
            mActBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);
            mActBarDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.addDrawerListener(mActBarDrawerToggle);

            ListView listView = (ListView) findViewById(R.id.listView);
            ArrayAdapter<CharSequence> arrAdapWeekday =
                    ArrayAdapter.createFromResource(this, R.array.mode,
                            android.R.layout.simple_list_item_1);
            listView.setAdapter(arrAdapWeekday);
            listView.setOnItemClickListener(listViewOnItemClick);
        }


        //設定隱藏標題
        //getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tab0 = tabLayout.newTab().setText(R.string.text_menu);
        tab1 = tabLayout.newTab().setText(R.string.text_checkout);
        tab2 = tabLayout.newTab().setText(R.string.text_history);
        tab3 = tabLayout.newTab().setText(R.string.text_member);
        tabLayout.addTab(tab0);
        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);
        tabLayout.addTab(tab3);


        initBody(tabLayout.getSelectedTabPosition());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                initBody(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
        if (isAdmin.equals("1")) {
            mActBarDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 要先把選單的項目傳給 ActionBarDrawerToggle 處理。
        // 如果它回傳 true，表示處理完成，不需要再繼續往下處理。
        if (mActBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener listViewOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {

            if (position == 0) {
                // gotoAdminmain
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), AdminMainActivity.class);
                startActivity(intent);
            } else {
                // gotomain
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            mDrawerLayout.closeDrawers();
        }
    };

    public void initBody(int position) {
        Fragment fragment;
        switch (position) {

            case 0:
                fragment = new MenuFragment();
                switchFragment(fragment);
                setTitle(R.string.text_menu);
                tab0.select();
                break;
            case 1:
                fragment = new CheckoutFragment();
                switchFragment(fragment);
                setTitle(R.string.text_checkout);
                tab1.select();
                break;
            case 2:
                fragment = new HistoryFragment();
                switchFragment(fragment);
                setTitle(R.string.text_history);
                tab2.select();
                break;
            case 3:
                fragment = new MemberDataFragment();
                switchFragment(fragment);
                setTitle(R.string.text_member);
                tab3.select();
                break;
            default:

                break;
        }
    }

    public void switchFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        return true;
    }

    public void tabSelect(int i) {
        switch (i) {
            case 0:
                tab0.select();
                break;
            case 1:
                tab1.select();
                break;
            case 2:
                tab2.select();
                break;
            case 3:
                tab3.select();
                break;
            default:

                break;
        }
    }

}

