package com.jayhsugo.orderlunchbox;


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

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArrangeReadyStoreFragment extends Fragment {

    private Button btnReArrangeStore;
    private TableLayout tableLayoutDateStore;
    private List<String> storeList, dateList, dateWithYearList, storeNameArrangeList;
    private SharedPreferences memberData;
    private String groupCode;
    private AlertDialog dialog;


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

//        loadingDialog(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arrange_ready_store, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("MyLog", "ArrangeReadyStoreFragment_onActivityCreated");

        btnReArrangeStore = getView().findViewById(R.id.btnReArrangeStore);
        btnReArrangeStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳轉到ArrangeStoreFragment
               goToNextPage();
            }
        });

        tableLayoutDateStore = getView().findViewById(R.id.tableLayoutDateStore);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);

        String storeNameArrangeListData = memberData.getString("STORE_NAME_ARRANGE_LIST", "0");

        showDateStoreByTable(storeNameArrangeListData);

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

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, MP, 1);
                tv.setLayoutParams(layoutParams);
                tv.setGravity(Gravity.CENTER);
                tableRow2.addView(tv);
                j = j + 1;

            }
            tableLayoutDateStore.addView(tableRow2, new TableLayout.LayoutParams(WC, WC, 1));


//        loadingDialog(false);
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

        public StoreItem() {

        }

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
