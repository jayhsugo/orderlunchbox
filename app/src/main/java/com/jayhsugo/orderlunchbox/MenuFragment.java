package com.jayhsugo.orderlunchbox;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment {
    private List<Menu> menuList;
    private List<Item> mData;
    private TextView tvStorename, tvOrderItem;
    private Button btnOrderCheck;
    private SharedPreferences memberData, todayOrderItemData;
    private RecyclerView recyclerView;
    private AlertDialog dialog;


    public MenuFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_menuitem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MenuAdapter(inflater));

        getMenuItemAndPrice();
        return view;
    }

    private void getMenuItemAndPrice() {

        menuList = new ArrayList<>();
        SharedPreferences menuTodayJasonData = getActivity().getSharedPreferences("menu_today", MODE_PRIVATE);
        String jasonDataString = menuTodayJasonData.getString("menuTodayJasonData", "0");

            JSONObject j;
            String menuitem = null;
            String itemprice = null;

            try {
                j = new JSONObject(jasonDataString);
                int jDataLength = j.getJSONArray("data").length();

                for (int i = 0; i < jDataLength; i++) {
                    menuitem = j.getJSONArray("data").getJSONObject(i).getString("menuitem");
                    itemprice = j.getJSONArray("data").getJSONObject(i).getString("itemprice");
                    menuList.add(new Menu(menuitem, itemprice));
                }

            } catch (JSONException e) {
                Toast.makeText(getActivity(), "今日未提供店家訂購", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

    }



    private void orderAgainDialog() {

        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("是否需要重新點菜")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        todayOrderItemData.edit().putBoolean("SERVICE_IS_OPEN", false).apply();
                        Intent intent = new Intent(getActivity(), LongRunningService.class);
                        getActivity().stopService(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        goToCheckoutPage();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnOrderCheck = getView().findViewById(R.id.btnOrderCheck);
        tvOrderItem = getView().findViewById(R.id.tvOrderItem);
        tvStorename = getView().findViewById(R.id.tvStorename);
        ArrayList<Item> myDataset = new ArrayList<>();
        for (int i = 0; i < menuList.size(); i++) {
            Item item = new Item();
            myDataset.add(item);
        }

        mData = myDataset;
        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String storeName = memberData.getString("TODAY_ORDER_STORENAME", "");
        Log.d("MyLog", "MenuFragment_storeName:" + storeName);
        if (storeName.equals("") || storeName.equals(" ")) {
            tvStorename.setText("未提供");
        } else {
            tvStorename.setText(storeName);
        }
        btnOrderCheck.setOnClickListener(btnOrderCheckOnClick);


        todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        Boolean todayMenuIsChecked = todayOrderItemData.getBoolean("TODAY_MENU_IS_CHECKED", false);

        if (todayMenuIsChecked) {
            // 跳出視窗詢問是否需要重新訂餐
            orderAgainDialog();
        }
    }


    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public MenuAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMenuItem, tvItemPrice;
            CheckBox menuItemCheckBox;

            public ViewHolder(View itemView) {
                super(itemView);
                tvMenuItem = itemView.findViewById(R.id.tvMenuItem);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
                menuItemCheckBox = itemView.findViewById((R.id.menuItemCheckBox));
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = inflater.inflate(R.layout.recyclerview_menuitem, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            final Menu menu = menuList.get(position);

            Item item = mData.get(position);
            viewHolder.tvMenuItem.setText(menu.getMenuItem());
            viewHolder.tvItemPrice.setText(menu.getItemPrice());
            viewHolder.menuItemCheckBox.setChecked(item.isCheck());


            viewHolder.menuItemCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean b = ((CheckBox) view).isChecked();
                    viewHolder.menuItemCheckBox.setChecked(b);
                    mData.get(position).setCheck(b);
                    showOrderNumber();

                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //檢查被點擊的item是否已經被勾選
                    //如果有則取消勾選，將菜單選項陣列中的該item設為false
                    //否則設定為勾選，將菜單選項陣列中的該item設為true
                    if (viewHolder.menuItemCheckBox.isChecked()) {
                        viewHolder.menuItemCheckBox.setChecked(false);
                        mData.get(position).setCheck(false);
                    } else {
                        viewHolder.menuItemCheckBox.setChecked(true);
                        mData.get(position).setCheck(true);
                    }
                    showOrderNumber();

                }
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();

        }
    }

    private void showOrderNumber() {
        int itemNumber = 0;
        for (int i = 0; i < menuList.size(); i++) {
            if (mData.get(i).isCheck()) {
                itemNumber = 1 + itemNumber;
            }
        }
        tvOrderItem.setText("共 "+ String.valueOf(itemNumber) + " 項");
    }

    private View.OnClickListener btnOrderCheckOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mData.size() == 0) {
                Toast.makeText(getActivity(), "請勾選項目", Toast.LENGTH_SHORT).show();
            } else {
                todayOrderItemData.edit().putBoolean("TODAY_MENU_IS_CHECKED", false).apply();
                todayOrderItemData.edit().putBoolean("TODAY_IS_PAID", false).apply();
                menuOrderItemSave();
                goToCheckoutPage();
            }
        }

    };

    private void goToCheckoutPage() {

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.initBody(1);

    }

    private static class Item {
        boolean check;

        public boolean isCheck() {
            return check;
        }

        public void setCheck(boolean check) {
            this.check = check;
        }
    }

    private void menuOrderItemSave() {


        JSONArray jsonOrderItemArray = new JSONArray();
        JSONObject jsonOrderItemTotalObject = new JSONObject();


        // 將被勾選的每一項菜單包裝成json物件，再依序放入json陣列
        for (int i = 0; i < menuList.size(); i++) {
            if (mData.get(i).isCheck()) {
                JSONObject jsonOrderItemObject = new JSONObject();
                String orderItem = menuList.get(i).getMenuItem();
                String itemPrice = menuList.get(i).getItemPrice();

                try {
                    jsonOrderItemObject.put("orderItem", orderItem);
                    jsonOrderItemObject.put("itemPrice", itemPrice);
                    jsonOrderItemArray.put(jsonOrderItemObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // 將json陣列包裝成一個json物件，取名orderData
        try {
            jsonOrderItemTotalObject.put("orderData", jsonOrderItemArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 將結果已字串形式儲存
        SharedPreferences todayOrderItemData = getActivity().getSharedPreferences("today_order_item_data", MODE_PRIVATE);
        todayOrderItemData.edit().clear();
        todayOrderItemData.edit().putString("TODAY_ORDER_ITEM_JSON", jsonOrderItemTotalObject.toString()).apply();


    }
}
