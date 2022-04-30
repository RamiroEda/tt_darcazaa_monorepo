package mx.ipn.upiiz.darcazaa.data.models

import io.socket.emitter.Emitter
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.ConcurrentSkipListSet

class WebSocketStreamCollector: Emitter.Listener {
    private val wssData = ConcurrentSkipListSet<ByteString>()

    fun onClosing() {
        wssData.removeAll(wssData)
    }

    fun canStream(): Boolean {
        return wssData.size > 0
    }

    fun getNextStream(): ByteString {
        return wssData.pollFirst()
    }

    override fun call(vararg args: Any?) {
        (args.firstOrNull() as? ByteArray?)?.let {
            wssData.add(it.toByteString())
        }
    }
}