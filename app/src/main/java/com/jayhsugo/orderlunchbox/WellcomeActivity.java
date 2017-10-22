package com.jayhsugo.orderlunchbox;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WellcomeActivity extends AppCompatActivity {

    private RequestQueue mQueue;
    private StringRequest getRequest;
    private String result, isAdmin, userId, groupCode;
    private SharedPreferences memberData, menuTodayJasonData;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);

        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        userId = memberData.getString("MEMBER_USERID", "0"); // 如果沒會員檔案則取的字串0
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0"); // 如果沒會員檔案則取的字串0

        if (!userId.equals("0")) {
            getUserIdIsExistFromSever(userId);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNextPage();
                }
            }, 3000);

        }
    }

    private void serverBusyDialog() {
        dialog = new AlertDialog.Builder(WellcomeActivity.this)
                .setTitle("伺服器忙碌中，請稍候再試，謝謝!")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }


    public void getUserIdIsExistFromSever(String userId) {
        String url = "https://amu741129.000webhostapp.com/get_user_id_is_exist.php?userid=" + userId;

        mQueue = new Volley().newRequestQueue(this);
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        Log.d("MyLog", "WellcomeActivity_getUserIdIsExistFromSever_onResponse:" + s);

                        result = s.toString();
                        if (result.equals("1")) {
                            getStoreArrangeList(groupCode);
                        } else {
                            goToNextPage();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        serverBusyDialog();
                    }
                });
        mQueue.add(getRequest);
    }

    public void getStoreArrangeList(String groupCode) {
        String url = "https://amu741129.000webhostapp.com/get_store_arrange_list.php?groupcode=" + groupCode;
        Log.d("MyLog", "WellcomeActivity_getStoreArrangeList_url:" + url);
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("MyLog", "WellcomeActivity_getStoreArrangeList_onResponse:" + s);
                        if (!s.equals("0")) {
                            memberData.edit().putString("STORE_NAME_ARRANGE_LIST", s).apply();
                            getTodayOrderStoreName(s);
                        } else {
                            memberData.edit().putString("STORE_NAME_ARRANGE_LIST", s).apply();
                            goToNextPage();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        serverBusyDialog();
                    }
                });
        mQueue.add(getRequest);
    }

    public void getTodayOrderStoreName(String storeArrangeListData) {

        // 利用逗號分割字串取得資料，並存到storeArrangeList陣列
        List<StoreItem> storeArrangeList = new ArrayList<>();
        String[] storeArrangeAfterSplit = storeArrangeListData.split(",");
        for (int i = 0; i < storeArrangeAfterSplit.length; i = i + 2) {
            StoreItem storeItem = new StoreItem();
            storeItem.setDate(storeArrangeAfterSplit[i]);
            storeItem.setStoreName(storeArrangeAfterSplit[i+1]);
            storeArrangeList.add(storeItem);
        }

        // 從storeArrangeList當中找出今日訂購店家
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 0);
        Date day = calendar.getTime();
        CharSequence today = DateFormat.format("yyyy-MM-dd", day);

        String storeNameToday = null;
        for (int i = 0; i < storeArrangeList.size(); i++) {

            if (storeArrangeList.get(i).getDate().toString().indexOf(today.toString()) != -1) {
                storeNameToday = storeArrangeList.get(i).getStoreName().toString();
            }
        }

        memberData.edit().putString("TODAY_ORDER_STORENAME", storeNameToday).apply();
        getTodayMenuItemAndPrice(storeNameToday);
    }

    private void getTodayMenuItemAndPrice(String storeNameToday) {
        String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
        menuTodayJasonData = getSharedPreferences("menu_today", MODE_PRIVATE);
        menuTodayJasonData.edit().clear(); // 先清除檔案內容

        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl = null;
        try {
            parameterUrl = urlGetMenu +
                    "?groupcode=" + groupCode +
                    "&storenametoday=" + URLEncoder.encode(storeNameToday, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        menuTodayJasonData.edit().putString("menuTodayJasonData", s).apply();
                        goToNextPage();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        serverBusyDialog();
                    }
                });
        mQueue.add(getRequest);
    }

    public void goToNextPage() {

        // 如果未建立過會員資料則開啟登入介面
        if (userId.equals("0")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            isAdmin = memberData.getString("MEMBER_USERADMIN", "0");
            if (isAdmin.equals("0")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, AdminMainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private static class StoreItem {
        String date;
        String storeName;

        public StoreItem(String date, String storeName) {
            super();
            this.date = date;
            this.storeName = storeName;
        }

        public StoreItem() {}

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
    }
}
