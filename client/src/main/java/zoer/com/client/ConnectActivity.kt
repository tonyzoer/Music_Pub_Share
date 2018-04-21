package zoer.com.client

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_connect.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import android.net.wifi.WifiInfo
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import com.fasterxml.jackson.databind.ObjectMapper
import zoer.com.client.data.User


class ConnectActivity : AppCompatActivity(), ListChangedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        ipLV.adapter=ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, ipList)
        var udp= UDPServerThread(this).execute()
        ipLV.setOnItemClickListener({ adapterView: AdapterView<*>, view: View, i: Int, l: Long ->
            val info=(view as TextView).text.toString().split(":")
            MyClientTask(info[0],info[1].toInt()).execute()
            udpActive=false
        })
        update.setOnClickListener({
            ipList.clear()
            elementChanged()
            udpActive=true
            udp.cancel(true)
            udp=UDPServerThread(this@ConnectActivity).execute()
        })
    }

    override fun elementChanged() {
        val adapter=ipLV.adapter as ArrayAdapter<*>
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("StaticFieldLeak")
    inner class MyClientTask internal constructor(private var dstAddress: String, private var dstPort: Int=0) : AsyncTask<String, String, String>() {
        private var response = ""

        override fun doInBackground(vararg arg0: String): String? {

            var socket: Socket?=null
            var dataOutputStream:DataOutputStream
            try {
                socket = Socket(dstAddress, dstPort)
                val objMap = ObjectMapper()

                val byteArrayOutputStream = ByteArrayOutputStream(1024)
                val buffer = objMap.writeValueAsBytes(User(getMAC()))
                dataOutputStream= DataOutputStream(socket.getOutputStream())

                var bytesRead: Int
                val inputStream = socket.getInputStream()

                dataOutputStream.writeUTF("play")
                bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                    response += byteArrayOutputStream.toString("UTF-8")
                    bytesRead = inputStream.read(buffer)
                }

            } catch (e: UnknownHostException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                response = "UnknownHostException: " + e.toString()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                response = "IOException: " + e.toString()
            } finally {
                if (socket != null) {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }

                }
            }
            return response
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            this@ConnectActivity.runOnUiThread({ Toast.makeText(this@ConnectActivity,response, Toast.LENGTH_SHORT ).show()})
        }
    }

    fun getMAC():String{
        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        return info.macAddress
    }
}
