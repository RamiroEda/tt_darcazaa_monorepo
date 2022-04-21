package mx.ipn.upiiz.darcazaa.data.models

enum class SystemStatus(val message: String) {
    STARTING("Iniciando"),
    FLYING("Volando"),
    LANDING("Aterrizando"),
    IDLE("Inactivo"),
    CRITICAL("Critico"),
    CANCELING("Cancelando"),
    WAITING_FOR_BATTERY("Esperando batería"),
    WAITING_FOR_WEATHER("Esperando clima"),
    UNKNOWN("Desconocido");

    companion object {
        fun safeByName(name: String?) = values().find { it.name == name } ?: UNKNOWN
    }
}