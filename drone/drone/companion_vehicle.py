from threading import Thread
from time import sleep
from dronekit import Vehicle, connect, SystemStatus
from playsound import playsound
from multiprocessing import Process
from pathlib import Path
import os

root = Path(__file__).parent.parent

print(root)

class CompanionVehicle:
    vehicle: Vehicle = None
    sound_instance: Process = None
    
    def __init__(self) -> None:
        Thread(target=self.connect).start()
        
    def connect(self):
        while True:
            if self.vehicle is None:
                try:
                    print("Connecting to drone at localhost...")
                    self.vehicle = connect("127.0.0.1:14550", wait_ready=True)
                    print("Drone connected")
                    playsound(os.path.join(root, "start.mp3"))
                    self.vehicle.add_attribute_listener("system_status", self.on_vehicle_status_change)
                    print("Waiting changes...")
                except Exception as e:
                    print(e)
                    if self.vehicle is not None:
                        self.vehicle.close()
                        self.vehicle = None
            sleep(2)
            
    def on_vehicle_status_change(self, attr_name: str, aux: any, status: SystemStatus):
        if status.state == "ACTIVE":
            if self.sound_instance is None:
                self.sound_instance = Process(target=self.play_sound)
                self.sound_instance.start()
        else:
            self.sound_instance.terminate()
        
    def play_sound(self):
        while True:
            playsound(os.path.join(root, "sound.mp3"))