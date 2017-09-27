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
public class MenuFragment extends Fragment {
    private List<Menu> menuList;
    private static final String TAG = "tagMenuFragment";
    private final static String urlGetMenu = "https://amu741129.000webhostapp.com/get_today_menu.php";
    private TextView tvStorename, tvOrderItem;
    private String todayOrderItem;
    private Button btnOrderCheck;
    SharedPreferences memberData;



    public MenuFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_menuitem);
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

        memberData = getActivity().getSharedPreferences("member_data", MODE_PRIVATE);
        String storename = memberData.getString("TODAY_ORDER_STORENAME","0");
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

            public ViewHolder(View itemView) {
                super(itemView);
                tvMenuItem = itemView.findViewById(R.id.tvMenuItem);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.recyclerview_menuitem, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            final Menu menu = menuList.get(position);
            viewHolder.tvMenuItem.setText(menu.getMenuItem());
            viewHolder.tvItemPrice.setText(menu.getItemPrice());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = menu.getMenuItem() + " " + menu.getItemPrice();
                    tvOrderItem.setText(text);
                    todayOrderItem = text;

                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();
        }
    }

    private View.OnClickListener btnOrderCheckOnClick = new View.OnClickListener() {


        @Override
        public void onClick(View view) {
            memberData.edit().putString("TODAY_ORDER_ITEM", todayOrderItem).apply();
            Toast.makeText(getActivity(), "Go to CheckoutPage", Toast.LENGTH_SHORT).show();



//            SharedPreferences sp = MenuFragment.this.getActivity().getSharedPreferences(CheckoutFragment.CONTENT_VIEW, Context.MODE_PRIVATE);
//            boolean state = sp.getBoolean(CheckoutFragment.IS_UPDATE, false);
//            Toast.makeText(getActivity(),state+"",0).show();
//            Editor editor = sp.edit();
//            editor.putBoolean(CheckoutFragment.IS_UPDATE,!state);
//            editor.commit();
//            MainActivity a = (MainActivity) getActivity();
//            a.getAdapter().reLoad();
        }

    };





}
