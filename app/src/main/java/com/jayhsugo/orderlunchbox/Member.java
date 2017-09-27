package com.jayhsugo.orderlunchbox;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/30.
 */

public class Member implements Serializable {
    private String userid;
    private String userName;
    private String userAdmin;
    private String groupName;
    private String groupCode;
    private String groupPassword;


    public Member() {
        super();
    }

    public Member(String userid, String userName, String groupName, String userAdmin, String groupCode, String groupPassword) {
        super();
        this.userid = userid;
        this.userName = userName;
        this.userAdmin = userAdmin;
        this.groupName = groupName;
        this.groupCode = groupCode;
        this.groupPassword = groupPassword;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAdmin() { return userAdmin; }

    public void setUserAdmin(String userAdmin) { this.userAdmin = userAdmin; }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }
}
