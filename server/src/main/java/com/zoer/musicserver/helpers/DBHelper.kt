package com.zoer.musicserver.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.zoer.musicserver.activities.SettingsActivity
import com.zoer.musicserver.data.Path
import com.zoer.musicserver.data.User

class DBHelper(context: Context) : SQLiteOpenHelper(context, "myDB", null, 3) {

    companion object {
        val TAG = "DBHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "--- onCreate database ---")
        // создаем таблицу с полями
        db.execSQL("CREATE table pathes ("
                + "id integer primary key autoincrement,"
                + "path text,"
                + "datetime_modified INTEGER " + ");")
        db.execSQL("CREATE TABLE users (id integer primary key autoincrement, mac text unique, name text);")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "--- onUpgrade database")
        if (newVersion < 2)
            db.execSQL("ALTER TABLE pathes ADD COLUMN datetime_modified INTEGER;")
        if (newVersion < 3)
            db.execSQL("CREATE TABLE users (id integer primary key autoincrement, mac text unique, name text);")
    }

    fun getMusicPathesFromDb(): ArrayList<Path> {
        val pathes: ArrayList<Path> = ArrayList()
        try {
            val db = this.readableDatabase
            val c = db?.rawQuery("SELECT * FROM pathes", null)
            if (c?.moveToFirst() == true) {
                do {
                    val path = c.getString(c.getColumnIndex("path"))
                    val id = c.getInt(c.getColumnIndex("id"))
                    val date = c.getInt(c.getColumnIndex("id"))
                    Log.d(SettingsActivity.TAG, path)
                    pathes.add(Path(id, path, date))

                } while (c.moveToNext() ?: false)
            }
            c?.close()
        } catch (se: SQLiteException) {
            Log.e(SettingsActivity.TAG, "Couldn't open the database")
        }
        return pathes
    }
    fun upsertUser(user: User){
        writableDatabase.execSQL("INSERT OR REPLACE INTO user (mac, name) " +
                "VALUES (COALESCE((SELECT mac FROM users WHERE mac = ${user.mac}),${user.mac}),${user.name})")
    }
}