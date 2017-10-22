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
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditStoreNameListFragment extends Fragment {


    private RequestQueue mQueue;
    private StringRequest getRequest;
    private List<String> storeList;
    private String groupCode;
    private Button btnAddNewStore;
    private RecyclerView recyclerView;
    private StoreAdapter storeAdapter;
    private AlertDialog dialog;
    private SharedPreferences memberData;


    public EditStoreNameListFragment() {
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
        Log.d("MyLog", "EditStoreNameListFragment_onCreateView");
        View view = inflater.inflate(R.layout.fragment_edit_store_name_list, container, false);

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        groupCode = memberData.getString("MEMBER_GROUPCODE", "0"); // 如果沒會員檔案則取的字串0

        getStoreNameListDataFromServer(groupCode);

        return view;
    }

    private void getStoreNameListDataFromServer(String groupCode) {
        loadingDialog(true);
        String url = "https://amu741129.000webhostapp.com/get_store_list.php?groupcode=" + groupCode;

        mQueue = new Volley().newRequestQueue(getActivity());
        getRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                   public void onResponse(String s) {
                        Log.d("MyLog", "getStoreNameListDataFromServer_onResponse:"+ s);
                        if (s.equals("-1")) {
                            loadingDialog(false);
                        } else {
                            getStoreNameList(s);
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

    private void getStoreNameList(String jsonData) {
        storeList = new ArrayList<>();
        JSONObject j;
        String storeName;

        try {
            j = new JSONObject(jsonData);
            int jDataLength = j.getJSONArray("data").length();
            for (int i = 0; i < jDataLength; i++) {
                storeName = j.getJSONArray("data").getJSONObject(i).getString("storename");
                storeList.add(storeName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        createRecyclerView();
    }

    private void createRecyclerView() {
        recyclerView = getView().findViewById(R.id.recyclerView_storename);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(storeAdapter);
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        loadingDialog(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("MyLog", "EditStoreNameListFragment_onActivityCreated");

        storeAdapter = new StoreAdapter(getActivity().getLayoutInflater());

        btnAddNewStore = getView().findViewById(R.id.btnAddNewStore);
        btnAddNewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String storeName = "0";
                memberData.edit().putString("EDIT_STORENAME", storeName).apply();
                goToNextPage();
            }
        });

    }

    private class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
        private LayoutInflater inflater;

        public StoreAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStoreName;

            public ViewHolder(View itemView) {
                super(itemView);
                tvStoreName = itemView.findViewById(R.id.tvStoreName);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.recyclerview_storename, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            viewHolder.tvStoreName.setText(storeList.get(position).toString());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String storeName = viewHolder.tvStoreName.getText().toString();
                    memberData.edit().putString("EDIT_STORENAME", storeName).apply();
                    goToNextPage();
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMenuDialog(position, viewHolder.tvStoreName.getText().toString());

                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return storeList.size();
        }

        public void removeItem(int position){
            storeList.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void deleteMenuDialog(final int position, final String storeName) {

        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("確定刪除 "+ storeName + " 嗎?")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadingDialog(true);
                        deleteStoreMenuData(storeName, position);
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

    private void deleteStoreMenuData(String storeName, final int position) {
        // 建立向PHP網頁發出請求的參數網址

        String mUrl = "https://amu741129.000webhostapp.com/store_name_data_delete.php";

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
                        if (s.equals("1")) {
                            Toast.makeText(getActivity(), "資料刪除成功", Toast.LENGTH_SHORT).show();
                            storeAdapter.removeItem(position);
                        } else {
                            Toast.makeText(getActivity(), "未知錯誤，請重試", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog(false);
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

    private void goToNextPage() {
        getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, new EditStoreMenuFragment(), null)
                        .addToBackStack(null)
                        .commit();
    }


}
