package com.jayhsugo.orderlunchbox;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/18.
 */

public class Store implements Serializable {
    private String storeName;
    private String storeType;
    private String storePhone;
    private String storeAddress;
    private String storeRequirement;
    private String groupCode;

    public Store() {
        super();
    }

    public Store(String storeName, String storeType, String storePhone, String storeAddress, String storeRequirement, String groupCode) {
        super();
        this.storeName = storeName;
        this.storeType = storeType;
        this.storePhone = storePhone;
        this.storeAddress = storeAddress;
        this.storeRequirement = storeRequirement;
        this.groupCode = groupCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreRequirement() {
        return storeRequirement;
    }

    public void setStoreRequirement(String storeRequirement) { this.storeRequirement = storeRequirement; }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
