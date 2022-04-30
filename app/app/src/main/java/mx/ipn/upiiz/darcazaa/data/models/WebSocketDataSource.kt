package mx.ipn.upiiz.darcazaa.data.models

import android.net.Uri
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Inject
import kotlin.math.min

class WebSocketDataSource @Inject constructor(): BaseDataSource(true) {
    private val dataStreamCollector = WebSocketStreamCollector()

    private var webSocketClient: Socket? = null
    var dataSpec: DataSpec? = null

    private var currentByteStream: ByteArray? = null
    private var currentPosition = 0
    private var remainingBytes = 0

    override fun open(dataSpec: DataSpec): Long {
        // Form the request and open the socket.
        // Provide the listener
        // which collects the data for us (Previous class).
        this.dataSpec = dataSpec

        webSocketClient = IO.socket(
            dataSpec.uri.toString(),
            IO.Options
                .builder()
                .setExtraHeaders(dataSpec.httpRequestHeaders.mapValues {
                    listOf(it.value)
                })
                .build()
        ).connect()

        webSocketClient?.on("message", dataStreamCollector)

        return -1 // Return -1 as the size is unknown (streaming)
    }

    override fun getUri(): Uri? = dataSpec?.uri

    override fun read(target: ByteArray, offset: Int, length: Int): Int {
        // return 0 (nothing read) when no data present...
        if (currentByteStream == null && !dataStreamCollector.canStream()) {
            return 0
        }

        // parse one (data) ByteString at a time.
        // reset the current position and remaining bytes
        // for every new data
        if (currentByteStream == null) {
            currentByteStream = dataStreamCollector.getNextStream().toByteArray()
            currentPosition = 0
            remainingBytes = currentByteStream?.size ?: 0
        }

        val readSize = min(length, remainingBytes)

        currentByteStream?.copyInto(target, offset, currentPosition, currentPosition + readSize)
        currentPosition += readSize
        remainingBytes -= readSize

        // once the data is read set currentByteStream to null
        // so the next data would be collected to process in next
        // iteration.
        if (remainingBytes == 0) {
            currentByteStream = null
        }

        return readSize
    }

    override fun close() {
        // close the socket and relase the resources
        dataStreamCollector.onClosing()
        webSocketClient?.close()
    }

    // Factory class for DataSource
    class Factory : DataSource.Factory {
        override fun createDataSource(): DataSource = WebSocketDataSource()
    }
}