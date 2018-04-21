package com.zoer.musicserver.server

import android.net.wifi.WifiManager
import android.util.Log
import org.intellij.lang.annotations.Language
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UDPBroadcastThread internal constructor(var wifiManager: WifiManager) : Thread() {
    companion object {
        val TAG = "UDP Broadcast Thread"
    }

    override fun run() {
        super.run()
        val messageStr = "Muserver port=$port"
        val server_port_1 = 11111
        val server_port_2 = 11121
        val server_port_3 = 11131

        val mask = intToIp(wifiManager.dhcpInfo.netmask)
        val broadcastIP =
                if (mask.equals("255.255.255.0")) {
                    getIpAddress().toString().replaceAfterLast(".", "255")
                } else {
                    getIpAddress().toString().replaceAfterLast(".", "").dropLast(1).replaceAfterLast(".", "255.255")
                }
        Log.d(TAG, "Broadcast IP: $broadcastIP")
//        Log.d(TAG,"Port: $server_port")


        val local = InetAddress.getByName(broadcastIP)
        val msg_length = messageStr.length
        val message = messageStr.toByteArray()

        val s = DatagramSocket()
        //

//        var p = DatagramPacket(message, msg_length, local, server_port)
//        s.broadcast = true
//        s.send(p)//properly able to send data. i receive data to server

        while (UDP_BroadCast_Thread_Flag) {
            val p1 = DatagramPacket(message, msg_length, local, server_port_1)
            val p2 = DatagramPacket(message, msg_length, local, server_port_2)
            val p3 = DatagramPacket(message, msg_length, local, server_port_3)
            s.send(p1)//properly able to send data. i receive data to server
            s.send(p2)//properly able to send data. i receive data to server
            s.send(p3)//properly able to send data. i receive data to server
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                UDP_BroadCast_Thread_Flag = false
            }
        }
    }
}