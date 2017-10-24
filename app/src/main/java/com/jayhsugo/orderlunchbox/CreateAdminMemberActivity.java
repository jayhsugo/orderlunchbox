package com.jayhsugo.orderlunchbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.UUID;

public class CreateAdminMemberActivity extends AppCompatActivity {
    private EditText etUsername, etGroupCode, etGroupName, etGroupPassword;
    private String userName, groupCode, groupName, groupPassword;
    private String userAdmin = "1" ; // 設定會員權限為1，代表為管理員

    private RequestQueue mQueue;
    private final static String urlInsert = "https://amu741129.000webhostapp.com/admin_member_data_insert.php";
    private final static String urlUpdate = "https://amu741129.000webhostapp.com/admin_member_data_update.php";
    private StringRequest getRequest;
    private static String userId;
    private AlertDialog dialog;
    private SharedPreferences memberData;

    private void loadingDialog(boolean isShow) {

        if (isShow) {
            View view = View.inflate(this, R.layout.loading_dialog, null);
            dialog = new AlertDialog.Builder(this)
                    .setTitle("資料載入中...")
                    .setView(view)
                    .setCancelable(false)
                    .create();
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin_member);
        findViews();
    }

    private void findViews() {
        etUsername = (EditText) findViewById(R.id.etUserName);
        etUsername.addTextChangedListener(new MaxTextLengthWatcher(6)); // 限制字串的字元長度=6
        
        etGroupCode = (EditText) findViewById(R.id.etGroupCode);
        etGroupName = (EditText) findViewById(R.id.etGroupName);
        etGroupPassword = (EditText) findViewById(R.id.etGroupPassword);

        Intent intent = getIntent();
        Boolean b = intent.getBooleanExtra("EditData", false);
        if (b) {
            etGroupCode.setEnabled(false);
        }

        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        userId = memberData.getString("MEMBER_USERID", "0");

        if (!userId.equals("0")) {
            etUsername.setText(memberData.getString("MEMBER_NAME", "0"));
            etGroupCode.setText(memberData.getString("MEMBER_GROUPCODE", "0"));
            etGroupName.setText(memberData.getString("MEMBER_GROUPNAME", "0"));
            etGroupPassword.setText(memberData.getString("MEMBER_GROUPPASSWORD", "0"));
        } else {
            userId = UUID.randomUUID().toString(); // 本機登入帳號
        }


    }

    public void onBtnSendClick(View view) {

        userName = etUsername.getText().toString().trim();
        groupCode = etGroupCode.getText().toString().trim();
        groupName = etGroupName.getText().toString().trim();
        groupPassword = etGroupPassword.getText().toString().trim();
        Boolean isInputValid = true;

        if (userName.isEmpty()) {
            String text = "請輸入暱稱!";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
            isInputValid = false;
        } else if (userName.indexOf(",") != -1) {
            String text = "暱稱不可含有逗號\",\"";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
            isInputValid = false;
        } else if (groupCode.isEmpty()) {
            String text = "請輸入群組代碼!";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
            isInputValid = false;
        } else if (groupName.isEmpty()) {
            String text = "請輸入群組名稱!";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
            isInputValid = false;
        } else if (groupPassword.isEmpty()) {
            String text = "請輸入群組密碼!";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
            isInputValid = false;
        }

        if (!isInputValid) {
            String text = "錯誤";
            Toast.makeText(CreateAdminMemberActivity.this, text, Toast.LENGTH_SHORT).show();
        } else {
            Member member = new Member();
            member.setUserid(userId);
            member.setUserName(userName);
            member.setUserAdmin(userAdmin);
            member.setGroupCode(groupCode);
            member.setGroupName(groupName);
            member.setGroupPassword(groupPassword);

            memberDataInsert(member);

        }
    }

    public void memberDataInsert(Member member) {
        loadingDialog(true);
        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl = null;

        String mUrl;
        memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        if (memberData.getString("MEMBER_USERID", "0").equals(userId)) {
            mUrl = urlUpdate;
        } else {
            mUrl = urlInsert;
        }

        try {
            parameterUrl = mUrl +
                    "?userid=" + member.getUserid() +
                    "&username=" + URLEncoder.encode(member.getUserName(), "UTF-8") +
                    "&useradmin=" + member.getUserAdmin() +
                    "&groupcode=" + member.getGroupCode() +
                    "&groupname=" + URLEncoder.encode(member.getGroupName(), "UTF-8") +
                    "&grouppassword=" + member.getGroupPassword();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(this);
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        Log.d("MyLog", "CerateAdminMemberActivity_memberDataInsert_Response:" + s);
                        memberDataResult(s.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loadingDialog(false);
                        Toast.makeText(CreateAdminMemberActivity.this, "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(getRequest);

    }

    public void memberDataResult(String s) {
        // 解讀伺服器回傳訊息代碼
        String resultMsg = null;
        if (s.equals("1")) {
            resultMsg = "群組代號已存在，請重新輸入!\n若要加入該群組成為共同管理員\n需輸入正確密碼";
        } else if (s.equals("2")) {
            resultMsg = "新增為群組共同管理員";
            memberDataSave();
        } else if (s.equals("3")) {
            resultMsg = "新增成功";
            memberDataSave();
        } else if (s.equals("5")) {
            resultMsg = "更新成功";
            memberDataSave();
        } else if (s.equals("6")) {
            resultMsg = "群組代碼已存在，請重新輸入";
        } else if (s.equals("7")) {
            resultMsg = "更新成功"; // 群組代碼有更新，必須清除原本相關資料

            memberDataSave();
        }
        Toast.makeText(CreateAdminMemberActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

    }

    public void getStoreArrangeList(String groupCode) {
        loadingDialog(true);
        String url = "https://amu741129.000webhostapp.com/get_store_arrange_list.php?groupcode=" + groupCode;

        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        memberData.edit().putString("STORE_NAME_ARRANGE_LIST", s).apply();
                        Log.d("MyLog", "CreateAdminMemberActivity_STORE_NAME_ARRANGE_LIST:" + s);
                        if (s.equals("0")) {
                            goToNextPage();
                        } else {
                            getTodayOrderStoreName(s);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loadingDialog(false);
                        Toast.makeText(CreateAdminMemberActivity.this, "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
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
        goToNextPage();
    }

    public void memberDataSave() {
        SharedPreferences memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        memberData.edit()
                .putString("MEMBER_USERID", userId)
                .putString("MEMBER_NAME", userName)
                .putString("MEMBER_USERADMIN", userAdmin)
                .putString("MEMBER_GROUPCODE", groupCode)
                .putString("MEMBER_GROUPNAME", groupName)
                .putString("MEMBER_GROUPPASSWORD", groupPassword)
                .apply();

        getStoreArrangeList(groupCode);
    }

    private void goToNextPage() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.putExtra("isMember", true);
        startActivity(intent);
        finish();
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
