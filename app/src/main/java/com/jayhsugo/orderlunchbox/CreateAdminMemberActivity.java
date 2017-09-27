package com.jayhsugo.orderlunchbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.UUID;

public class CreateAdminMemberActivity extends AppCompatActivity {
    private EditText etUsername, etGroupCode, etGroupName, etGroupPassword;
    private String userName, groupCode, groupName, groupPassword;
    private String userAdmin = "1" ; // 設定會員權限為1，代表為管理員

    private RequestQueue mQueue;
    private final static String urlInsert = "https://amu741129.000webhostapp.com/admin_member_data_insert.php";
    private final static String urlUpdate = "https://amu741129.000webhostapp.com/admin_member_data_update.php";
    private StringRequest getRequest;
    private final static String userId = UUID.randomUUID().toString(); // 本機登入帳號

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin_member);
        findViews();
    }

    private void findViews() {
        etUsername = (EditText) findViewById(R.id.etUserName);
        etGroupCode = (EditText) findViewById(R.id.etGroupCode);
        etGroupName = (EditText) findViewById(R.id.etGroupName);
        etGroupPassword = (EditText) findViewById(R.id.etGroupPassword);

        SharedPreferences memberData = getSharedPreferences("member_data", MODE_PRIVATE);
        if (memberData.getString("MEMBER_USERID", "0").equals(userId)) {
            etUsername.setText(memberData.getString("MEMBER_NAME", "0"));
            etGroupCode.setText(memberData.getString("MEMBER_GROUPCODE", "0"));
            etGroupName.setText(memberData.getString("MEMBER_GROUPNAME", "0"));
            etGroupPassword.setText(memberData.getString("MEMBER_GROUPPASSWORD", "0"));
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
        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl = null;

        String mUrl;
        SharedPreferences memberData = getSharedPreferences("member_data", MODE_PRIVATE);
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
                        memberDataResult(s.toString());
                        Toast.makeText(CreateAdminMemberActivity.this, s.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        mQueue.add(getRequest);

    }

    public void memberDataResult(String s) {
        // 解讀伺服器回傳訊息代碼
        String resultMsg;
        if (s.equals("1")) {
            resultMsg = "以群組代號已存在，請重新輸入!";
        } else if (s.equals("3")) {
            resultMsg = "新增成功";
            memberDataSave();
        } else if (s.equals("4")) {
            resultMsg = "暫時無法建立";
        } else if (s.equals("5")) {
            resultMsg = "資料更新成功";
            memberDataSave();
        } else if (s.equals("6")) {
            resultMsg = "群組代碼已存在，請重新輸入";
        } else if (s.equals("7")) {
            resultMsg = "資料更新成功";
            memberDataSave();
        } else {
            resultMsg = "未知狀態";
        }
        Toast.makeText(CreateAdminMemberActivity.this, resultMsg, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.putExtra("isMember", true);
        startActivity(intent);
        finish();
    }


}
