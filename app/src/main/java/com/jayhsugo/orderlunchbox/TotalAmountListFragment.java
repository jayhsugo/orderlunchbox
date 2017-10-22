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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class TotalAmountListFragment extends Fragment {


    private TextView tvStoreName, tvStorePhone, tvStoreRequirement, tvTotalAmount, tvTotalNumber;
    private Button btnRefresh;
    private String storeName, storeDataJson;
    private SharedPreferences memberData;
    private List<TotalAmountVO> totalAmountList;
    private TotalAmountSQLiteHelper helper;
    private AlertDialog dialog;

    public TotalAmountListFragment() {
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

        View view = inflater.inflate(R.layout.fragment_total_amount_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_total_amount_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TotalAmountListAdapter(inflater));

        if (helper == null) {
            helper = new TotalAmountSQLiteHelper(getActivity());
        }

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        storeName = memberData.getString("TODAY_ORDER_STORENAME", "0");
        storeDataJson = memberData.getString("TODAY_ORDER_STORE_DATA", "0");

        getTotalAmountListFromSQLite();

        return view;
    }

    private void getTotalAmountListFromSQLite() {
        totalAmountList = new ArrayList<>();
        totalAmountList = helper.getTotalAmountList();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvStoreName = getView().findViewById(R.id.tvStoreName);
        tvStorePhone = getView().findViewById(R.id.tvStorePhone);
        tvStoreRequirement = getView().findViewById(R.id.tvStoreRequirement);
        tvTotalAmount = getView().findViewById(R.id.tvTotalAmount);
        tvTotalNumber = getView().findViewById(R.id.tvTotalNumber);
        btnRefresh = getView().findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, new TotalAmountFragment(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });


        int TotalAmount = 0;
        int totalNumber = 0;

        if (totalAmountList.size() == 0 ) {
            Toast.makeText(getActivity(), "今日尚無人訂餐，請稍候再試，謝謝!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("MyLog", "totalAmountList.size():" + String.valueOf(totalAmountList.size()));
            for (int i = 0; i < totalAmountList.size(); i++) {
                int price = Integer.parseInt(totalAmountList.get(i).getItemprice());
                int number = Integer.parseInt(totalAmountList.get(i).getItemnumber());
                TotalAmount = (price * number) + TotalAmount;
                totalNumber = number + totalNumber;
            }
        }

        tvTotalAmount.setText(String.valueOf(TotalAmount)+" 元");
        tvTotalNumber.setText(String.valueOf(totalNumber)+" 個");

        showTodayOrderStoreData(storeDataJson);

    }

    private void showTodayOrderStoreData(String jsonString) {

        JSONObject j;
        String storePhone, storeRequirement;
        try {
            j = new JSONObject(jsonString);
            int jDataLength = j.getJSONArray("data").length();
            for (int i = 0; i < jDataLength; i++) {
                storePhone = j.getJSONArray("data").getJSONObject(i).getString("storephone");
                storeRequirement = j.getJSONArray("data").getJSONObject(i).getString("storerequirement");
                tvStoreName.setText(storeName);
                tvStorePhone.setText(storePhone);
                tvStoreRequirement.setText(storeRequirement);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadingDialog(false);
    }

    private class TotalAmountListAdapter extends RecyclerView.Adapter<TotalAmountListAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public TotalAmountListAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderItem, tvItemPrice, tvItemNumber, tvMemberName;

            public ViewHolder(View itemView) {
                super(itemView);
                tvOrderItem = itemView.findViewById(R.id.tvOrderItem);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
                tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
                tvMemberName = itemView.findViewById(R.id.tvMemberName);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = inflater.inflate(R.layout.recyclerview_total_amount_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            viewHolder.tvOrderItem.setText("0");
            viewHolder.tvItemPrice.setText("1");
            viewHolder.tvItemNumber.setText("2");
            viewHolder.tvMemberName.setText("3");

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

            viewHolder.tvOrderItem.setText(totalAmountList.get(position).getOrderitem());
            viewHolder.tvItemPrice.setText(totalAmountList.get(position).getItemprice());
            viewHolder.tvItemNumber.setText(totalAmountList.get(position).getItemnumber());

            String memberName = totalAmountList.get(position).getMembername();
            memberName = memberName.replace(",","");
            memberName = memberName.substring(0,memberName.length()-1);
            viewHolder.tvMemberName.setText(memberName);

        }

        @Override
        public int getItemCount() {
            return totalAmountList.size();

        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

}
