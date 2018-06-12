package com.zoer.musicserver.server

import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.Socket

class SocketServerReplyThread internal constructor(private val hostThreadSocket: Socket, private var data: String) : Thread() {

    override fun run() {
        val outputStream: OutputStream
        try {
            outputStream = hostThreadSocket.getOutputStream()
            val printStream = PrintStream(outputStream)
            printStream.print(data)
            printStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}