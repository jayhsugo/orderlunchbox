package com.jayhsugo.orderlunchbox;

/**
 * Created by Administrator on 2017/9/21.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

/**
 * @author Rowand jj
 * 页面适配器
 */
public class MyPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "YiPageAdapter";
    /**
     * 页面内容集合
     */


    private List<Fragment> fgs = null;
    private FragmentManager mFragmentManager;

    /**
     * 当数据发生改变时的回调接口
     */
//    private OnReloadListener mListener;

    public MyPagerAdapter(FragmentManager fm, List<Fragment> fgs)
    {
        super(fm);
        this.fgs = fgs;
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int index)
    {
        Log.i(TAG,"ITEM CREATED...");
        return fgs.get(index);
    }

    @Override
    public int getCount()
    {
        return fgs.size();// 返回选项卡总数
    }

//    @Override
//    public int getItemPosition(Object object)
//    {
//        return POSITION_NONE;
//    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "菜單";
            case 1:
                return "結帳";
            case 2:
                return "紀錄";
            case 3:
                return "會員";
            default:
                return null;
        }
    }

//    @Override
//    public int getItemPosition(Object object) {
//        return POSITION_NONE;
//    }



//    /**
//     * 重新设置页面内容
//     * @param items
//     */
//    public void setPagerItems(List<Fragment> items)
//    {
//        if (items != null)
//        {
//            for (int i = 0; i < fgs.size(); i++)
//            {
//                mFragmentManager.beginTransaction().remove(fgs.get(i)).commit();
//            }
//            fgs = items;
//        }
//    }
//    /**
//     *当页面数据发生改变时你可以调用此方法
//     *
//     * 重新载入数据，具体载入信息由回调函数实现
//     */
//    public void reLoad()
//    {
//        if(mListener != null)
//        {
//            mListener.onReload();
//        }
//        this.notifyDataSetChanged();
//    }
//    public void setOnReloadListener(OnReloadListener listener)
//    {
//        this.mListener = listener;
//    }
//    /**
//     * @author Rowand jj
//     *回调接口
//     */
//    public interface OnReloadListener
//    {
//        public void onReload();
//    }



//    private class MyPagerAdapter extends FragmentPagerAdapter{
//
//        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            Fragment fragment = null;
//
//            switch (position) {
//                case 0:
//                    fragment = new MenuFragment();
//                    break;
//                case 1:
//                    fragment = new CheckoutFragment();
//                    break;
//                case 2:
//                    fragment = new HistoryFragment();
//                    break;
//                case 3:
//                    fragment = new MemberDataFragment();
//                    break;
//            }
//            return fragment;
//        }
//
//        @Override
//        public int getCount() {
//            return TAB_COUNT;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "菜單";
//                case 1:
//                    return "結帳";
//                case 2:
//                    return "紀錄";
//                case 3:
//                    return "會員";
//                default:
//                    return null;
//            }
//        }
//
//        public void setOnReloadListener(OnReloadListener onReloadListener) {
//        }
//    }
}
