package com.jayhsugo.orderlunchbox;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class TotalAmountFragment extends Fragment {

    private RequestQueue mQueue;
    private StringRequest getRequest;
    private String groupCode, storeName, storeDataJson;
    private SharedPreferences memberData;
    private TextView tvStoreName, tvStorePhone, tvStoreRequirement, tvReJson;
    private Button btnRefresh;

    private List<TotalAmountVO> totalAmountList;
    private TotalAmountSQLiteHelper helper;

    private AlertDialog dialog;

    private View view;


    public TotalAmountFragment() {
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

        loadingDialog(true);

        view = inflater.inflate(R.layout.fragment_total_amount, container, false);

        if (helper == null) {
            helper = new TotalAmountSQLiteHelper(getActivity());
        }

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0");
        storeName = memberData.getString("TODAY_ORDER_STORENAME", "0");
        Log.d("MyLog", "groupCode:"+groupCode+"storeName:"+storeName);

        getTodayOrderStoreData(groupCode, storeName);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvStoreName = getView().findViewById(R.id.tvStoreName);
        tvStorePhone = getView().findViewById(R.id.tvStorePhone);
        tvStoreRequirement = getView().findViewById(R.id.tvStoreRequirement);
        btnRefresh = getView().findViewById(R.id.btnRefresh);
    }

    private void getTodayOrderStoreData(final String groupCode, String storeName) {
        String mUrl = "https://amu741129.000webhostapp.com/get_today_order_store_data.php";

        String url = null;
        try {
            url = mUrl +
                    "?groupcode=" + groupCode +
                    "&storename=" + URLEncoder.encode(storeName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        memberData.edit().putString("TODAY_ORDER_STORE_DATA", s).apply();
                        Log.d("MyLog", "TotalAmountFragment_TODAY_ORDER_STORE_DATA:" + s);
                        getTotalMemberOrderList(groupCode);
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

    private void getTotalMemberOrderList(String groupCode) {
        String mUrl = "https://amu741129.000webhostapp.com/get_total_member_order_list.php";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new java.util.Date());

        String url = mUrl +
                "?groupcode=" + groupCode +
                "&todaydate=" + todayDate;

            getRequest = new StringRequest(url,
                    new Response.Listener<String>() {
                        @Override
                       public void onResponse(String s) {
                            Log.d("MyLog", "getTotalMemberOrderList_onResponse:" + s);
                            if (s.equals("-1")) {
//                                memberData.edit().putBoolean("TOTAL_MEMBER_ORDER_LIST_IS_EXIST", false).apply();
                                // 先清除日期為當天的數據
                                long deleteCount = helper.deleteAll();
                                Log.d("MyLog", "deleteCount:" + String.valueOf(deleteCount));
                                showTodayTotalAmountList();
                            } else {
//                                memberData.edit().putBoolean("TOTAL_MEMBER_ORDER_LIST_IS_EXIST", true).apply();
                                saveTotalMemberOrderList(s);
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

    private void saveTotalMemberOrderList(String jsonString) {

        JSONObject j;
        String orderDataBeforeSplit = "";
        try {
            j = new JSONObject(jsonString);
           int jDataLength = j.getJSONArray("data").length();
            Log.d("MyLog", "jDataLength:" + String.valueOf(jDataLength));
            // 將所有會員資料存成一個大字串
            for (int i = 0; i < jDataLength; i++) {
                orderDataBeforeSplit = j.getJSONArray("data").getJSONObject(i).getString("0") + orderDataBeforeSplit;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // 利用逗號分割字串取得資料，並存到orderDataList陣列
        List<OrderItem> orderDataList = new ArrayList<>();
        String[] orderDataAfterSplit = orderDataBeforeSplit.split(",");

        for (int i = 0; i < orderDataAfterSplit.length; i = i + 4) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItem(orderDataAfterSplit[i]);
            orderItem.setItemPrice(orderDataAfterSplit[i+1]);
            orderItem.setItemNumber(orderDataAfterSplit[i+2]);
            orderItem.setMemberName(orderDataAfterSplit[i+3]);
            orderDataList.add(orderItem);
        }

        // 將orderDataList存入SQLite資料庫
        // 先清除日期為當天的數據
        long deleteCount = helper.deleteAll();
        Log.d("MyLog", "deleteCount:" + String.valueOf(deleteCount));

        totalAmountList = new ArrayList<>();
        if (orderDataList.size() > 0) {
            for (int i = 0; i < orderDataList.size(); i++) {
                String orderItem = orderDataList.get(i).getOrderItem();
                String itemPrice = orderDataList.get(i).getItemPrice();
                String itemNumber = orderDataList.get(i).getItemNumber();
                String memberName = orderDataList.get(i).getMemberName();
                totalAmountList.add(new TotalAmountVO(orderItem, itemPrice, itemNumber, memberName + "(" + itemNumber + ")" + "\n"));
                long insertCount = helper.insert(totalAmountList.get(i));
            }
        } else {
            Toast.makeText(getActivity(), "No Data", Toast.LENGTH_SHORT).show();
        }

        showTodayTotalAmountList();
    }

    private void showTodayTotalAmountList() {

        loadingDialog(false);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new TotalAmountListFragment(), null)
                .addToBackStack(null)
                .commit();
    }

    private static class OrderItem {
        String orderItem;
        String itemPrice;
        String itemNumber;
        String memberName;

        public OrderItem(String orderItem, String itemPrice, String itemNumber, String memberName) {
            super();
            this.orderItem = orderItem;
            this.itemPrice = itemPrice;
            this.itemNumber = itemNumber;
            this.memberName = memberName;
        }

        public OrderItem() {}

        public String getOrderItem() {
            return orderItem;
        }

        public void setOrderItem(String orderItem) {
            this.orderItem = orderItem;
        }

        public String getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(String itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getItemNumber() {
            return itemNumber;
        }

        public void setItemNumber(String itemNumber) {
            this.itemNumber = itemNumber;
        }

        public String getMemberName() { return memberName; }

        public void setMemberName(String memberName) { this.memberName = memberName; }
    }



}
