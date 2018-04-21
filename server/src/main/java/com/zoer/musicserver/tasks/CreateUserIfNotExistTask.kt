package com.zoer.musicserver.tasks

import android.content.Context
import android.os.AsyncTask
import com.zoer.musicserver.data.User
import com.zoer.musicserver.helpers.DBHelper

/**
 * Created by mafio on 4/4/2018.
 */
class CreateUserIfNotExistTask(var context:Context, var user: User):AsyncTask<Unit,Unit,Unit>() {
    override fun doInBackground(vararg p0: Unit?) {
    DBHelper(context).upsertUser(user)
    }
}