package mx.ipn.upiiz.darcazaa.data.data_providers

import io.socket.client.IO

val ioOptions = IO.Options.builder()
    .setTransports(arrayOf("websocket"))
    .build()