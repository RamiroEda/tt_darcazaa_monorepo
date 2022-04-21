package mx.ipn.upiiz.darcazaa.data.models

import org.json.JSONObject
import java.lang.Exception

class SafeJsonObject(value: String) {
    private val json = JSONObject(value)

    fun getInt(name: String): Int? {
        return try {
            json.getInt(name)
        }catch (e: Exception){
            null
        }
    }

    fun getDouble(name: String): Double? {
        return try {
            json.getDouble(name)
        }catch (e: Exception){
            null
        }
    }

    fun getString(name: String): String? {
        return try {
            json.getString(name)
        }catch (e: Exception){
            null
        }
    }

    fun getJSONObject(name: String): JSONObject? {
        return try {
            json.getJSONObject(name)
        }catch (e: Exception){
            null
        }
    }
}