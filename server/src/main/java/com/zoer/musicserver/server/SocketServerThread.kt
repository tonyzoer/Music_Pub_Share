package com.zoer.musicserver.server

import android.util.Log
import java.io.DataInputStream
import java.io.IOException
import java.net.InetAddress


class SocketServerThread : Thread() {
    private var count = 0
    var message = ""
    companion object {
        val TAG="SocketServerThread"
    }
    override fun run() {
        try {
            var serverSocket = java.net.ServerSocket(StartServerSocket.SocketServerPORT)
            var dataInputStream: DataInputStream
//                this@ServerActivity.runOnUiThread({ info.text = "I'm waiting here:   ${serverSocket.localPort}" })
            Log.d("Server", "I'm waiting here:  ${InetAddress.getLocalHost().hostAddress}  ${serverSocket.localPort}  ")
            var bytes = ByteArray(256)
            var messageFromClient: String
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
                messageFromClient = dataInputStream.readUTF()
                val os = socket.getOutputStream()
                os.write(bytes, 0, bytes.size)
                Log.d(StartServerSocket.TAG, bytes[0].toString())
                Log.d(StartServerSocket.TAG, "message from client: =" + messageFromClient)

//                this.run { nextSong() }
//                    this@ServerActivity.runOnUiThread({ msg.text = message })

                val socketServerReplyThread = SocketServerReplyThread(
                        socket, count)
                socketServerReplyThread.run()

            }
        } catch (e: IOException) {
            e.printStackTrace()
            Socket_Server_Thread_Flag=false
        }

    }

}