package com.zoer.musicserver.server

import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.Socket

/**
 * Created by mafio on 3/16/2018.
 */
class SocketServerReplyThread internal constructor(private val hostThreadSocket: Socket, internal var cnt: Int) : Thread() {

    override fun run() {
        val outputStream: OutputStream
        val msgReply = "Hello from Music Server, you are #" + cnt
        Log.d("TAG", msgReply)
        try {
            outputStream = hostThreadSocket.getOutputStream()
            val printStream = PrintStream(outputStream)
            printStream.print(msgReply)
            printStream.close()

//            message += "replayed: " + msgReply + "\n"

//                this@ServerActivity.runOnUiThread({ msg.setText(message) })

        } catch (e: IOException) {
            e.printStackTrace()
//                message += "Something wrong! " + e.toString() + "\n"
        }

//            this@ServerActivity.runOnUiThread({ msg.setText(message) })
    }

}