package com.jayhsugo.orderlunchbox;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/9/18.
 */

public class DateOrderStore implements Serializable {
    private String store;
    private Calendar today;

    public DateOrderStore() { super(); }

    public DateOrderStore(String store, Calendar today) {
        this.store = store;
        this.today = today;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Calendar getToday() {
        return today;
    }

    public void setToday(Calendar today) {
        this.today = today;
    }
}
