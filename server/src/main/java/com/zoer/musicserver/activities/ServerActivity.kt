package com.zoer.musicserver.activities

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.CompoundButton
import com.zoer.musicserver.R
import com.zoer.musicserver.server.Socket_Server_Thread_Flag
import com.zoer.musicserver.server.StartServerSocket
import com.zoer.musicserver.server.UDP_BroadCast_Thread_Flag
import kotlinx.android.synthetic.main.activity_main.*
import java.net.NetworkInterface
import java.net.SocketException
import android.R.id.edit
import android.graphics.Color
import com.nightonke.boommenu.BoomMenuButton
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum
import com.zoer.musicserver.builders.BMBBuilderManager
import com.zoer.musicserver.server.Socket_Reply_Thread_Flag
//import kotlinx.android.synthetic.main.activity_settings.*


class ServerActivity : AppCompatActivity() {


    companion object {
        val TAG = "ServerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        infoip.text = getIpAddress()
        var serverIntent = Intent(this@ServerActivity, StartServerSocket::class.java)
        startService(serverIntent)
        init()
//        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show()
        if (!serverActivityPreviuslyStarted) {
           serverActivityPreviuslyStarted=true
            startActivity(Intent(this, MusicActivity::class.java))
        }
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
        Log.d("IP: ", ip)
        return ip
    }

    private fun init() {
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.server_setings)
        toolbar.setTitleTextColor(Color.BLACK)

        serverSwitch.isChecked=Socket_Server_Thread_Flag
        serverSwitch.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                Socket_Server_Thread_Flag = true
                var serverIntent = Intent(this@ServerActivity, StartServerSocket::class.java)
                startService(serverIntent)
            } else {
                Socket_Server_Thread_Flag = false
            }
        }
        visibilitySwitch.isChecked=UDP_BroadCast_Thread_Flag
        visibilitySwitch.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                UDP_BroadCast_Thread_Flag = true
                var serverIntent = Intent(this@ServerActivity, StartServerSocket::class.java)
                startService(serverIntent)
            } else {
                UDP_BroadCast_Thread_Flag = false
            }
        }

        val bmb = toolbar.findViewById<BoomMenuButton>(R.id.menu_bmb)
        bmb.buttonEnum = ButtonEnum.Ham
        bmb.piecePlaceEnum = PiecePlaceEnum.HAM_2
        bmb.addBuilder(BMBBuilderManager.getMusicHAMButtonBuilder().listener({ startActivity(Intent(this, MusicActivity::class.java)) }))
        bmb.addBuilder(BMBBuilderManager.getSettingsHAMButtonBuilder().listener({ startActivity(Intent(this, ServerActivity::class.java)) }))
    }

}

