package com.zoer.musicserver.server

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.zoer.musicserver.R
import java.net.ServerSocket

class StartServerSocket : Service() {


    lateinit var serverSocket: ServerSocket
    var mBinder = ServerBinder()
    var server: Thread? = null
    var udpBroadcast: Thread? = null
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    @SuppressLint("WifiManagerLeak")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Socket_Server_Thread_Flag) {
            if (!(socketServerThreadLink?.isAlive ?: true)) {
                server = socketServerThreadLink
            } else {
                server = Thread(SocketServerThread(context = applicationContext))
                server?.start()
                Log.d(TAG, "Server Started")
                socketServerThreadLink = server
            }
        } else {
            server = socketServerThreadLink
            server?.interrupt()
            Log.d(TAG, "Server Is Disabled")
            socketServerThreadLink = null
            server = null
        }
        if (UDP_BroadCast_Thread_Flag) {
            if (!(udpThreadLink?.isAlive ?: true)) {
                udpBroadcast = udpThreadLink
            } else {
                udpBroadcast = Thread(UDPBroadcastThread(getSystemService(Context.WIFI_SERVICE) as WifiManager))
                udpBroadcast?.start()
                Log.d(TAG, "Broadcasting started")
                udpThreadLink = udpBroadcast
            }
        } else {
            udpBroadcast = udpThreadLink
            udpBroadcast?.interrupt()
            Log.d(TAG, "Broadcasting is disabled")
            udpThreadLink = null
            udpBroadcast = null
        }

        return START_STICKY

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        var intent = Intent(applicationContext, this.javaClass)
        intent.`package` = packageName
        startService(intent)
        super.onTaskRemoved(rootIntent)
    }


    inner class ServerBinder : Binder() {
        internal// Return this instance of LocalService so clients can call public methods
        val service: com.zoer.musicserver.server.StartServerSocket
            get() = this@StartServerSocket
    }

    companion object {
        val TAG = this.javaClass.simpleName
        internal val SocketServerPORT = 8080


    }


    fun nextSong(): Unit {
        var intent = Intent()
        intent.action = getString(R.string.playnext)
        sendBroadcast(intent)
    }

    fun prevSong(): Unit {

    }

}
