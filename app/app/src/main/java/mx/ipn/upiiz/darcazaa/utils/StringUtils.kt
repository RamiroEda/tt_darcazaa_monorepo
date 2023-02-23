package mx.ipn.upiiz.darcazaa.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build


fun String.isValidIP() = matches(Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:[0-9]{1,4})?\$"))

fun Context.connectToWifiNetwork(
    ssid: String,
    pass: String?
){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass ?: "")
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()
        val connectivityManager =
            this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, ConnectivityManager.NetworkCallback())
    } else {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = java.lang.String.format("\"%s\"", ssid)
        wifiConfig.preSharedKey = java.lang.String.format("\"%s\"", pass)

        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager?
        val netId = wifiManager!!.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
    }
}