package com.jayhsugo.orderlunchbox;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/30.
 */

public class HistoryVO implements Serializable {
    private int id;
    private String storename;
    private String orderitem;
    private String itemprice;
    private String itemnumber;
    private String orderdate;

    public HistoryVO() {
    }

    public HistoryVO(int id, String storename, String orderitem, String itemprice, String itemnumber, String orderdate) {
        this.id = id;
        this.storename = storename;
        this.orderitem = orderitem;
        this.itemprice = itemprice;
        this.itemnumber = itemnumber;
        this.orderdate = orderdate;
    }

    // id為資料庫自動產生，無需讓使用者輸入id資料
    public HistoryVO(String storename, String orderitem, String itemprice, String itemnumber, String orderdate) {
        this(0, storename, orderitem, itemprice, itemnumber, orderdate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStorename() {
        return storename;
    }

    public void setStorename(String storename) {
        this.storename = storename;
    }

    public String getOrderitem() {
        return orderitem;
    }

    public void setOrderitem(String orderitem) {
        this.orderitem = orderitem;
    }

    public String getItemprice() {
        return itemprice;
    }

    public void setItemprice(String itemprice) {
        this.itemprice = itemprice;
    }

    public String getItemnumber() {
        return itemnumber;
    }

    public void setItemnumber(String itemnumber) {
        this.itemnumber = itemnumber;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }
}
