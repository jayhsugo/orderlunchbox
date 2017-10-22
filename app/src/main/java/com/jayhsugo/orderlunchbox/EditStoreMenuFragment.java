package com.jayhsugo.orderlunchbox;


import android.content.DialogInterface;
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
import android.widget.EditText;
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
public class EditStoreMenuFragment extends Fragment {

    private RequestQueue mQueue;
    private StringRequest getRequest;
    private List<Menu> menuList;
    private StoreAdapter storeAdapter;
    private Button btnSendStoreMenu, btnAddNewItem;
    private RecyclerView recyclerView;
    private AlertDialog dialog;
    private SharedPreferences memberData;
    private String groupCode, storeName, storeMenuJsonData;
    private EditText etStoreName, etStorePhone, etStoreRequirement;

    public EditStoreMenuFragment() {
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

        View view = inflater.inflate(R.layout.fragment_edit_store_menu, container, false);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0"); // 如果沒會員檔案則取的字串0
        storeName = memberData.getString("EDIT_STORENAME", "0"); // 如果沒會員檔案則取的字串0
        Log.d("MyLog", "storeName" + storeName);

        if (!storeName.equals("0")) {

            getStoreDataFromServer();
        }
        return view;
    }

    private void getStoreDataFromServer() {

        String mUrl = "https://amu741129.000webhostapp.com/get_store_data.php";

        String parameterUrl = null;
        try {
            parameterUrl = mUrl +
                    "?groupcode=" + groupCode +
                    "&storename=" + URLEncoder.encode(storeName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        getStoreData(s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loadingDialog(false);
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(getRequest);


    }

    private void getStoreData(String jsonString) {
        JSONObject j;
        String storePhone, storeRequirement;
        try {
            j = new JSONObject(jsonString);
            int jDataLength = j.getJSONArray("data").length();
            for (int i = 0; i < jDataLength; i++) {
                storePhone = j.getJSONArray("data").getJSONObject(i).getString("storephone");
                storeRequirement = j.getJSONArray("data").getJSONObject(i).getString("storerequirement");
                etStoreName.setText(storeName);
                etStorePhone.setText(storePhone);
                etStoreRequirement.setText(storeRequirement);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getStoreMenuListDataFromServer();

    }

    private void getStoreMenuListDataFromServer() {
        String mUrl = "https://amu741129.000webhostapp.com/get_store_menu.php";

        String groupCode = memberData.getString("MEMBER_GROUPCODE", "0"); // 如果沒會員檔案則取的字串0
        String storeName = memberData.getString("EDIT_STORENAME", "0"); // 如果沒會員檔案則取的字串0

        String parameterUrl = null;
        try {
            parameterUrl = mUrl +
                    "?groupcode=" + groupCode +
                    "&storename=" + URLEncoder.encode(storeName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        getMenuItemAndPrice(s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loadingDialog(false);
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(getRequest);
    }

    private void getMenuItemAndPrice(String jasonDataString) {
        menuList = new ArrayList<>();

        JSONObject j;
        String menuitem;
        String itemprice;
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
         createRecyclerView();
    }

    private void createRecyclerView() {
        recyclerView = getView().findViewById(R.id.recyclerView_storemenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(storeAdapter);
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        loadingDialog(false);
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        storeAdapter = new StoreAdapter(getActivity().getLayoutInflater());

        etStoreName = getView().findViewById(R.id.etStoreName);
        etStorePhone = getView().findViewById(R.id.etStorePhone);
        etStoreRequirement = getView().findViewById(R.id.etStoreRequirement);

        btnAddNewItem = getView().findViewById(R.id.btnAddNewItem);
        btnAddNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMenuDialog(menuList.size()-1);
            }
        });

        btnSendStoreMenu = getView().findViewById(R.id.btnSendStoreMenu);
        btnSendStoreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etStoreName.getText().toString().isEmpty()) {
                    String text = "請輸入店家名稱!";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                } else if (etStoreName.getText().toString().indexOf(",") != -1) {
                    String text = "店名不可含有逗號\",\"";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                } else if (etStorePhone.getText().toString().isEmpty()) {
                    String text = "請輸入店家電話!";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                } else if (!(menuList.size() > 0)) {
                    String text = "請新增菜單!";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                } else {
                    loadingDialog(true);
                    saveStoreDataToJson();
                }
            }
        });

        if (storeName.equals("0")) {
            menuList = new ArrayList<>();
            createRecyclerView();
        }
    }

    private void saveStoreDataToJson() {

        JSONArray jsonStoreItemArray = new JSONArray();

        // 將每一項菜單包裝成json物件，再依序放入json陣列
        for (int i = 0; i < menuList.size(); i++) {

            JSONObject jsonStoreItemObject = new JSONObject();
            String menuItem = menuList.get(i).getMenuItem();
            String itemPrice = menuList.get(i).getItemPrice();
            try {
                jsonStoreItemObject.put("menuitem", menuItem);
                jsonStoreItemObject.put("itemprice", itemPrice);
                jsonStoreItemArray.put(jsonStoreItemObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        storeMenuJsonData = jsonStoreItemArray.toString();
        uploadStoreMenuData();
    }

    private void goToNextPage() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new EditStoreNameListFragment(), null)
                .addToBackStack(null)
                .commit();
    }

    private void uploadStoreMenuData() {
        // 建立向PHP網頁發出請求的參數網址

        String mUrl = "https://amu741129.000webhostapp.com/store_menu_data_insert.php";

        String parameterUrl = null;

        try {
            parameterUrl = mUrl +
                    "?groupcode=" + groupCode +
                    "&storename=" + URLEncoder.encode(etStoreName.getText().toString(), "UTF-8") +
                    "&storephone=" + etStorePhone.getText().toString() +
                    "&storerequirement=" + URLEncoder.encode(etStoreRequirement.getText().toString(), "UTF-8") +
                    "&storemenujsondata=" + URLEncoder.encode(storeMenuJsonData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loadingDialog(false);
                        if (s.equals("1")) {
                            Toast.makeText(getActivity(), "資料更新成功", Toast.LENGTH_SHORT).show();
                            goToNextPage();
                        } else if (s.equals("2")) {
                            Toast.makeText(getActivity(), "資料新增成功", Toast.LENGTH_SHORT).show();
                            goToNextPage();
                        } else {
                            Toast.makeText(getActivity(), "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "伺服器忙碌中，請稍後再試，謝謝!", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(getRequest);
    }

    private class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public StoreAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMenuItem, tvItemPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                tvMenuItem = itemView.findViewById(R.id.tvMenuItem);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.recyclerview_storemenu, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            viewHolder.tvMenuItem.setText(menuList.get(position).getMenuItem().toString());
            viewHolder.tvItemPrice.setText(menuList.get(position).getItemPrice().toString());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMenuDialog(position, menuList.get(position).getMenuItem().toString(), menuList.get(position).getItemPrice().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();
        }

        public void addData(int position, String menuItem, String itemPrice) {
            menuList.add(position+1, new Menu(menuItem, itemPrice));
            notifyItemInserted(position+1);
        }

        public void updateItem(int position, String menuItem, String itemPrice) {
            menuList.remove(position);
            notifyItemRemoved(position);
            menuList.add(position, new Menu(menuItem, itemPrice));
            notifyItemInserted(position);
        }

        public void removeItem(int position){
            menuList.remove(position);
            notifyItemRemoved(position);
        }

        public boolean checkMenuItemIsDouble(String menuItem){
            if (menuList.size() > 0) {
                for (int i = 0; i < menuList.size(); i++) {
                    if (menuItem.equals(menuList.get(i).getMenuItem().toString())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void editMenuDialog(final int position, String menuItem, String itemPrice) {
        View view = View.inflate(getActivity(), R.layout.edit_menu, null);
        final EditText etMenuItem, etItemPrice;
        etMenuItem = view.findViewById(R.id.etMenuItem);
        etItemPrice = view.findViewById(R.id.etItemPrice);

        etMenuItem.setText(menuItem);
        etItemPrice.setText(itemPrice);

        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("編輯菜單")
                .setView(view)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (etMenuItem.getText().toString().isEmpty() || etItemPrice.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "請輸入項目及單價", Toast.LENGTH_SHORT).show();
                        } else {
                            // 先檢查點選的項目名稱是否有更動
                            if (etMenuItem.getText().toString().equals(menuList.get(position).getMenuItem().toString())) {
                                // 如果未更動則檢查價錢是否有更動
                                if (etItemPrice.getText().toString().equals(menuList.get(position).getItemPrice().toString())) {
                                    // 如果價錢未更動則提示項目未更動
                                    Toast.makeText(getActivity(), "項目未更動", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 如果價錢有更動則執行項目更新
                                    storeAdapter.updateItem(position, etMenuItem.getText().toString(), etItemPrice.getText().toString());
                                }
                            } else {
                                // 如果項目名稱有更動則檢查項目名稱是否與菜單中其他名稱有重覆
                                boolean itemIsDouble = storeAdapter.checkMenuItemIsDouble(etMenuItem.getText().toString());
                                if (itemIsDouble) {
                                    // 如果有重覆則提示項目已存在
                                    Toast.makeText(getActivity(), "項目已存在，請點選該項目進行編輯", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 如果未重覆則執行項目更新
                                    storeAdapter.updateItem(position, etMenuItem.getText().toString(), etItemPrice.getText().toString());
                                }
                            }
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNeutralButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        storeAdapter.removeItem(position);
                    }
                })
                .setCancelable(true)
                .create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
    }

    private void addMenuDialog(final int position) {

        View view = View.inflate(getActivity(), R.layout.edit_menu, null);
        final EditText etMenuItem, etItemPrice;
        etMenuItem = view.findViewById(R.id.etMenuItem);
        etItemPrice = view.findViewById(R.id.etItemPrice);

        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("新增菜單")
                .setView(view)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etMenuItem.getText().toString().isEmpty() || etItemPrice.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "請輸入項目及單價", Toast.LENGTH_SHORT).show();
                        } else {
                            // 先檢查項目名稱是否已重覆
                            boolean itemIsDouble = storeAdapter.checkMenuItemIsDouble(etMenuItem.getText().toString());
                            if (itemIsDouble) {
                                Toast.makeText(getActivity(), "項目已存在，請點選該項目進行編輯", Toast.LENGTH_SHORT).show();
                            } else {
                                storeAdapter.addData(position, etMenuItem.getText().toString(), etItemPrice.getText().toString());
                            }
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(true)
                .create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
    }
}
