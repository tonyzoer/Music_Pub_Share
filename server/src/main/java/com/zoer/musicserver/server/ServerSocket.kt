package com.zoer.musicserver.server

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.zoer.musicserver.R
import java.io.DataInputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.*
import java.net.ServerSocket
import java.nio.charset.Charset

class ServerSocket : Service() {

    var message = ""
    lateinit var serverSocket: ServerSocket
    var mBinder = ServerBinder()


    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val server = Thread(SocketServerThread())
        server.start()
        val udpbcThread=Thread(UDPBroadcast())
        udpbcThread.start()
        Toast.makeText(getApplicationContext(), "Server Started", Toast.LENGTH_SHORT).show();
        return START_STICKY

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        var intent = Intent(applicationContext, this.javaClass)
        intent.`package` = packageName
        startService(intent)
        super.onTaskRemoved(rootIntent)
    }


    private inner class SocketServerReplyThread internal constructor(private val hostThreadSocket: Socket, internal var cnt: Int) : Thread() {

        override fun run() {
            val outputStream: OutputStream
            val msgReply = "Hello from Music Server, you are #" + cnt
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
                var dataInputStream: DataInputStream
//                this@ServerActivity.runOnUiThread({ info.text = "I'm waiting here:   ${serverSocket.localPort}" })
                Log.d("Server", "I'm waiting here:  ${InetAddress.getLocalHost().hostAddress}  ${serverSocket.localPort}  ")
                var bytes = ByteArray(256)
                var messageFromClient: String
                while (true) {
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
                    Log.d(TAG, bytes[0].toString())
                    Log.d(TAG, "message from client: =" + messageFromClient)

                    this.run { nextSong() }
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

    private inner class UDPBroadcast : Thread() {
        override fun run() {
            super.run()
            var messageStr: String = getIpAddress().toString()+" port:$SocketServerPORT"
            var server_port = 8888
            var local = InetAddress.getByName("192.168.0.255")
            var msg_length = messageStr.length
            var message = messageStr.toByteArray()

            var s = DatagramSocket()
            //

            var p = DatagramPacket(message, msg_length, local, server_port);
            s.broadcast=true
            s.send(p)//properly able to send data. i receive data to server

            for (i in 1..10000) {
                var p = DatagramPacket(message, msg_length, local, server_port);
                s.send(p)//properly able to send data. i receive data to server
                try {
                    Thread.sleep(1000);
                } catch (e: InterruptedException) {
                    e.printStackTrace();
                }
            }
        }
    }



inner class ServerBinder : Binder() {
    internal// Return this instance of LocalService so clients can call public methods
    val service: com.zoer.musicserver.server.ServerSocket
        get() = this@ServerSocket
}

companion object {
    val TAG = this.javaClass.simpleName
    internal val SocketServerPORT = 8080
}


fun nextSong(): Unit {
    var intent = Intent()
    intent.setAction(getString(R.string.playnext))
    sendBroadcast(intent)
}

fun prevSong(): Unit {

}

    private fun getIpAddress(): CharSequence {
        var ip = ""
        try {
            val enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces()
            while (enumNetworkInterfaces.hasMoreElements()) {
                val networkInterface = enumNetworkInterfaces
                        .nextElement()
                val enumInetAddress = networkInterface
                        .inetAddresses
                while (enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip += ("SiteLocalAddress: "
                                + inetAddress.hostAddress + "\n")
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
            ip += "Something Wrong! " + e.toString() + "\n"
        }
        Log.d("IP: ",ip)
        return ip
    }

}
