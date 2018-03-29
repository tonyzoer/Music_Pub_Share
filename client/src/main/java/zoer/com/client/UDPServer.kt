package zoer.com.client

import java.net.DatagramPacket
import java.net.DatagramSocket
import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Build

class UDPServer {
    private var async: AsyncTask<Void, Void, Void>? = null
    private var Server_aktiv = true


    fun runUdpServer() {
         object : AsyncTask<Void, Void, Void>() {
             override fun doInBackground(vararg params: Void): Void? {
                 val lMsg = ByteArray(4096)
                 val dp = DatagramPacket(lMsg, lMsg.size)
                 var ds: DatagramSocket? = null
                 var msg:String
                 try {
                     ds = DatagramSocket(8888)

                     while (Server_aktiv) {
                         ds.receive(dp)
                         msg=lMsg.toString()
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                 } finally {
                     if (ds != null) {
                         ds.close()
                     }
                 }

                 return null
             }
         }.execute()

         if (Build.VERSION.SDK_INT >= 11)
             async!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
         else
             async!!.execute()
     }

    fun stop_UDP_Server() {
        Server_aktiv = false
    }
}