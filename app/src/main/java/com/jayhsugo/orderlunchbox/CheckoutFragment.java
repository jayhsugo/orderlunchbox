package com.jayhsugo.orderlunchbox;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
public class CheckoutFragment extends Fragment {

    private List<OrderItem> orderList;
    private TextView tvStorename, tvOrderTotalAmount;
    private Button btnPaymentCheck;
    private HistorySQLiteHelper helper;
    private List<HistoryVO> historyList;
    private String storename;
    private String today;
    private int totalAmount;
    private SharedPreferences todayOrderItemData;
    private AlertDialog dialog;


    public CheckoutFragment() {
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

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_orderitem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new OrderAdapter(inflater));
        if (helper == null) {
            helper = new HistorySQLiteHelper(getActivity());
        }
        getMenuItemAndPrice();
        return view;
    }

    private void getMenuItemAndPrice() {
        orderList = new ArrayList<>();

        todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        String jasonDataString = todayOrderItemData.getString("TODAY_ORDER_ITEM_JSON", "0");

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd" + " E");
        today = sdf.format(new java.util.Date());

        String orderDate = today;

        String orderItem;
        String itemPrice;
        String itemNumber;

        JSONObject j;

        try {
            j = new JSONObject(jasonDataString);
            int jDataLength = j.getJSONArray("orderData").length();
            for (int i = 0; i < jDataLength; i++) {

                orderItem = j.getJSONArray("orderData").getJSONObject(i).getString("orderItem");
                itemPrice = j.getJSONArray("orderData").getJSONObject(i).getString("itemPrice");
                itemNumber = "1";
                orderList.add(new OrderItem(orderDate, orderItem, itemPrice, itemNumber));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnPaymentCheck = getView().findViewById(R.id.btnPaymentCheck);
        tvStorename = getView().findViewById(R.id.tvStorename);
        tvOrderTotalAmount = getView().findViewById(R.id.tvOrderTotalAmount);

        showOrderTotalAmount();

        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        storename = memberData.getString("TODAY_ORDER_STORENAME","未點餐");

        btnPaymentCheck.setOnClickListener(btnPaymentCheckOnClick);
        tvStorename.setText(storename);


        Boolean todayMenuIsChecked = todayOrderItemData.getBoolean("TODAY_MENU_IS_CHECKED", false);

        if (todayMenuIsChecked) {

            showTodayOrderList();
        }
    }

    private void showOrderTotalAmount() {
        totalAmount = 0;
        for(int i = 0; i < orderList.size(); i++){
            int itemPrice = Integer.valueOf(orderList.get(i).getItemPrice().toString()).intValue();
            int itemNumber = Integer.valueOf(orderList.get(i).getItemNumber().toString()).intValue();
            totalAmount = itemPrice * itemNumber + totalAmount;
        }
        tvOrderTotalAmount.setText("總共 " + String.valueOf(totalAmount) + " 元");
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public OrderAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderItem, tvOrderItemPrice, tvItemNumber;
            Button btnNumberUp, btnNumberDown;

            public ViewHolder(View itemView) {
                super(itemView);
                tvOrderItem = itemView.findViewById(R.id.tvOrderItem);
                tvOrderItemPrice = itemView.findViewById(R.id.tvOrderItemPrice);
                tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
                btnNumberUp = itemView.findViewById(R.id.btnNumberUp);
                btnNumberDown = itemView.findViewById(R.id.btnNumberDown);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.recyclerview_orderitem, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            OrderItem orderitem = orderList.get(position);

            viewHolder.tvOrderItem.setText(orderitem.getOrderItem());
            viewHolder.tvOrderItemPrice.setText(orderitem.getItemPrice());
            viewHolder.tvItemNumber.setText(orderitem.getItemNumber());

            viewHolder.btnNumberUp.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 數量+1
                    String itemNumber = viewHolder.tvItemNumber.getText().toString();
                    int intItemNumber = Integer.valueOf(itemNumber).intValue();
                    intItemNumber = intItemNumber + 1;
                    viewHolder.tvItemNumber.setText(String.valueOf(intItemNumber));
                    orderList.get(position).setItemNumber(String.valueOf(intItemNumber));

                    showOrderTotalAmount();
                }
            });

            viewHolder.btnNumberDown.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 數量-1
                    String itemNumber = viewHolder.tvItemNumber.getText().toString();
                    int intItemNumber = Integer.valueOf(itemNumber).intValue();
                    if (intItemNumber > 0) {
                        intItemNumber = intItemNumber - 1;
                    }
                    viewHolder.tvItemNumber.setText(String.valueOf(intItemNumber));
                    orderList.get(position).setItemNumber(String.valueOf(intItemNumber));

                    showOrderTotalAmount();
                }
            });

        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }
    }

    private View.OnClickListener btnPaymentCheckOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (totalAmount == 0){
                Toast.makeText(getActivity(), "總金額為0，不需提交訂單", Toast.LENGTH_SHORT).show();
            } else {
                todayOrderItemData.edit().putString("TODAY_ORDER_TOTAL_AMOUNT", String.valueOf(totalAmount)).apply();
                menuOrderItemSave(false);
                uploadTodayOrderData();
            }
        }

    };

    private void dataSaveToSQLite() {
        // 先清除日期為當天的數據
        long deleteCount = helper.deleteByOrderDate(today);

        historyList = new ArrayList<>();

        if (Integer.valueOf(orderList.size()).intValue() > 0) {
            for (int i = 0; i < orderList.size(); i++) {
                String storeName = storename;
                String orderItem = orderList.get(i).getOrderItem();
                String itemPrice = orderList.get(i).getItemPrice();
                String itemNumber = orderList.get(i).getItemNumber();
                String orderDate = orderList.get(i).getOrderDate();
                historyList.add(new HistoryVO(storeName, orderItem, itemPrice, itemNumber, orderDate));
                long insertCount = helper.insert(historyList.get(i));
            }
        } else {
            Toast.makeText(getActivity(), "No Data", Toast.LENGTH_SHORT).show();
        }

        showTodayOrderList();
    }

    private static class OrderItem {
        String orderDate;
        String orderItem;
        String itemPrice;
        String itemNumber;

        public OrderItem(String orderDate, String orderItem, String itemPrice, String itemNumber) {
            super();
            this.orderDate = orderDate;
            this.orderItem = orderItem;
            this.itemPrice = itemPrice;
            this.itemNumber = itemNumber;
        }

        public String getOrderDate() { return orderDate; }

        public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

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
    }

    public void menuOrderItemSave(boolean isPaid) {

        String isPaidString = null;

        if (isPaid) {
            isPaidString = "";
        } else {
            isPaidString = "(X)";
        }

        // 將用戶名稱從檔案取出並放入OrderItemTotalText
        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String memberName = memberData.getString("MEMBER_NAME", "0");

        String OrderItemTotalText = "";

        // 將被勾選的每一項菜單包裝成json物件，再依序放入json陣列
        for (int i = 0; i < orderList.size(); i++) {
            if (Integer.valueOf(orderList.get(i).getItemNumber()).intValue() > 0) {

                String orderItem = orderList.get(i).getOrderItem();
                String itemPrice = orderList.get(i).getItemPrice();
                String itemNumber = orderList.get(i).getItemNumber();

                OrderItemTotalText = orderItem + "," +
                        itemPrice + "," +
                        itemNumber + "," +
                        isPaidString + memberName + "," +
                        OrderItemTotalText;
            }
        }

        // 將結果已字串形式儲存
        SharedPreferences todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        todayOrderItemData.edit().clear();
        todayOrderItemData.edit().putString("TODAY_ORDER_ITEM_DATA", OrderItemTotalText).apply();
        todayOrderItemData.edit().putBoolean("TODAY_ORDER_ITEM_IS_CHECKED", true).apply();
    }

    public void uploadTodayOrderData() {

        loadingDialog(true);

        // 建立向PHP網頁發出請求的參數網址
        String mUrl = "https://amu741129.000webhostapp.com/member_order_data_insert.php";
        String parameterUrl = null;

        SharedPreferences todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        String todayOrderItemDataString = todayOrderItemData.getString("TODAY_ORDER_ITEM_DATA", "0");

        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new java.util.Date());
        Log.d("MyLog", "todayDate:" + todayDate);
        try {
            parameterUrl = mUrl +
                    "?userid=" + memberData.getString("MEMBER_USERID", "0") +
                    "&groupcode=" + memberData.getString("MEMBER_GROUPCODE", "0") +
                    "&todayorderitemdata=" + URLEncoder.encode(todayOrderItemDataString, "UTF-8") +
                    "&todaydate=" + todayDate;
            Log.d("MyLog", "parameterUrl:" + parameterUrl);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestQueue mQueue;
        StringRequest getRequest;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        dataSaveToSQLite();
                        loadingDialog(false);

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

    private void showTodayOrderList() {

        todayOrderItemData.edit().putBoolean("TODAY_MENU_IS_CHECKED", true).apply();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new OrderListFragment(), null)
                .addToBackStack(null)
                .commit();
    }


//    public void updateOrderItem(String aaa) {
//
//        tvOrderTotalAmount.setText(aaa);
//
//        getMenuItemAndPrice(aaa);
//
//    }
}
