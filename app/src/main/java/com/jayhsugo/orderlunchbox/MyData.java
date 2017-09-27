package com.jayhsugo.orderlunchbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Administrator on 2017/8/31.
 */

public class MyData {
    public static final String URL = "https://amu741129.000webhostapp.com/";
    private static final String USER = "id1299440_jayhsugo";
    private static final String PASSWORD = "zzzz1111";

    public static void main(String[] args) {
        Connection con1 = null;

        try {
            Class.forName("com.mysql.jdbc.Driver"); //載入驅動(必須處理ClassNotFoundException例外)
            con1 = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("OK!");
        } catch (ClassNotFoundException ce) {
            System.out.println(ce);
        } catch (SQLException se) {
            System.out.println(se);
        } finally {
            if (con1 != null) {
                try {
                    con1.close(); //連線是珍貴的資源，使用完必須關閉
                } catch (SQLException se) {
                    System.out.println(se);
                }
            }
        }



    }
}