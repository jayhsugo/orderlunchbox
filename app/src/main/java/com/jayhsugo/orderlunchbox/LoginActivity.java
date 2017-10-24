package com.jayhsugo.orderlunchbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;

public class LoginActivity extends AppCompatActivity {
    private RadioGroup radioGroupMemberRank;
    private Button btnLoginByUUID;
    private RequestQueue mQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        radioGroupMemberRank = (RadioGroup) findViewById(R.id.radioGroupMemberRank);
        btnLoginByUUID = (Button) findViewById(R.id.btnLoginByUUID);

    }

    public void onBtnLoginByUUIDClick(View view) {

        if (radioGroupMemberRank.getCheckedRadioButtonId() == R.id.radioButtonMember) {
            Intent intent = new Intent();
            intent.putExtra("EditData", false);
            intent.setClass(this, CreateMemberActivity.class);
            startActivity(intent);
            finish();
        } else if (radioGroupMemberRank.getCheckedRadioButtonId() == R.id.radioButtonAdmin) {
            Intent intent = new Intent();
            intent.putExtra("EditData", false);
            intent.setClass(this, CreateAdminMemberActivity.class);
            startActivity(intent);
            finish();
        } else {
            String text = "請先選擇身份別";
            Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
        }

    }
}
