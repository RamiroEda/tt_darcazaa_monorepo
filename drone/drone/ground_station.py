from threading import Thread
from time import sleep
from pymavlink import mavutil
from pymavlink.mavutil import mavserial

class GroundStation:
    ground_station: mavserial = None
    
    def __init__(self) -> None:
        # Thread(target=self.connect).start()
        self.connect()
    
    # "/dev/ttyAMA0" "com4"
    def connect(self):
        self.ground_station = mavutil.mavlink_connection("/dev/ttyAMA0", 57600, autoreconnect=True)
        self.ground_station.wait_heartbeat()
        print("Conectado")
        while True:
            self.send_camera_buffer(bytes("Hola mundo00000000000", encoding="utf8"))
            sleep(1)
        
    def send_camera_buffer(self, buffer: bytearray):
        print("Paquete mandado")
        self.ground_station.mav.data16_send(0, len(buffer), buffer)