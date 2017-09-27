package com.jayhsugo.orderlunchbox;


import android.content.Context;
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
public class Page1Fragment extends Fragment {
    private List<Menu> menuList;
    private List<Item> mData;
    private static final String TAG = "tagMenuFragment";
    private final static String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
    private TextView tvStorename, tvOrderItem;
    private Button btnOrderCheck;
    SharedPreferences memberData;
    RecyclerView recyclerView;
    String aaa;

    public interface CallbackInterface {
        // 宣告一個要給MainActivity 實做的方法
        // 更新今日點菜的菜單項目
        public void updateOrderItem(String aaa);
    }

    public Page1Fragment() {

    }

    private CallbackInterface mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (CallbackInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement Page2Fragment.CallbackInterface.");
        }
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
            e.printStackTrace();
        }
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
        String storename = memberData.getString("TODAY_ORDER_STORENAME", "0");
        btnOrderCheck.setOnClickListener(btnOrderCheckOnClick);
        tvStorename.setText(storename);
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

                }
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();
//            return 2;
        }
    }

    private View.OnClickListener btnOrderCheckOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            menuOrderItemSave();

            mCallback.updateOrderItem(aaa);
        }

    };

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
        int itemNumber = 0;

        JSONArray jsonOrderItemArray = new JSONArray();
        JSONObject jsonOrderItemTotalObject = new JSONObject();


        // 將被勾選的每一項菜單包裝成json物件，再依序放入json陣列
        for (int i = 0; i < menuList.size(); i++) {
            if (mData.get(i).isCheck()) {
                JSONObject jsonOrderItemObject = new JSONObject();
                String orderItem = menuList.get(i).getMenuItem();
                String itemPrice = menuList.get(i).getItemPrice();
                itemNumber = 1 + itemNumber;
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
        memberData.edit().putString("TODAY_ORDER_ITEM_JSON", "")
                         .putString("TODAY_ORDER_ITEM_JSON", jsonOrderItemTotalObject.toString()).apply();
        tvOrderItem.setText(String.valueOf(itemNumber));
        Toast.makeText(getActivity(), jsonOrderItemTotalObject.toString(), Toast.LENGTH_SHORT).show();
        aaa = jsonOrderItemTotalObject.toString();
    }
}
