package com.zoer.musicserver.server

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoer.musicserver.data.Actions
import com.zoer.musicserver.data.Request
import com.zoer.musicserver.data.User
import com.zoer.musicserver.tasks.CreateUserIfNotExistTask

fun manage(request: Request){
    var objectMaper = ObjectMapper()
    when (request.action) {
        Actions.LOGIN.value -> {

        }

    }
}