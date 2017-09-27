package com.jayhsugo.orderlunchbox;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CheckoutFragment extends Fragment {
    private static final String TAG = "tagCheckoutFragment";
    public static final String CONTENT_VIEW = "content_view";
    public static final String IS_UPDATE = "is_update";
    private TextView tvCheckout;
    private String todayOrderItem;
    SharedPreferences memberData;


    public CheckoutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_checkout,container,false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}
