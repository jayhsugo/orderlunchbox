package com.jayhsugo.orderlunchbox;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private List<HistoryVO> historyList;
    private HistorySQLiteHelper helper;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_historylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new OrderAdapter(inflater));

        if (helper == null) {
            helper = new HistorySQLiteHelper(getActivity());
        }

        getHistoryListFromSQLite();
        return view;
    }

    private void getHistoryListFromSQLite() {
        historyList = new ArrayList<>();
        historyList = helper.getAll();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public OrderAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderDate, tvStoreName, tvOrderItem, tvOrderItemPrice, tvItemNumber;

            public ViewHolder(View itemView) {
                super(itemView);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvStoreName = itemView.findViewById(R.id.tvStoreName);
                tvOrderItem = itemView.findViewById(R.id.tvOrderItem);
                tvOrderItemPrice = itemView.findViewById(R.id.tvOrderItemPrice);
                tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = inflater.inflate(R.layout.recyclerview_historylist, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

            viewHolder.tvOrderDate.setText(historyList.get(position).getOrderdate());
            viewHolder.tvStoreName.setText(historyList.get(position).getStorename());
            viewHolder.tvOrderItem.setText(historyList.get(position).getOrderitem());
            viewHolder.tvOrderItemPrice.setText(historyList.get(position).getItemprice());
            viewHolder.tvItemNumber.setText(historyList.get(position).getItemnumber());

        }

        @Override
        public int getItemCount() {
            return historyList.size();

        }
    }

}
