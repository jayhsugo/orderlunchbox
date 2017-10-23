package com.jayhsugo.orderlunchbox;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private SharedPreferences memberData, todayOrderItemData, menuTodayJasonData;
    private RecyclerView recyclerView;
    private AlertDialog dialog;
    private RequestQueue mQueue;
    private StringRequest getRequest;
    private String groupCode;
    private MenuAdapter menuAdapter;
    private Boolean arrangeIsChanged;


    public MenuFragment() {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MyLog", "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        menuTodayJasonData = getActivity().getSharedPreferences("menu_today", MODE_PRIVATE);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        arrangeIsChanged = memberData.getBoolean("ARRANGE_LIST_IS_CHANGED", false);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0");
        String storeName = memberData.getString("TODAY_ORDER_STORENAME", "0");
        Log.d("MyLog", "arrangeIsChanged:" + String.valueOf(arrangeIsChanged));
        Log.d("MyLog", "groupCode:" + groupCode);
        Log.d("MyLog", "storeName:" + storeName);


        return view;
    }

    private void getTodayMenuItemAndPrice(String storeNameToday) {
        Log.d("MyLog", "getTodayMenuItemAndPrice()");
        loadingDialog(true);
        String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
        menuTodayJasonData = getActivity().getSharedPreferences("menu_today", MODE_PRIVATE);
        menuTodayJasonData.edit().clear(); // 先清除檔案內容

        // 建立向PHP網頁發出請求的參數網址
        String parameterUrl = null;
        try {
            parameterUrl = urlGetMenu +
                    "?groupcode=" + groupCode +
                    "&storenametoday=" + URLEncoder.encode(storeNameToday, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        menuTodayJasonData.edit().putString("menuTodayJasonData", s).apply();
                        getMenuItemAndPrice();
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

    private void getMenuItemAndPrice() {
        Log.d("MyLog", "getMenuItemAndPrice()");

        menuList = new ArrayList<>();

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

        ArrayList<Item> myDataset = new ArrayList<>();
        Log.d("MyLog", "menuList.size():"+String.valueOf(menuList.size()));
        for (int i = 0; i < menuList.size(); i++) {
            Item item = new Item();
            myDataset.add(item);
        }
        mData = myDataset;

        createRecyclerView();
    }

    private void createRecyclerView() {
        Log.d("MyLog", "createRecyclerView()");
        menuAdapter = new MenuAdapter(getActivity().getLayoutInflater());
        recyclerView = getView().findViewById(R.id.recyclerView_menuitem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(menuAdapter);
        recyclerView.setItemAnimator( new DefaultItemAnimator());
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
        Log.d("MyLog", "onActivityCreated()");


        btnOrderCheck = getView().findViewById(R.id.btnOrderCheck);
        tvOrderItem = getView().findViewById(R.id.tvOrderItem);
        tvStorename = getView().findViewById(R.id.tvStorename);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String storeName = memberData.getString("TODAY_ORDER_STORENAME", "X");
        Log.d("MyLog", "MenuFragment_storeName:" + storeName);

        if (storeName.equals("X")) {
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

        if (arrangeIsChanged) {
            memberData.edit().putBoolean("ARRANGE_LIST_IS_CHANGED", false).apply();
            getTodayMenuItemAndPrice(storeName);
        } else {
            getMenuItemAndPrice();
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
        mainActivity.tabSelect(1);

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
