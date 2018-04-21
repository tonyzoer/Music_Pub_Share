package com.zoer.musicserver.tasks

import android.content.Context
import android.os.AsyncTask
import com.zoer.musicserver.Utils.SongsManager
import com.zoer.musicserver.data.Path
import java.lang.ref.WeakReference


class SaveMusicDataToJsonFileTask(ctx: Context) : AsyncTask<Unit, Unit, Boolean>() {
    private var contextWR: WeakReference<Context> = WeakReference(ctx)
    var allWorksFine=false
    override fun doInBackground(vararg p0: Unit):Boolean {
        contextWR.get().let {
            val songManager = SongsManager(it!!)
            songManager.initPlayList()
            songManager.saveToJsonFile()
            allWorksFine=true
        }
        contextWR.clear()

        return allWorksFine
    }


}