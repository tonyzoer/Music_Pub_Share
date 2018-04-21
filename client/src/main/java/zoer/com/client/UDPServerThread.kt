package zoer.com.client

import android.os.AsyncTask
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.Charset

class UDPServerThread (activity:ConnectActivity): AsyncTask<Unit,String,Unit>() {
    var mListener:ListChangedListener=activity

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        ipList.addUnique(values[0])
        mListener.elementChanged()
    }

    override fun doInBackground(vararg p0: Unit?) {

        var ds: DatagramSocket? = null
        var msg: String
        try {
            ds = DatagramSocket(11111, InetAddress.getByName(SERVER_BROADCAST_PORT))
            ds.broadcast = true

            var lMsg = ByteArray(20)
            val dp = DatagramPacket(lMsg, lMsg.size)
            while(udpActive) {
                ds.receive(dp)
                lMsg = lMsg.trimZero()
                msg = lMsg.toString(Charset.defaultCharset()).trimEnd()
                if (msg.contains("Muserver")) {
                    val address = dp.address.hostAddress
                    val port= msg.split("port=")[1]
                        publishProgress("$address:$port")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (ds != null) {
                ds.close()
            }
        }
    }

}


fun ArrayList<String>.addUnique(element:String?):Boolean {
    if(element!=null && !this.contains(element)){
        this.add(element)
        return true
    }
    return false
}
fun ByteArray.trimZero():ByteArray{
    val zeroByte:Byte=0
    return this.filter { it != zeroByte }.toByteArray()
}