package zoer.com.client

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_connect.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
        connect.setOnClickListener({
            val myClientTask = MyClientTask(
                    address.text.toString(),
                    Integer.parseInt(port.text.toString()))
            myClientTask.execute()

        })
        var udpServerThread=UDPServerThread()
        udpServerThread.start()
        clear.setOnClickListener({ response_text.text = "" })
    }


    inner class MyClientTask internal constructor(private var dstAddress: String, private var dstPort: Int) : AsyncTask<String, String, String>() {
        private var response = ""

        override fun doInBackground(vararg arg0: String): String? {

            var socket: Socket?=null
            var dataOutputStream:DataOutputStream
            try {
                socket = Socket(dstAddress, dstPort)

                val byteArrayOutputStream = ByteArrayOutputStream(1024)
                var buffer = "play".toByteArray()
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
            response_text.text = result
            super.onPostExecute(result)
            this@ConnectActivity.runOnUiThread({ Toast.makeText(this@ConnectActivity,response, Toast.LENGTH_SHORT ).show()})
        }

    }

}
