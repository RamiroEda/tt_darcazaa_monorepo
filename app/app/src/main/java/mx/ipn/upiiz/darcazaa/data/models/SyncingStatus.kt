package mx.ipn.upiiz.darcazaa.data.models

enum class SyncingStatus {
    UNKNOWN,
    SYNCING,
    SYNCED,
    ERROR;

    companion object{
        fun safeValueOf(name: String?) = values().find { it.name == name } ?: UNKNOWN
    }
}