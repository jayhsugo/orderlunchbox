package com.jayhsugo.orderlunchbox;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Page2Fragment extends Fragment {


    private List<OrderItem> orderList;
    private List<OrderItem> mData;

    private TextView tvStorename2, tvOrderTotalAmount;
    private Button btnPaymentCheck;
    RecyclerView recyclerView;
    SharedPreferences memberData;
    String text;



    public Page2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_orderitem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new OrderAdapter(inflater));

        getMenuItemAndPrice("aaa");
        return view;
    }

    private void getMenuItemAndPrice(String aaa) {
        orderList = new ArrayList<>();
        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
//        String jasonDataString = memberData.getString("TODAY_ORDER_ITEM_JSON", "0");

        String jasonDataString = aaa;
        JSONObject j;
        String orderItem = null;
        String itemPrice = null;
        String itemNumber = null;

        try {
            j = new JSONObject(jasonDataString);
            int jDataLength = j.getJSONArray("orderData").length();
            for (int i = 0; i < jDataLength; i++) {
                orderItem = j.getJSONArray("orderData").getJSONObject(i).getString("orderItem");
                itemPrice = j.getJSONArray("orderData").getJSONObject(i).getString("itemPrice");
                itemNumber = "1";
                orderList.add(new OrderItem(orderItem, itemPrice, itemNumber));
                text = orderList.get(i).getOrderItem().toString() + text;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnPaymentCheck = getView().findViewById(R.id.btnPaymentCheck);
        tvStorename2 = getView().findViewById(R.id.tvStorename2);
        tvOrderTotalAmount = getView().findViewById(R.id.tvOrderTotalAmount);

        ArrayList<OrderItem> myDataset = new ArrayList<>();
        for(int i = 0; i < orderList.size(); i++){
            OrderItem orderItem = new OrderItem();
            myDataset.add(orderItem);
        }

        mData = myDataset;

//        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String storename = memberData.getString("TODAY_ORDER_STORENAME","0");

        btnPaymentCheck.setOnClickListener(btnPaymentCheckOnClick);
        tvStorename2.setText(storename);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainTestActivity) getActivity()).fragResult = this;
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
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
//            OrderItem orderitem = orderList.get(position);
//
//            OrderItem orderitem2 = mData.get(position);
//
//
//
//            viewHolder.tvOrderItem.setText(orderitem.getOrderItem());
//            viewHolder.tvOrderItemPrice.setText(orderitem.getItemPrice());
//            viewHolder.tvItemNumber.setText("1");
//            viewHolder.btnNumberUp.setText("+");
//            viewHolder.btnNumberDown.setText("-");

            viewHolder.tvOrderItem.setText("1");
            viewHolder.tvOrderItemPrice.setText("1");
            viewHolder.tvItemNumber.setText("1");
            viewHolder.btnNumberUp.setText("+");
            viewHolder.btnNumberDown.setText("-");

        }

        @Override
        public int getItemCount() {
            return orderList.size();
//            return 5;
        }
    }

    private View.OnClickListener btnPaymentCheckOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
//            Toast.makeText(getActivity(), orderList.get(0).getItemNumber().toString(), Toast.LENGTH_SHORT).show();
//              Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
//            OrderItem orderitem = orderList.get(0);
//            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), String.valueOf(orderList.size()), Toast.LENGTH_SHORT).show();

        }

    };

    private static class OrderItem {
        String orderItem;
        String itemPrice;
        String itemNumber;



        public OrderItem(String orderItem, String itemPrice, String itemNumber) {
            super();
            this.orderItem = orderItem;
            this.itemPrice = itemPrice;
            this.itemNumber = itemNumber;
        }

        public OrderItem() {

        }

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

    public void updateOrderItem(String aaa) {

        tvOrderTotalAmount.setText(aaa);

        getMenuItemAndPrice(aaa);

    }
}
