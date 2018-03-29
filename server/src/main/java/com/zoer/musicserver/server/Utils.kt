package com.zoer.musicserver.server

import android.net.DhcpInfo
import android.util.Log
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by mafio on 3/16/2018.
 */
fun getIpAddress(): CharSequence {
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

                    ip = inetAddress.hostAddress
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


fun intToIp(i: Int): String {

    return (i and 0xFF).toString()  + "." +
            (i shr 8 and 0xFF) + "." +
            (i shr 16 and 0xFF) + "." +
            (i shr 24 and 0xFF).toString()

}