package com.jayhsugo.orderlunchbox;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArrangeReadyStoreFragment extends Fragment {

    private Button btnReArrangeStore;
    private TableLayout tableLayoutDateStore;
    private List<String> storeList, storeNotExistList;
    private SharedPreferences memberData;
    private String groupCode, storeNameArrangeListData;
    private AlertDialog dialog;
    private RequestQueue mQueue;
    private StringRequest getRequest;


    public ArrangeReadyStoreFragment() {
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

        Log.d("MyLog", "ArrangeReadyStoreFragment_onCreateView");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arrange_ready_store, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("MyLog", "ArrangeReadyStoreFragment_onActivityCreated");

        tableLayoutDateStore = getView().findViewById(R.id.tableLayoutDateStore);
        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0");
        storeNameArrangeListData = memberData.getString("STORE_NAME_ARRANGE_LIST", "0");

        btnReArrangeStore = getView().findViewById(R.id.btnReArrangeStore);
        btnReArrangeStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArrangeStoreListFromServer();
            }
        });

        getStoreNameListDataFromServer(groupCode);
    }

    private void getStoreNameListDataFromServer(String groupCode) {

        loadingDialog(true);
        String url = "https://amu741129.000webhostapp.com/get_store_list.php?groupcode=" + groupCode;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        if (s.equals("-1")) {
                            deleteArrangeStoreListFromServer();
                        } else {
                            getStoreNameList(s);
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

    private void getStoreNameList(String jsonData) {

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

        showDateStoreByTable(storeNameArrangeListData);
    }

    private void deleteArrangeStoreListFromServer() {
        loadingDialog(true);

        String url = "https://amu741129.000webhostapp.com/storename_arrange_list_data_delete.php?groupcode=" + groupCode;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                            loadingDialog(false);
                            memberData.edit().putString("STORE_NAME_ARRANGE_LIST", "0").apply();
                            // 跳轉到ArrangeStoreFragment
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

        memberData.edit().putString("STORE_NAME_ARRANGE_LIST", "0").apply();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new ArrangeStoreFragment(), null)
                .addToBackStack(null)
                .commit();
    }

    private void showDateStoreByTable(String storeNameArrangeListData) {

        storeNotExistList = new ArrayList<>();

        // 利用逗號分割字串取得資料，並存到storeArrangeList陣列
        List<StoreItem> storeArrangeList = new ArrayList<>();
        String[] storeArrangeAfterSplit = storeNameArrangeListData.split(",");
        for (int i = 0; i < storeArrangeAfterSplit.length; i = i + 2) {
            StoreItem storeItem = new StoreItem();
            storeItem.setDate(storeArrangeAfterSplit[i]);
            storeItem.setStoreName(storeArrangeAfterSplit[i + 1]);
            storeArrangeList.add(storeItem);
        }

        tableLayoutDateStore.removeAllViews();

        final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
        final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

        for (int r = 0; r < 2; r++) {

            TableRow tableRow1 = new TableRow(getActivity());

            int i = 7 * r;
            // 設定第一天~第七天的日期
            for (int c = 0; c < 7; c++) {
                TextView tv = new TextView(getActivity());
                // 將日期格式為 "yyyy-MM-DD週X" 轉換成 "MM-DD\n週X" ，以利於顯示於表單中
                String dateFormate = storeArrangeList.get(i).getDate().toString();
                dateFormate = dateFormate.substring(5, 10) + "\n" + dateFormate.substring(dateFormate.length() - 2);
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
                tableRow1.addView(tv);
                i = i + 1;
            }
            tableLayoutDateStore.addView(tableRow1, new TableLayout.LayoutParams(WC, WC)); // (W, H, Weight)

            TableRow tableRow2 = new TableRow(getActivity());

            int j = 7 * r;
            // 設定第一天~第七天的店家名稱
            for (int c = 0; c < 7; c++) {
                final TextView tv = new TextView(getActivity());

                String storeName = storeArrangeList.get(j).getStoreName().toString();
                String textV = getTextVertical(storeName);
                tv.setText(textV);
                tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                tv.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.listBackground));

                // 檢查若排程中的店家有不存在店家列表，將其標示成紅色
                boolean b = false;
                for (int s = 0; s < storeList.size(); s++ ) {
                    if (storeName.equals(storeList.get(s).toString())) {
                        b = true;
                    }
                }
                if (!b && !(storeName.equals("X"))) {
                    tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
                    String date = storeArrangeList.get(j).getDate().toString();
                    String store = storeArrangeList.get(j).getStoreName().toString();
                    storeNotExistList.add(date + " " + store);
                }

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, MP, 1);
                layoutParams.setMargins(5, 5, 5, 10);
                tv.setLayoutParams(layoutParams);
                tv.setGravity(Gravity.CENTER);
                tableRow2.addView(tv);
                j = j + 1;

            }
            tableLayoutDateStore.addView(tableRow2, new TableLayout.LayoutParams(WC, WC, 1));
        }

        if (storeNotExistList.size() > 0) {
            List<String> storeNameList = new ArrayList<>();
            for (int i = 0; i < storeNotExistList.size(); i++) {
                storeNameList.add(storeNotExistList.get(i).toString());
            }

            dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("紅字標示店家無資料\n請新增店家或重新安排，謝謝!")
                    .setItems(storeNameList.toArray(new String[storeNameList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("重新安排", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteArrangeStoreListFromServer();
                        }
                    })
                    .setNegativeButton("稍待", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setCancelable(true)
                    .show();
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        }
    }

    private String getTextVertical(String s) {
        String[] AfterSplit = s.split("");
        String newText = "";
        for (int i = 0; i < AfterSplit.length; i++) {
            newText = newText + AfterSplit[i] + "\n";
        }
        return newText;
    }

    private static class StoreItem {
        String date;
        String storeName;

        public StoreItem(String date, String storeName) {
            super();
            this.date = date;
            this.storeName = storeName;
        }

        public StoreItem() { }

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
