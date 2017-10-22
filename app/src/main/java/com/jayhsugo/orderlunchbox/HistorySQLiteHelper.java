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

public class HistorySQLiteHelper extends SQLiteOpenHelper {
    // 資料庫名稱、資料庫版本、資料表名稱設定
    private static final String DB_NAME = "HistoryDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "history";

    // 欄位名稱
    private static final String COL_ID = "id";
    private static final String COL_STORENAME = "storename";
    private static final String COL_ORDERITEM = "orderitem";
    private static final String COL_ITEMPRICE = "itemprice";
    private static final String COL_ITEMNUMBER = "itemnumber";
    private static final String COL_ORDERDATE = "orderdate";

    // 建立表格的SQL語法
    private static final String CREATE_TALBE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_STORENAME + " TEXT, " +
                    COL_ORDERITEM + " TEXT, " +
                    COL_ITEMPRICE + " TEXT, " +
                    COL_ITEMNUMBER + " TEXT, " +
                    COL_ORDERDATE + " TEXT ); ";


    public HistorySQLiteHelper(Context context) {
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

    public long insert(HistoryVO historyVO) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STORENAME, historyVO.getStorename());
        values.put(COL_ORDERITEM, historyVO.getOrderitem());
        values.put(COL_ITEMPRICE, historyVO.getItemprice());
        values.put(COL_ITEMNUMBER, historyVO.getItemnumber());
        values.put(COL_ORDERDATE, historyVO.getOrderdate());
        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return rowId; // 如果值為-1代表新增失敗
    }

    public int deleteByOrderDate(String orderdate) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_ORDERDATE + " = ?"; // whereClause：是否要加上刪除條件，同SQL的WHERE功能
        String[] whereArgs = {orderdate}; // whereArgs：指定WHERE條件的" ? "為何
        int count = db.delete(TABLE_NAME, whereClause, whereArgs);
        return count; // 回傳刪除了幾筆資料
    }

    public List<HistoryVO> getAll() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COL_ORDERDATE, COL_STORENAME, COL_ORDERITEM, COL_ITEMPRICE, COL_ITEMNUMBER};
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        List<HistoryVO> historyVOList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String orderdate = cursor.getString(0);
            String storename = cursor.getString(1);
            String orderitem = cursor.getString(2);
            String itemprice = cursor.getString(3);
            String itemnumber = cursor.getString(4);
            HistoryVO historyVO = new HistoryVO(id, storename, orderitem, itemprice, itemnumber, orderdate);
            historyVOList.add(historyVO);
        }
        cursor.close();
        return  historyVOList;

    }
}
