package com.jayhsugo.orderlunchbox;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArrangeStoreFragment extends Fragment {
    private RequestQueue mQueue;
    private StringRequest getRequest;
    private TextView tvArrangeNoticeMsg;
    private Button btnArrangeStoreByRandom, btnSendStoreList;
    private CheckBox cbWeekdays, cbSaturday, cbSunday;
    private TableLayout tableLayoutDateStore;
    private List<String> storeList, dateList, storeNameArrangeList;
    private SharedPreferences memberData;
    private String groupCode;
    private AlertDialog dialog;
    private TotalAmountSQLiteHelper helper;

    public ArrangeStoreFragment() {
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
        Log.d("MyLog", "ArrangeStoreFragment_onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arrange_store, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("MyLog", "ArrangeStoreFragment_onActivityCreated");

        tvArrangeNoticeMsg = getView().findViewById(R.id.tvArrangeNoticeMsg);
        tvArrangeNoticeMsg.setText("請按下隨機安排紐，自動生成本週與下週訂購店家\n" +
                "如需手動更改，請直接點擊指定日期店家即可！");

        cbWeekdays = getView().findViewById(R.id.cbWeekdays);
        cbSaturday = getView().findViewById(R.id.cbSaturday);
        cbSunday = getView().findViewById(R.id.cbSunday);

        cbWeekdays.setOnCheckedChangeListener(checkedChangeListener);
        cbSaturday.setOnCheckedChangeListener(checkedChangeListener);
        cbSunday.setOnCheckedChangeListener(checkedChangeListener);

        btnArrangeStoreByRandom = getView().findViewById(R.id.btnArrangeStoreByRandom);

        btnArrangeStoreByRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 製作表格
                showDateStoreByTable();
            }
        });

        btnSendStoreList = getView().findViewById(R.id.btnSendStoreList);
        btnSendStoreList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storeNameArrangeList.isEmpty()) {
                    Toast.makeText(getActivity(), "尚未安排店家!", Toast.LENGTH_SHORT).show();
                } else {
                    getStoreNameArrangeListData();
                }
            }
        });

        tableLayoutDateStore = getView().findViewById(R.id.tableLayoutDateStore);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0");

        String storeNameArrangeListData = memberData.getString("STORE_NAME_ARRANGE_LIST", "0");
        Log.d("MyLog", "ArrangeStoreFragment_STORE_NAME_ARRANGE_LIST:" + storeNameArrangeListData);

        if (!storeNameArrangeListData.equals("0")) {
            goToNextPage();
        } else {
            // 讀取伺服器中群組所擁有的店家名稱列表，並存入陣列
            getStoreListJsonFromServer(groupCode);
            // 製作從今天算起兩週的日期陣列
            getDateListFromSystem();
        }

    }

    private void goToEditStoreNameListFragment() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("尚未建立菜單")
                .setMessage("請先至店家編輯頁面新增店家")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AdminMainActivity adminMainActivity = (AdminMainActivity) getActivity();
                        adminMainActivity.initBody(2);

                    }
                })
                .setCancelable(false)
                .show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
    }

    private void getStoreNameArrangeListData() {

        String storeNameArrangeListData = "";
        for (int i = 0; i < 14; i++) {
            storeNameArrangeListData = storeNameArrangeListData + dateList.get(i).toString() + "," + storeNameArrangeList.get(i).toString() + ",";
        }

//        memberData.edit().putBoolean("ARRANGE_LIST_IS_CHANGED", true).apply();
        memberData.edit().putString("STORE_NAME_ARRANGE_LIST", storeNameArrangeListData).apply();
        memberData.edit().putString("TODAY_ORDER_STORENAME", storeNameArrangeList.get(0).toString()).apply();

        uploadStoreArrangeData(storeNameArrangeListData);
    }

    private void uploadStoreArrangeData(String storeNameArrangeListData) {
        loadingDialog(true);

        String mUrl = "https://amu741129.000webhostapp.com/storename_arrange_list_data_insert.php";

        String parameterUrl = null;
        try {
            parameterUrl = mUrl +
                    "?groupcode=" + groupCode +
                    "&storename_arrange_list_data=" + URLEncoder.encode(storeNameArrangeListData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        checkTodayMemberOrderListDelete();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後重試", Toast.LENGTH_SHORT).show();
                        loadingDialog(false);
                    }
                });
        mQueue.add(getRequest);
    }

    private void checkTodayMemberOrderListDelete() {
        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("是否需要清空今日會員訂購菜單?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTodayMemberOrderListDataFromServer();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToNextPage();
                    }
                })
                .setCancelable(true)
                .show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
    }

    private void deleteTodayMemberOrderListDataFromServer() {
        loadingDialog(true);

        String url = "https://amu741129.000webhostapp.com/total_member_order_list_delete.php?groupcode=" + groupCode;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        helper = new TotalAmountSQLiteHelper(getActivity());
                        helper.deleteAll();
                        // 跳轉到ArrangeReadyStoreFragment
                        goToNextPage();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後重試", Toast.LENGTH_SHORT).show();
                        loadingDialog(false);
                    }
                });
        mQueue.add(getRequest);
    }

    private void goToNextPage() {

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new ArrangeReadyStoreFragment(), null)
                .addToBackStack(null)
                .commit();
    }

    private CheckBox.OnCheckedChangeListener checkedChangeListener = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showDateStoreByTable();
        }
    };

    private void getDateListFromSystem() {
        dateList = new ArrayList<>();

        for (int i = 0; i < 14; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, i);
            Date day = calendar.getTime();
            CharSequence s = DateFormat.format("yyyy-MM-dd" + "E", day);
            dateList.add(s.toString());
        }
    }

    private void getStoreListJsonFromServer(String groupCode) {
        loadingDialog(true);

        String url = "https://amu741129.000webhostapp.com/get_store_list.php?groupcode=" + groupCode;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        if (s.equals("-1")) {
                            goToEditStoreNameListFragment();
                        } else {
                            // 將回傳的店家名稱從Json資料型態解析存成字串陣列
                            getStoreListFromJson(s);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後重試", Toast.LENGTH_SHORT).show();
                        loadingDialog(false);
                    }
                });
        mQueue.add(getRequest);
    }

    private void getStoreListFromJson(String jsonData) {
        storeList = new ArrayList<>();
        JSONObject j;
        String storeName;

        try {
            j = new JSONObject(jsonData);
            int jDataLength = j.getJSONArray("data").length();
            for (int i = 0; i < jDataLength; i++) {
                storeName = j.getJSONArray("data").getJSONObject(i).getString("storename");
                storeList.add(storeName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 製作表格
        showDateStoreByTable();
    }

    private void showDateStoreByTable() {
        storeNameArrangeList = new ArrayList<>();

        tableLayoutDateStore.removeAllViews();

        final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
        final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 宣告一個長度＝14，預設值為false的布林陣列
        // 此陣列是用來儲存兩週=14天當中，當天是否須納入菜單排列
        List<Boolean> dayShouldBeArranged = new ArrayList<>();
        for ( int i = 0; i < 14; i++) {
            dayShouldBeArranged.add(false);
        }

        // 分別檢查三個checkbox，若有勾選則將當天設定為true
        String weekdays[] = {"週一", "週二", "週三", "週四", "週五"};
        String saturday = "週六";
        String sunday = "週日";
        for ( int i = 0; i < 14; i++) {

            if (cbWeekdays.isChecked()) {
                for (int j = 0; j < 5; j++) {
                    if (dateList.get(i).toString().indexOf(weekdays[j]) != -1) {
                        dayShouldBeArranged.set(i, true);
                    }
                }
            }

            if (cbSaturday.isChecked()) {
                if (dateList.get(i).toString().indexOf(saturday) != -1) {
                    dayShouldBeArranged.set(i, true);
                }
            }

            if (cbSunday.isChecked()) {
                if (dateList.get(i).toString().indexOf(sunday) != -1) {
                    dayShouldBeArranged.set(i, true);
                }
            }
        }


        int x = 0;
        int[] n = getRandomNumber(storeList.size());

        for(int r=0; r<2; r++) {

            TableRow tableRow1=new TableRow(getActivity());
//            tableRow.setWeightSum(7);

            int i = 7 * r;
            // 設定第一天~第七天的日期
            for(int c=0; c<7; c++) {
                TextView tv=new TextView(getActivity());
                // 將日期格式為 "yyyy-MM-DD週X" 轉換成 "MM-DD\n週X" ，以利於顯示於表單中
                String dateFormate = dateList.get(i).toString();
                dateFormate = dateFormate.substring(5,10) + "\n" + dateFormate.substring(dateFormate.length()-2);
                tv.setText(dateFormate);

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, MP, 1);
                layoutParams.setMargins(1, 0, 1, 0);
                tv.setLayoutParams(layoutParams);
                tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.columnTitleText));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setMaxLines(2);
                String text = tv.getText().toString();
                if ((text.indexOf("週六") != -1) || (text.indexOf("週日") != -1)) {
                    tv.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                } else {
                    tv.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                }

                if (dayShouldBeArranged.get(i)) {
                    tableRow1.addView(tv);
                }
                i = i + 1;
            }
            tableLayoutDateStore.addView(tableRow1, new TableLayout.LayoutParams(WC, WC)); // (W, H, Weight)

            TableRow tableRow2=new TableRow(getActivity());

            int j = 7 * r;
            // 設定第一天~第七天的店家名稱
            for(int c=0; c<7; c++) {
                final TextView tv=new TextView(getActivity());
                tv.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.listBackground));

                final int finalJ = j;
                tv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        changeStore(tv, finalJ); // 呼叫改變店名的方法，並傳入(TextView) tv
                    }
                });

                String storeName = null;
                if (dayShouldBeArranged.get(j)) {
                    if (x < n.length) {
                        storeName = storeList.get(n[x]).toString();
                        String textV = getTextVertical(storeName);
                        tv.setText(textV);
                    } else {
                        x = 0;
                        storeName = storeList.get(n[x]).toString();
                        String textV = getTextVertical(storeName);
                        tv.setText(textV);
                    }
                    x = x + 1;
                } else {
                    storeName = "X";
                    tv.setText(storeName);
                }

                storeNameArrangeList.add(storeName);

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, MP, 1);
                layoutParams.setMargins(5, 5, 5, 10);
                tv.setLayoutParams(layoutParams);
                tv.setGravity(Gravity.CENTER);

                if (dayShouldBeArranged.get(j)) {
                    tableRow2.addView(tv);
                }
                j = j + 1;

            }
            tableLayoutDateStore.addView(tableRow2, new TableLayout.LayoutParams(WC, WC, 1));
        }

        loadingDialog(false);

    }

    private String getTextVertical(String s) {
        String[] AfterSplit = s.split("");
        String newText = "";
        for (int i = 0; i < AfterSplit.length; i++) {
            newText = newText + AfterSplit[i] + "\n";
        }
        return newText;
    }

    private void changeStore(final TextView tv, final int position) {
        // 暫時增加一個空白選項給storeList
        storeList.add("X");
        final String[] name = {null};
        new AlertDialog.Builder(getActivity())
                .setTitle("店家列表")
                .setItems(storeList.toArray(new String[storeList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String storeName = null;
                        if (which == storeList.size()-1) {
                            storeName = "X";
                            String textV = storeName;
                            tv.setText(textV);
                        } else {
                            name[0] = storeList.get(which).toString();
                            storeName = name[0];
                            String textV = getTextVertical(storeName);
                            tv.setText(textV);
                        }
                        storeNameArrangeList.set(position, storeName);

                        // 使用完畢再將空白選項刪除
                        storeList.remove(storeList.size()-1);
                    }
                })
                .show();
    }

    private int[] getRandomNumber(int range) {
        // 先製作一個範圍是range的整數陣列
        int[] num = new int[range];

        // 將整數從0~(num.length)依序放入陣列
        for(int i = 0; i < num.length; i++) {
            num[i] = i;
        }

        // 再另外製作一個範圍大小一樣的整數陣列
        int[] arr = new int[range];
        int n;

        // 進行兩個陣列的內容對換已獲得一個隨機的整數陣列
        for(int i = 0; i < arr.length; i++) {
            // 先隨機抽出num[]陣列
            n = (int)(Math.random()*(range-i));
            // 將值存入依序存入新陣列
            arr[i] = num[n];
            // 以被抽出的num[]為基準，將後面的值往前面遞補一位
            for(int j = n; j < num.length - 1; j++)
            {
                num[j] = num[j+1];
            }
        }

        // 回傳一個內容大小為range，但順序已變成隨機整數的陣列
        return arr;
    }
}
