from threading import Thread
from time import sleep
from dronekit import Vehicle, connect, SystemStatus
from playsound import playsound
from multiprocessing import Process

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
                    self.vehicle = connect("tcp:127.0.0.1:5760", wait_ready=True)
                    print("Drone connected")
                    self.vehicle.add_attribute_listener("system_status", self.on_vehicle_status_change)
                except:
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
            playsound("sound.mp3")