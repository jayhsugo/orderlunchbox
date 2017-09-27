package com.jayhsugo.orderlunchbox;

/**
 * Created by Administrator on 2017/9/18.
 */

public class Menu {
    private String storeName;
    private String menuItem;
    private String itemPrice;

    public Menu(){ super(); }

    public Menu(String menuItem, String itemPrice) {
        super();
        this.menuItem = menuItem;
        this.itemPrice = itemPrice;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }
}
