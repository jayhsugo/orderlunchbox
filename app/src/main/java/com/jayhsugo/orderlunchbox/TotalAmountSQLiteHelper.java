package com.jayhsugo.orderlunchbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/30.
 */

public class TotalAmountSQLiteHelper extends SQLiteOpenHelper {
    // 資料庫名稱、資料庫版本、資料表名稱設定
    private static final String DB_NAME = "TotalAmountDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "totalAmount";

    // 欄位名稱
    private static final String COL_ID = "id";
    private static final String COL_ORDERITEM = "orderitem";
    private static final String COL_ITEMPRICE = "itemprice";
    private static final String COL_ITEMNUMBER = "itemnumber";
    private static final String COL_MEMBERNAME = "membername";

    // 建立表格的SQL語法
    private static final String CREATE_TALBE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ORDERITEM + " TEXT, " +
                    COL_ITEMPRICE + " TEXT, " +
                    COL_ITEMNUMBER + " TEXT, " +
                    COL_MEMBERNAME + " TEXT ); ";


    public TotalAmountSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TALBE); // 在系統自動建立資料庫的同時，建立資料表
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insert(TotalAmountVO totalAmountVO) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ORDERITEM, totalAmountVO.getOrderitem());
        values.put(COL_ITEMPRICE, totalAmountVO.getItemprice());
        values.put(COL_ITEMNUMBER, totalAmountVO.getItemnumber());
        values.put(COL_MEMBERNAME, totalAmountVO.getMembername());
        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return rowId; // 如果值為-1代表新增失敗
    }

    public int deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_ITEMNUMBER + " != ?"; // whereClause：是否要加上刪除條件，同SQL的WHERE功能
        String[] whereArgs = {"0"}; // whereArgs：指定WHERE條件的" ? "為何
        int count = db.delete(TABLE_NAME, whereClause, whereArgs);
        return count; // 回傳刪除了幾筆資料
    }

    public List<TotalAmountVO> getAll() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COL_ORDERITEM, COL_ITEMPRICE, COL_ITEMNUMBER, COL_MEMBERNAME};
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        List<TotalAmountVO> totalAmountVOList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String orderitem = cursor.getString(0);
            String itemprice = cursor.getString(1);
            String itemnumber = cursor.getString(2);
            String membername = cursor.getString(3);
            TotalAmountVO totalAmountVO = new TotalAmountVO(id, orderitem, itemprice, itemnumber, membername);
            totalAmountVOList.add(totalAmountVO);
        }
        cursor.close();
        return  totalAmountVOList;
    }

    public List<TotalAmountVO> getTotalAmountList() {

        // SQL語法參考:"SELECT orderitem, itemprice, sum(itemnumber), GROUP_CONCAT(membername) FROM `Table_name` GROUP BY orderitem"

        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COL_ORDERITEM, COL_ITEMPRICE, "sum("+COL_ITEMNUMBER+")", "GROUP_CONCAT("+COL_MEMBERNAME+")"};
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, COL_ORDERITEM, null, null); // 語法參考: db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
        List<TotalAmountVO> totalAmountVOList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String orderitem = cursor.getString(0);
            String itemprice = cursor.getString(1);
            String itemnumber = cursor.getString(2);
            String membername = cursor.getString(3);
            TotalAmountVO totalAmountVO = new TotalAmountVO(id, orderitem, itemprice, itemnumber, membername);
            totalAmountVOList.add(totalAmountVO);
        }
        cursor.close();
        return  totalAmountVOList;
    }
}
