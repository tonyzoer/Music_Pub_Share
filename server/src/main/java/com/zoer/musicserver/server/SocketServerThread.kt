package com.zoer.musicserver.server

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoer.musicserver.data.Request
import java.io.DataInputStream
import java.io.IOException
import java.net.InetAddress
import java.nio.charset.Charset


class SocketServerThread(val context: Context) : Thread() {
    private var count = 0
    var message = ""

    companion object {
        val TAG = "SocketServerThread"
    }

    override fun run() {
        try {
            val serverSocket = java.net.ServerSocket(StartServerSocket.SocketServerPORT)
            port = serverSocket.localPort
            var dataInputStream: DataInputStream
//                this@ServerActivity.runOnUiThread({ info.text = "I'm waiting here:   ${serverSocket.localPort}" })
            Log.d("Server", "I'm waiting here:  ${InetAddress.getLocalHost().hostAddress}  ${serverSocket.localPort}  ")
            val bytes = ByteArray(256)
            var messageFromClient: String
            val objMapper = ObjectMapper()
            while (Socket_Server_Thread_Flag) {
                val socket = serverSocket.accept()
                Log.d(TAG, serverSocket.localSocketAddress.toString())
                Log.d(TAG, serverSocket.inetAddress.hostName.toString())
                Log.d(TAG, serverSocket.inetAddress.hostAddress.toString())
                Log.d(TAG, serverSocket.receiveBufferSize.toString())
                count++
                message += ("#" + count + " from " + socket.inetAddress
                        + ":" + socket.port + "\n")


                dataInputStream = DataInputStream(socket.getInputStream())
                var buffer = ByteArray(1000)
                dataInputStream.read(buffer)
                val request: Request = objMapper.readValue(buffer.toString(Charset.defaultCharset()).trimEnd(), Request::class.java)
                manage(request,socket,context)
                val os = socket.getOutputStream()
                os.write(bytes, 0, bytes.size)
                Log.d(StartServerSocket.TAG, bytes[0].toString())


//                this.run { nextSong() }
//                    this@ServerActivity.runOnUiThread({ msg.text = message })

                val socketServerReplyThread = TestSocketServerReplyThread(
                        socket, count)
                socketServerReplyThread.run()

            }
        } catch (e: IOException) {
            e.printStackTrace()
            Socket_Server_Thread_Flag = false
        }

    }

}
