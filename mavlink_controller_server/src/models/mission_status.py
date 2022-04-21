from enum import Enum

class MissionStatus(Enum):
    STARTING = 'STARTING'
    FLYING = 'FLYING'
    LANDING = 'LANDING'
    IDLE = 'IDLE'
    CRITICAL = "CRITICAL"
    CANCELING = "CANCELING"
    WAITING_FOR_BATTERY = "WAITING_FOR_BATTERY"
    WAITING_FOR_WEATHER = "WAITING_FOR_WEATHER"