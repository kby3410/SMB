package com.example.smb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler {
    SQLiteOpenHelper mHelper = null;
    SQLiteDatabase mDB = null;

    public DBHandler(Context context) {
        mHelper = new DBHelper(context);
    }

    public static DBHandler open(Context context) {
        return new DBHandler(context);
    }

    public Cursor select() {
        mDB = mHelper.getReadableDatabase();
        String sql_query = "SELECT * FROM LauncherDB";
        Cursor c = mDB.rawQuery(sql_query, null);
        //c.moveToFirst();
        return c;
    }

    public void insert(String ip, String id, String pass){
        mDB = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ip",ip);
        values.put("id",id);
        values.put("pass",pass);
        mDB.insertWithOnConflict("LauncherDB",null,values,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void delete(String ip){
        mDB = mHelper.getWritableDatabase();
        mDB.delete("LauncherDB", "ip=?", new String[]{ip});
    }
}