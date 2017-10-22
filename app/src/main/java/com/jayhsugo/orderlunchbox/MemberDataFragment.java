package com.jayhsugo.orderlunchbox;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberDataFragment extends Fragment {

    private TextView tvUsername, tvGroupCode, tvGroupName;
    private Button btnCopyGroupCode, btnEditMemberData, btnGoToAdminMain, btnDeleteMemberData;
    private String isAdmin; // 0代表一般會員，1代表管理員
    private SharedPreferences memberData, menuTodayJasonData, todayOrderItemData;
    private RequestQueue mQueue;
    private StringRequest getRequest;
    private AlertDialog dialog;

    public MemberDataFragment() {
        // Required empty public constructor
    }

    private void loadingDialog(boolean isShow) {

        if (isShow) {
            View view = View.inflate(getActivity(), R.layout.loading_dialog, null);
            dialog = new AlertDialog.Builder(getActivity())
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
        btnDeleteMemberData = getView().findViewById(R.id.btnDeleteMemberData);
        btnGoToAdminMain = getView().findViewById(R.id.btnGoToAdminMain);

        todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        menuTodayJasonData = getActivity().getSharedPreferences("menu_today", MODE_PRIVATE);
        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        tvUsername.setText(memberData.getString("MEMBER_NAME", "0"));
        tvGroupCode.setText(memberData.getString("MEMBER_GROUPCODE", "0"));
        tvGroupName.setText(memberData.getString("MEMBER_GROUPNAME", "0"));

        // 判斷若為管理員身份則顯示切換會員身份按鈕
        isAdmin = memberData.getString("MEMBER_USERADMIN", "0");
        String act = getActivity().toString(); // 判斷目前所在介面

        if (isAdmin.equals("1")) {
            btnGoToAdminMain.setVisibility(View.VISIBLE);
            // 若為管理員身份，則當畫面切回AdminMember時，才顯示編輯及刪除會員資料
            if (act.contains("Admin")) {
                btnEditMemberData.setVisibility(View.VISIBLE);
                btnDeleteMemberData.setVisibility(View.VISIBLE);
                btnGoToAdminMain.setText("切換至一般訂購人介面");
            } else {
                btnEditMemberData.setVisibility(View.INVISIBLE);
                btnDeleteMemberData.setVisibility(View.INVISIBLE);
                btnGoToAdminMain.setText("切換至訂購負責人介面");
            }
        } else {
            if (!act.contains("Admin")) {
                btnEditMemberData.setVisibility(View.VISIBLE);
                btnDeleteMemberData.setVisibility(View.VISIBLE);
            }
        }

        btnEditMemberData.setOnClickListener(btnEditMemberDataOnClick);
        btnCopyGroupCode.setOnClickListener(btnCopyGroupCodeOnClick);
        btnGoToAdminMain.setOnClickListener(btnGoToAdminMainOnClick);
        btnDeleteMemberData.setOnClickListener(btnDeleteMemberDataOnClick);

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

    private View.OnClickListener btnDeleteMemberDataOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("確認視窗")
                    .setMessage("確定要刪除會員資料嗎?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btnDeleteMemberDataFromServer();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setCancelable(true)
                    .show();
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        }
    };

    private void goToWellcomeActivity() {

        Intent intent = new Intent(getActivity(), WellcomeActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void deleteMemberDataFromThisPhone() {
        getActivity().deleteDatabase("HistoryDB");
        getActivity().deleteDatabase("TotalAmountDB");
        todayOrderItemData.edit().clear().apply();
        menuTodayJasonData.edit().clear().apply();
        memberData.edit().clear().apply();

        goToWellcomeActivity();
    }

    private void btnDeleteMemberDataFromServer() {
        loadingDialog(true);
        // 建立向PHP網頁發出請求的參數網址
        String mUrl = "https://amu741129.000webhostapp.com/member_data_delete.php";
        String parameterUrl;
        parameterUrl = mUrl +
                "?userid=" + memberData.getString("MEMBER_USERID", "0");

        Log.d("MyLog", "btnDeleteMemberDataFromServer_parameterUrl" + parameterUrl);

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("MyLog", "btnDeleteMemberDataFromServer_onResponse" + s);
                        loadingDialog(false);
                        deleteMemberDataFromThisPhone();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loadingDialog(false);
                        Log.d("MyLog", "btnDeleteMemberDataFromServer_onResponse" + volleyError.toString());
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(getRequest);
    }

    private View.OnClickListener btnGoToAdminMainOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            String act = getActivity().toString(); // 判斷目前所在介面

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
