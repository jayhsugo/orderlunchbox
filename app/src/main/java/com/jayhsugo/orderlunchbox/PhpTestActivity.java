package com.jayhsugo.orderlunchbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PhpTestActivity extends AppCompatActivity {

    private RequestQueue mQueue;
    private final static String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
    private TextView tvMsg;
    private StringRequest getRequest;
    SharedPreferences memberData, menuToday;
    private List<Menu> menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_php_test);
        tvMsg = (TextView) findViewById(R.id.tvMsg);
        menuList = new ArrayList<>();

//        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
//        String groupcode = memberData.getString("MEMBER_GROUPCODE", "0");
        menuToday = getSharedPreferences("menu_today", MODE_PRIVATE);
        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl;
        parameterUrl = urlGetMenu + "?groupcode=" + "Celsiatw";
        mQueue = new Volley().newRequestQueue(this);
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        JSONObject j;
                        String text = null;
                        String menuitem = null;
                        String itemprice = null;
                        try {
                            j = new JSONObject(s);
                            int jDataLength = j.getJSONArray("data").length();
                            for (int i = 0; i < jDataLength; i++) {
                                menuitem = j.getJSONArray("data").getJSONObject(i).getString("menuitem");
                                itemprice = j.getJSONArray("data").getJSONObject(i).getString("itemprice");
                                menuToday.edit().putString(menuitem, itemprice).apply();
                            }
//                            Map<String, ?> allEntries = menuToday.getAll();
//                            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//                                menuList.add(new Menu(entry.getKey(), entry.getValue().toString()));
//                            }
//                            text = menuList.get(0).getMenuItem() + " " + menuList.get(0).getItemPrice();
//                            tvMsg.setText(text);
                              tvMsg.setText("OK");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            tvMsg.setText("Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        mQueue.add(getRequest);
    }

//    public class MyJson {
//        @SerializedName("menuitem")
//        private String menuitem;
//        @SerializedName("itemprice")
//        private String itemprice;
//
//        public String getMenuitem() {
//            return menuitem;
//        }
//
//        public void setMenuitem(String menuitem) {
//            this.menuitem = menuitem;
//        }
//
//        public String getItemprice() {
//            return itemprice;
//        }
//
//        public void setItemprice(String itemprice) {
//            this.itemprice = itemprice;
//        }
//    }


}
