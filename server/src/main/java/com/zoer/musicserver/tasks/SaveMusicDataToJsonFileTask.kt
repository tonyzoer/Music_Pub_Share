package com.zoer.musicserver.tasks

import android.content.Context
import android.os.AsyncTask
import com.zoer.musicserver.Utils.SongsManager
import java.lang.ref.WeakReference


class SaveMusicDataToJsonFileTask: AsyncTask<ArrayList<String>, Unit, Unit> {
    var contextWR:WeakReference<Context>?=null

    constructor(ctx:Context):super(){
        contextWR=WeakReference(ctx)
    }

    override fun doInBackground(vararg p0: ArrayList<String>) {
        var songManager=SongsManager()
        songManager.initPlayList(p0[0])
        songManager.saveToJsonFile(contextWR?.get())
    }
}