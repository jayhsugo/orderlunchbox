package com.jayhsugo.orderlunchbox;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.List;


/**
 * Created by Administrator on 2017/8/30.
 */

public class MemberDAOImpl implements MemberDAO {

    private RequestQueue mQueue;
    private final static String mUrl = "https://amu741129.000webhostapp.com/insert.php";
    private StringRequest getRequest;

    @Override
    public String insert(Member member) {

        String parameterUrl = mUrl +
                "?userid=" + member.getUserid() +
                "&username=" + member.getUserName() +
                "&grouppcode=" + member.getGroupCode() +
                "&groupname=" + member.getGroupName() +
                "&grouppassword=" + member.getGroupPassword();

        getRequest = new StringRequest(parameterUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {}
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {}
                });
        return "OK";
    }

    @Override
    public Member findByUserid(String name) {
        return null;
    }

    @Override
    public List<Member> getAll() {
        return null;
    }
}
