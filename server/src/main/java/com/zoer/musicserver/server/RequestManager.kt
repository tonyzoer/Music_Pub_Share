package com.zoer.musicserver.server

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoer.musicserver.Utils.SongsManager
import com.zoer.musicserver.data.Actions
import com.zoer.musicserver.data.Request
import com.zoer.musicserver.data.User
import com.zoer.musicserver.network.MusicBrowser
import com.zoer.musicserver.tasks.CreateUserIfNotExistTask
import java.net.Socket

fun manage(request: Request,socket : Socket, context: Context){
    var objectMaper = ObjectMapper()
    when (request.action) {
        Actions.LOGIN.value -> {

        }
        Actions.GET_PLAY_LIST.value->{
            val data=MusicBrowser.loadJSONFromInternalStorage(context)
            Thread(SocketServerReplyThread(socket,data?: "Empty List")).run()
        }
    }
}