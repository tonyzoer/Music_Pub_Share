package zoer.com.client

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Created by mafio on 3/17/2018.
 */
class UDPServerThread : Thread() {
    override fun run() {
        super.run()

        var ds: DatagramSocket? = null
        var msg: String
        var address: String?
        try {
            ds = DatagramSocket(11111, InetAddress.getByName("192.168.1.255"))
            ds.broadcast = true

            val lMsg = ByteArray(4096)
            val dp = DatagramPacket(lMsg, lMsg.size)
            ds.receive(dp)
            msg = lMsg.toString()
                var address = dp.address.hostAddress
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (ds != null) {
                ds.close()
            }
        }
    }

}