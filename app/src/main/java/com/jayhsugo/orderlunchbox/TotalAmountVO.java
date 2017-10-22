package com.jayhsugo.orderlunchbox;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/30.
 */

public class TotalAmountVO implements Serializable {
    private int id;
    private String orderitem;
    private String itemprice;
    private String itemnumber;
    private String membername;

    public TotalAmountVO() {
    }

    public TotalAmountVO(int id, String orderitem, String itemprice, String itemnumber, String membername) {
        this.id = id;
        this.orderitem = orderitem;
        this.itemprice = itemprice;
        this.itemnumber = itemnumber;
        this.membername = membername;
    }

    // id為資料庫自動產生，無需讓使用者輸入id資料
    public TotalAmountVO(String orderitem, String itemprice, String itemnumber, String membername) {
        this(0, orderitem, itemprice, itemnumber, membername);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }
}
