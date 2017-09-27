package com.jayhsugo.orderlunchbox;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberDataFragment extends Fragment {
    private TextView tvUsername, tvGroupCode, tvGroupName;
    private Button btnCopyGroupCode, btnEditMemberData, btnGoToAdminMain;
    private String isAdmin; // 0代表一般會員，1代表管理員



    public MemberDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_member_data, container, false);


        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 必須先呼叫getView()取得程式畫面物件，然後才能呼叫它的findViewByIs()取得介面物件
        tvUsername = getView().findViewById(R.id.tvUserName);
        tvGroupCode = getView().findViewById(R.id.tvGroupCode);
        tvGroupName = getView().findViewById(R.id.tvGroupName);

        btnCopyGroupCode = getView().findViewById(R.id.btnCopyGroupCode);
        btnEditMemberData = getView().findViewById(R.id.btnEditMemberData);
        btnGoToAdminMain = getView().findViewById(R.id.btnGoToAdminMain);

        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        tvUsername.setText(memberData.getString("MEMBER_NAME", "0"));
        tvGroupCode.setText(memberData.getString("MEMBER_GROUPCODE", "0"));
        tvGroupName.setText(memberData.getString("MEMBER_GROUPNAME", "0"));

        // 判斷若為管理員身份則顯示切換會員身份按鈕
        isAdmin = memberData.getString("MEMBER_USERADMIN", "0");
        String act = getActivity().toString(); // 判斷目前所在介面

        if (isAdmin.equals("1")) {
            btnGoToAdminMain.setVisibility(View.VISIBLE);
            if (act.contains("Admin")) {
                btnEditMemberData.setVisibility(View.VISIBLE);
                btnGoToAdminMain.setText("切換至一般訂購人介面");
            } else {
                btnEditMemberData.setVisibility(View.INVISIBLE);
                btnGoToAdminMain.setText("切換至訂購負責人介面");
            }
        } else {
            if (!act.contains("Admin")) {
                btnEditMemberData.setVisibility(View.VISIBLE);
            }
        }

        btnEditMemberData.setOnClickListener(btnEditMemberDataOnClick);
        btnCopyGroupCode.setOnClickListener(btnCopyGroupCodeOnClick);
        btnGoToAdminMain.setOnClickListener(btnGoToAdminMainOnClick);



    }

    private View.OnClickListener btnEditMemberDataOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (isAdmin.equals("1")) {
                Intent intentMemberRank = new Intent();
                intentMemberRank.setClass(getActivity(), CreateAdminMemberActivity.class);
                startActivity(intentMemberRank);
            } else {
                Intent intentMemberRank = new Intent();
                intentMemberRank.setClass(getActivity(), CreateMemberActivity.class);
                startActivity(intentMemberRank);
            }
        }
    };

    private View.OnClickListener btnGoToAdminMainOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            String act = getActivity().toString(); // 判斷目前所在介面
            Toast.makeText(getActivity(),act,Toast.LENGTH_SHORT).show();
            if (act.contains("Admin")) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AdminMainActivity.class);
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener btnCopyGroupCodeOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            copyToClipboard(tvGroupCode.getText().toString());
            Toast.makeText(getActivity(),"已複製完成",Toast.LENGTH_SHORT).show();
        }
    };

    private void copyToClipboard(String str){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(str);
            Log.e("version","1 version");
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",str);
            clipboard.setPrimaryClip(clip);
            Log.e("version","2 version");
        }
    }

}
