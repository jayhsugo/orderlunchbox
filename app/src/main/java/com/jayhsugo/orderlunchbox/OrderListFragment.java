package com.jayhsugo.orderlunchbox;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
public class OrderListFragment extends Fragment {


    private List<OrderItem> orderDataList;

    private TextView tvNoticeMsg;
    private ToggleButton tbtnPaymentNotice;
    private RecyclerView recyclerView;
    private String text;
    private View view;
    private List<OrderItem> orderList;
    private TextView tvStorename, tvOrderTotalAmount;
    private String today, totalAmount;
    private SharedPreferences todayOrderItemData;
    private Intent intent;
    private Boolean todayIsPaid, payRemindServiceIsOpen;
    private AlertDialog dialog;



    public OrderListFragment() {
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
        if (view == null)
        view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_orderlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new OrderAdapter(inflater));

        todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);

        getOrderItemAndPrice();
        getMenuItemAndPrice();
        return view;
    }

    private void getOrderItemAndPrice() {
        String todayOrderDataStringBeforeSplit = todayOrderItemData.getString("TODAY_ORDER_ITEM_DATA", "0");

        // 利用逗號分割字串取得資料，並存到orderDataList陣列
        orderDataList = new ArrayList<>();
        String[] todayOrderDataAfterSplit = todayOrderDataStringBeforeSplit.split(",");
        String newText = "";
        for (int i = 0; i < todayOrderDataAfterSplit.length; i = i + 4) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItem(todayOrderDataAfterSplit[i]);
            orderItem.setItemPrice(todayOrderDataAfterSplit[i+1]);
            orderItem.setItemNumber(todayOrderDataAfterSplit[i+2]);
            orderDataList.add(orderItem);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tbtnPaymentNotice = getView().findViewById(R.id.tbtnPaymentNotice);
        tvNoticeMsg = getView().findViewById(R.id.tvNoticeMsg);
        tvStorename = getView().findViewById(R.id.tvStorename);
        tvOrderTotalAmount = getView().findViewById(R.id.tvOrderTotalAmount);
        totalAmount = todayOrderItemData.getString("TODAY_ORDER_TOTAL_AMOUNT", "0");
        tvOrderTotalAmount.setText("總共 " + totalAmount + " 元");

        todayIsPaid = todayOrderItemData.getBoolean("TODAY_IS_PAID", false);
        String msgText = null;
        if (todayIsPaid) {
            tbtnPaymentNotice.setChecked(false);
            tbtnPaymentNotice.setText("繳費提醒已關閉");
            msgText = "感謝您!<br>" +
                    "祝用餐愉快~";
        } else {
            tbtnPaymentNotice.setChecked(true);
            tbtnPaymentNotice.setText("繳費提醒開啟中");
            msgText = "訂單送出成功!<br>" +
                    "<font color='#FF0000'>每15分鐘</font>會推播提醒繳費<br>" +
                    "請盡快繳費給訂餐負責人，謝謝!<br><br>" +
                    "如果已繳費完成<br>" +
                    "請點擊右下按鈕關閉通知";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvNoticeMsg.setText(Html.fromHtml(msgText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvNoticeMsg.setText(Html.fromHtml(msgText));
        }

        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String storename = memberData.getString("TODAY_ORDER_STORENAME","0");

        tbtnPaymentNotice.setOnClickListener(tbtnPaymentNoticeOnClick);
        tvStorename.setText(storename);

        // 推播提醒繳費通知服務
        payRemindService();
    }

    private void payRemindService() {

        todayIsPaid = todayOrderItemData.getBoolean("TODAY_IS_PAID", false);
        payRemindServiceIsOpen = todayOrderItemData.getBoolean("SERVICE_IS_OPEN", false);
        intent = new Intent(getActivity(), LongRunningService.class);
        intent.putExtra("totalAmount", totalAmount);
        Log.d("MyLog", "totalAmount:"+totalAmount);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.d("MyLog", "todayIsPaid:"+String.valueOf(todayIsPaid));
        Log.d("MyLog", "payRemindServiceIsOpen:"+String.valueOf(payRemindServiceIsOpen));

        if (todayIsPaid) {
            if (payRemindServiceIsOpen) {
                // 關閉Service
                getActivity().stopService(intent);
                todayOrderItemData.edit().putBoolean("SERVICE_IS_OPEN", false).apply();
            }
        } else {
            if (!payRemindServiceIsOpen) {
                // 開啟Service
                getActivity().startService(intent);

                todayOrderItemData.edit().putBoolean("SERVICE_IS_OPEN", true).apply();
            }
        }
    }


    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public OrderAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderItem, tvOrderItemPrice, tvItemNumber;


            public ViewHolder(View itemView) {
                super(itemView);
                tvOrderItem = itemView.findViewById(R.id.tvOrderItem);
                tvOrderItemPrice = itemView.findViewById(R.id.tvOrderItemPrice);
                tvItemNumber = itemView.findViewById(R.id.tvItemNumber);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.recyclerview_orderlist, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            OrderItem orderitem = orderDataList.get(position);

            viewHolder.tvOrderItem.setText(orderitem.getOrderItem());
            viewHolder.tvOrderItemPrice.setText(orderitem.getItemPrice() + "元");
            viewHolder.tvItemNumber.setText("x" + orderitem.getItemNumber());


        }

        @Override
        public int getItemCount() {
            return orderDataList.size();
        }
    }

    private View.OnClickListener tbtnPaymentNoticeOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (tbtnPaymentNotice.isChecked()) {

                tbtnPaymentNotice.setText("繳費提醒開啟中");

                String msgText = "訂單送出成功!<br>" +
                        "<font color='#FF0000'>每15分鐘</font>會推播提醒繳費<br>" +
                        "請盡快繳費給訂餐負責人，謝謝!<br><br>" +
                        "如果已繳費完成<br>" +
                        "請點擊右下按鈕關閉通知";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvNoticeMsg.setText(Html.fromHtml(msgText,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvNoticeMsg.setText(Html.fromHtml(msgText));
                }

                todayOrderItemData.edit().putBoolean("TODAY_IS_PAID", false).apply();
                menuOrderItemSave(false);

            } else {

                tbtnPaymentNotice.setText("繳費提醒已關閉");

                String msgText = "感謝您!<br>" + "祝用餐愉快~";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvNoticeMsg.setText(Html.fromHtml(msgText,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvNoticeMsg.setText(Html.fromHtml(msgText));
                }

                todayOrderItemData.edit().putBoolean("TODAY_IS_PAID", true).apply();
                menuOrderItemSave(true);
            }

            uploadTodayOrderData();

            // 推播提醒繳費通知服務
            payRemindService();
        }

    };

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

        public OrderItem() {

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

    private void getMenuItemAndPrice() {
        orderList = new ArrayList<>();

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
        todayOrderItemData.edit().putString("TODAY_ORDER_ITEM_DATA", OrderItemTotalText).apply();
    }

    public void uploadTodayOrderData() {
        loadingDialog(true);
        // 建立向PHP網頁發出請求的參數網址
        String mUrl = "https://amu741129.000webhostapp.com/member_order_data_insert.php";
        String parameterUrl = null;

        String todayOrderItemDataString = todayOrderItemData.getString("TODAY_ORDER_ITEM_DATA", "0");

        SharedPreferences memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new java.util.Date());

        try {
            parameterUrl = mUrl +
                    "?userid=" + memberData.getString("MEMBER_USERID", "0") +
                    "&groupcode=" + memberData.getString("MEMBER_GROUPCODE", "0") +
                    "&todayorderitemdata=" + URLEncoder.encode(todayOrderItemDataString, "UTF-8") +
                    "&todaydate=" + todayDate;
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

                        if (s.equals("1")) {
                            Toast.makeText(getActivity(), "資料已更新", Toast.LENGTH_SHORT).show();
                        } else if (s.equals("2")) {
                            Toast.makeText(getActivity(), "資料已更新", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "未知狀態碼:" + s, Toast.LENGTH_SHORT).show();
                        }
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
}
