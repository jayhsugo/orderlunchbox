package com.jayhsugo.orderlunchbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class WellcomeActivity extends AppCompatActivity {

    private RequestQueue mQueue;
    private StringRequest getRequest;
    private String result, isAdmin, memberUserId, memberGroupCode;
    SharedPreferences memberData, menuTodayJasonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);

        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        memberUserId = memberData.getString("MEMBER_USERID", "0"); // 如果沒會員檔案則取的字串0
        memberGroupCode = memberData.getString("MEMBER_GROUPCODE", "0"); // 如果沒會員檔案則取的字串0

        Toast.makeText(WellcomeActivity.this, memberGroupCode, Toast.LENGTH_SHORT).show();

        getUserIdIsExistFromSever(memberUserId);
    }

    public void getUserIdIsExistFromSever(String userId) {
        String url = "https://amu741129.000webhostapp.com/get_user_id_is_exist.php?userid=" + userId;

        mQueue = new Volley().newRequestQueue(this);
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        result = s.toString();

                        getTodayOrderStoreName(memberGroupCode);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        mQueue.add(getRequest);
    }

    public void getTodayOrderStoreName(String groupcode) {
        String url = "https://amu741129.000webhostapp.com/get_today_order_storename.php?groupcode=" + groupcode;

        mQueue = new Volley().newRequestQueue(this);
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(WellcomeActivity.this, s, Toast.LENGTH_LONG).show();
                        memberData.edit().putString("TODAY_ORDER_STORENAME", s).apply();
                        getTodayMenuItemAndPrice();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        mQueue.add(getRequest);
    }


    private void getTodayMenuItemAndPrice() {
        String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
        menuTodayJasonData = getSharedPreferences("menu_today", MODE_PRIVATE);
        menuTodayJasonData.edit().clear(); // 先清除檔案內容
        String groupcode = memberData.getString("MEMBER_GROUPCODE", "0");
        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl;
        parameterUrl = urlGetMenu + "?groupcode=" + groupcode;
        mQueue = new Volley().newRequestQueue(this);
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
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        mQueue.add(getRequest);
    }

    public void goToNextPage() {

        // 如果未建立過會員資料則開啟登入介面
        if (result.equals("0")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            isAdmin = memberData.getString("MEMBER_USERADMIN", "0");
            if (isAdmin.equals("0")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
