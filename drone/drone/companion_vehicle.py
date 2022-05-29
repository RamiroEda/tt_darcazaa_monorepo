import math
from threading import Thread
from time import sleep
from dronekit import Vehicle, connect, VehicleMode, SystemStatus, CommandSequence
from multiprocessing import Process
from pathlib import Path
import os
from pymavlink import mavutil
from landing_assistant import LandingAssistant

root = Path(__file__).parent.parent

class CompanionVehicle:
    vehicle: Vehicle = None
    sound_instance: Process = None
    landing_assistant: LandingAssistant = None
    
    def __init__(self) -> None:
        Thread(target=self.connect).start()
        
    def connect(self):
        while True:
            if self.vehicle is None:
                try:
                    print("🧩 Connecting to drone at localhost...")
                    self.vehicle = connect("tcp:192.168.1.230:5762", wait_ready=True) # 127.0.0.1:14550
                    self.vehicle.commands.download()
                    self.vehicle.commands.wait_ready()
                    print("✅ Drone connected")
                    
                    self.landing_assistant = LandingAssistant(self)
                    self._omx_play(os.path.join(root, "start.mp3"))
                    
                    self.vehicle.add_message_listener("MISSION_ITEM_REACHED", self.on_commands_update)
                    self.vehicle.add_attribute_listener("system_status", self.on_vehicle_status_change)
                    if self.vehicle.commands.next == self.vehicle.commands.count:
                        self.on_last_wp()
                    
                    self.on_vehicle_status_change(None, None, self.vehicle.system_status)
                except Exception as e:
                    print(e)
                    if self.vehicle is not None:
                        self.vehicle.close()
                        self.vehicle = None
            sleep(2)
            
    def set_mode(self, mode: VehicleMode):
        self.vehicle.mode = mode
    
    def get_mode(self) -> VehicleMode:
        return self.vehicle.mode
    
    def on_last_wp(self):
        self.landing_assistant.enable(True)
        
    def on_commands_update(self, attr_name: str, aux: any, reached):
        print("🛩️ Current waypoint: %i" % (reached.seq + 1))
        print("🛩️ Total waypoints:  %i" % self.vehicle.commands.count)
        
        if(self.vehicle is None):
            return
        
        if(reached.seq + 1 == self.vehicle.commands.count):
            self.on_last_wp()
            
    def marker_position_to_angle(self, x, y, z):
        angle_x = math.atan2(x,z)
        angle_y = math.atan2(y,z)
        
        return (angle_x, angle_y)
        
    def send_land_message(self, x_pos=0., y_pos=0., sensor_time=0):
        z_dist = self.vehicle.location.global_relative_frame.alt
        x_rad, y_rad = self.marker_position_to_angle(x_pos, y_pos, z_dist)

        self.vehicle.send_mavlink(self.vehicle.message_factory.landing_target_encode(
            int(sensor_time), 
            69, 
            mavutil.mavlink.MAV_FRAME_BODY_NED, 
            x_rad, 
            y_rad, 
            z_dist, 
            0, 0, 
            0, 0, 0,
            [1,0,0,0], 
            mavutil.mavlink.LANDING_TARGET_TYPE_VISION_FIDUCIAL, 
            1
        ))
            
    def on_vehicle_status_change(self, attr_name: str, aux: any, status: SystemStatus):
        if status.state == "ACTIVE":
            if self.sound_instance is None:
                self.sound_instance = Process(target=self.play_sound)
                self.sound_instance.start()
        elif status.state == "STANDBY":
            self.landing_assistant.enable(False)
        else:
            self.sound_instance.terminate()
            
    def _omx_play(slef, path: str):
        os.system("omxplayer %s" % path)
        
    def play_sound(self):
        try:
            while True:
                self._omx_play(os.path.join(root, "sound.mp3"))
        except:
            pass