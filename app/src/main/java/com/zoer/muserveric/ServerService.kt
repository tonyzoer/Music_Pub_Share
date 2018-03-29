package com.zoer.muserveric

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket

class ServerService : Service() {
    var message = ""
    lateinit var serverSocket: ServerSocket
    var mBinder = ServerBinder()


    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val server = Thread(SocketServerThread())
        server.start()
        Toast.makeText(applicationContext, "binded", Toast.LENGTH_SHORT).show()
        return START_STICKY

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        var intent=Intent(applicationContext,this.javaClass)
        intent.`package`=packageName
        startService(intent)
        super.onTaskRemoved(rootIntent)
    }
    private inner class SocketServerReplyThread internal constructor(private val hostThreadSocket: Socket, internal var cnt: Int) : Thread() {

        override fun run() {
            val outputStream: OutputStream
            val msgReply = "Hello from Android, you are #" + cnt
            Log.d("TAG", msgReply)
            try {
                outputStream = hostThreadSocket.getOutputStream()
                val printStream = PrintStream(outputStream)
                printStream.print(msgReply)
                printStream.close()

                message += "replayed: " + msgReply + "\n"

//                this@ServerActivity.runOnUiThread({ msg.setText(message) })

            } catch (e: IOException) {
                e.printStackTrace()
//                message += "Something wrong! " + e.toString() + "\n"
            }

//            this@ServerActivity.runOnUiThread({ msg.setText(message) })
        }

    }

    private inner class SocketServerThread : Thread() {
        internal var count = 0

        override fun run() {
            try {
                serverSocket = ServerSocket(SocketServerPORT)
//                this@ServerActivity.runOnUiThread({ info.text = "I'm waiting here:   ${serverSocket.localPort}" })
                Log.d("TAG", "I'm waiting here:   ${serverSocket.localPort}")
                while (true) {
                    val socket = serverSocket.accept()
                    count++
                    message += ("#" + count + " from " + socket.inetAddress
                            + ":" + socket.port + "\n")

//                    this@ServerActivity.runOnUiThread({ msg.text = message })

                    val socketServerReplyThread = SocketServerReplyThread(
                            socket, count)
                    socketServerReplyThread.run()

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    inner class ServerBinder : Binder() {
        internal// Return this instance of LocalService so clients can call public methods
        val service: ServerService
            get() = this@ServerService
    }

    companion object {

        internal val SocketServerPORT = 8080
    }


}
