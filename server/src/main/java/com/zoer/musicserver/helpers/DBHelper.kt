package com.zoer.musicserver.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, "myDB", null, 1) {

    companion object {
        val TAG="DBHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "--- onCreate database ---")
        // создаем таблицу с полями
        db.execSQL("create table pathes ("
                + "id integer primary key autoincrement,"
                + "path text" + ");")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}