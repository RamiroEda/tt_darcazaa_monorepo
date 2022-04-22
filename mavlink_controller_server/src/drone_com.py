import geopy.distance
from datetime import datetime
from threading import Thread
from time import sleep
from typing import Callable, List, Tuple
from dronekit import Battery, Command, LocationGlobalRelative, Locations, SystemStatus, connect, VehicleMode, Vehicle, CommandSequence, LocationGlobal
from pymavlink import mavutil
from models.lat_lng import LatLng
from models.mission_status import MissionStatus
from pymavlink.dialects.v20 import common
from constants import DRONE_ALTITUDE, DRONE_IP, MAX_WINDSPEED, MINIMUM_BATTERY, MINIMUM_BATTERY_TO_START, WAYPOINT_ACCEPTANCE_RADIUS

class DroneCom:
    send_message_listeners: List[Callable[[str, any], None]] = []
    is_cancel = False
    vehicle: Vehicle = None
    windspeed: float = 0.0
    
    vehicle_start_mission_battery: Tuple[datetime, float] = None
    
    def __init__(self, debug: bool):
        self.debug = debug
        Thread(target=self._worker).start()
        
    def _worker(self):
        while(True):
            if(self.vehicle is None):
                self.connect()
            sleep(1)

    def parse_mission(self, data: any) -> List[LatLng]:
        points: List[LatLng] = []
        waypoints: List[any] = data
        
        waypoints.sort(key=lambda wp: wp["index"])

        for item in waypoints:
            points.append(LatLng(item["latitude"], item["longitude"]))

        return points

    def on_location_update(self, attr_name: str, loc: Locations, location: LocationGlobalRelative):
        if(self.vehicle is None):
            return
        self.send_message("location", {
            "latitude": location.lat,
            "longitude": location.lon,
            "altitude": location.alt,
            "speed": self.vehicle.airspeed,
            "heading": self.vehicle.heading
        })

    def on_battery_update(self, attr_name: str, aux: any, battery: Battery):
        if(self.vehicle is None):
            return
        self.send_message("battery", {
            "current": battery.current,
            "level": battery.level,
            "voltage": battery.voltage
        })
        
        if self.vehicle_start_mission_battery is not None:
            (start, level) = self.vehicle_start_mission_battery
            timediff = datetime.now() - start
            leveldiff = battery.level - level
            
            if timediff.seconds == 0:
                return
            
            discharge_speed = leveldiff / timediff.seconds
            
            if discharge_speed == 0:
                return
            
            until_discharge = battery.level - 5 / discharge_speed
            
            if self.vehicle.airspeed == 0:
                return
            
            time_to_home = self.distance_between_locations(self.vehicle.home_location, self.vehicle.location.global_frame) / self.vehicle.airspeed
                        
            if until_discharge <= time_to_home and not self.is_cancel:
                self.print("🔋 Not enough battery. Time to home: %f. Time to discharge: %f" % (time_to_home, until_discharge))
                self.cancel_mission()
    

    def on_commands_update(self, attr_name: str, aux: any, commands: CommandSequence):
        if(self.vehicle is None):
            return
        if(commands.next == commands.count):
            if not self.is_cancel:
                self.send_message("status", MissionStatus.LANDING.value)
            else:
                self.send_message("status", MissionStatus.CANCELING.value)

    def on_system_status_update(self, attr_name: str, aux: any, status: SystemStatus):
        if(self.vehicle is None):
            return
        
        self.print("New status: %s" % status.state)
        
        self.send_message("system_status", {
            "status": status.state,
            "canceled": self.is_cancel
        })
        
        if status.state == "STANDBY":
            self.clear_commands()
            self.is_cancel = False
            self.vehicle_start_mission_battery = None
            self.send_message("current_mission", None)
            self.send_message("status", MissionStatus.IDLE.value)
        if status.state == "CRITICAL" or status.state == "EMERGENCY":
            self.cancel_mission()
            
    def on_camera_information_update(self, attr_name:str, message: common.MAVLink_camera_information_message):
        if message.flags and common.CAMERA_CAP_FLAGS_HAS_VIDEO_STREAM == 0 :
            print("⚠️ Video streaming not supported")
        else:
            self.vehicle.message_factory.command_long_encode(
                0, 0,
                common.MAV_CMD_REQUEST_MESSAGE,
                0,
                269, 0, 0, 0, 0, 0, 0
            )

    
    def on_video_stream_information_update(self, name: str, message: common.MAVLink_video_stream_information_message):
        self.send_message("camera_stream_uri", message.uri)
            
            
    def on_send_message(self, func: Callable[[str, any], None]):
        self.send_message_listeners.append(func)
        
        
    def translate_vehicle(self, velocity_x: float, velocity_y: float, velocity_z: float, relative = True):
        if relative:
            frame = mavutil.mavlink.MAV_FRAME_BODY_OFFSET_NED
        else:
            frame = mavutil.mavlink.MAV_FRAME_LOCAL_NED

        self.vehicle.send_mavlink(self.vehicle.message_factory.set_position_target_local_ned_encode(
            0, 0, 0,
            frame,
            0b0000111111000111,
            0, 0, 0,
            velocity_x, velocity_y, velocity_z,
            0, 0, 0,
            0, 0
        ))
  
        self.vehicle.flush()
    
        
    def rotate_vehicle(self, direction: int):
        self.vehicle.send_mavlink(self.vehicle.message_factory.command_long_encode(
            0, 0,
            mavutil.mavlink.MAV_CMD_CONDITION_YAW,
            0, 3, 0, direction, 1, 0, 0, 0))
        
    def clear_commands(self):
        cmds = self.vehicle.commands
        
        cmds.clear()
        cmds.next = 0
        cmds.upload()
        
        
        
    def set_mode(self, mode: str):
        self.vehicle.mode = VehicleMode(mode)
        
        self.print("⚙️ Mode changed: %s" % mode)
        
        if(mode == "AUTO"):
            self.print("✈️ Current command: %i. Total commands: %i" % (self.vehicle.commands.next, self.vehicle.commands.count))
        
        if mode == "AUTO" and self.vehicle.commands.count == 0:
            self.land()
        
    def set_windspeed(self, speed: float):
        self.windspeed = speed
        if self.windspeed > MAX_WINDSPEED:
            self.cancel_mission()
        
    def connect(self):
        try:
            self.print("🧩 Connecting to drone at %s" % DRONE_IP)
            self.vehicle = connect(DRONE_IP, wait_ready=True)
            self.print("✅ Drone connected. IP: %s" % DRONE_IP)
            self.vehicle.location.add_attribute_listener("global_relative_frame", self.on_location_update)
            self.vehicle.add_attribute_listener("system_status", self.on_system_status_update)
            self.vehicle.add_attribute_listener("battery", self.on_battery_update)
            self.vehicle.add_attribute_listener("commands", self.on_commands_update)
            self.vehicle.add_message_listener("CAMERA_INFORMATION", self.on_camera_information_update)
            self.vehicle.add_message_listener("VIDEO_STREAM_INFORMATION", self.on_video_stream_information_update)
        except:
            self.vehicle = None
            
    def init_camera(self):
        self.vehicle.send_mavlink(
            self.vehicle.message_factory.command_long_encode(
                0, 0,
                common.MAV_CMD_REQUEST_MESSAGE,
                0,
                259, 0, 0, 0, 0, 0, 0
            )
        )

    def run_mission(self, mission: any):
        if(self.vehicle is None):
            self.send_message("uav_error", {
                "message": "Drone not connected"
            })
            return

        self.init_camera()

        if self.vehicle.system_status.state != "STANDBY":
            return
        
        if self.windspeed > MAX_WINDSPEED:
            self.send_message("status", MissionStatus.WAITING_FOR_WEATHER.value)
            return

        if self.vehicle.battery.level >= MINIMUM_BATTERY_TO_START:
            self.send_message("status", MissionStatus.STARTING.value)
        else:
            self.send_message("status", MissionStatus.WAITING_FOR_BATTERY.value)
            return
        
        points = self.parse_mission(mission["waypoints"])
        self.print("🗺️  Points loaded: %i" % len(points))
        if(len(points) >= 3):
            self.send_message("current_mission", mission)
            self.upload_mission(points)
            self.arm_and_takeoff()
            self.vehicle.mode = VehicleMode("AUTO")

    def send_message(self, event: str, payload: any = {}):
        for func in self.send_message_listeners:
            func(event, payload)

    def cancel_mission(self):
        if self.vehicle.system_status.state == "STANDBY":
            return
            
        self.send_message("status", MissionStatus.CANCELING.value)
        cmds = self.vehicle.commands
        cmds.next = cmds.count

        self.is_cancel = True
        cmds.upload()
        self.print("❌ Routine canceled")
        
    def land(self):
        if(self.vehicle.system_status.state != "ACTIVE"):
            return
    
        self.clear_commands()
        
        home = self.vehicle.home_location
        
        if home is not None:
            cmds = self.vehicle.commands
            
            cmds.add(Command(
                0, 0, 0,
                mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,
                mavutil.mavlink.MAV_CMD_NAV_LAND,
                0, 0,
                0, 0, 0, 0, home.lat, home.lon, DRONE_ALTITUDE
            ))
            
            self.vehicle.commands.next = 0
            self.vehicle.commands.upload()
            
            self.vehicle.mode = VehicleMode("AUTO")
        
    
    def arm_and_takeoff(self):
        self.print("☑️ Basic pre-arm checks")
        while not self.vehicle.is_armable:
            sleep(1)

            
        self.print("⌛ Arming motors...")
        self.vehicle.mode = VehicleMode("GUIDED")
        self.vehicle.armed = True

        while not self.vehicle.armed:
            sleep(1)

        self.print("🛫 Taking off!")
        self.send_message("status", MissionStatus.FLYING.value)
        self.vehicle.simple_takeoff(DRONE_ALTITUDE)

        while True:
            self.print("✈️ Altitude: %sm." % self.vehicle.location.global_relative_frame.alt)      
            if self.vehicle.location.global_relative_frame.alt>=DRONE_ALTITUDE*0.95:
                self.print("✅ Reached target altitude")
                break
            sleep(1)
            
        self.vehicle_start_mission_battery = (datetime.now(), self.vehicle.battery.level)

    def print(
        self,
        message: str
    ) -> None:
        if self.debug:
            print(message)

    def upload_mission(self, points: List[LatLng]):
        self.print("🗑️ Clearing drone mission")
        home = None

        while home is None:
            self.vehicle.commands.download()
            self.vehicle.commands.wait_ready()
            home = self.vehicle.home_location
            cmds = self.vehicle.commands
            
        cmds.clear()
        cmds.next = 0
        self.print("🏠 Home at %s" % home)
        self.print("✈️ Generating mission")

        for point in points:
            cmds.add(Command(
                0, 0, 0,
                mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,
                mavutil.mavlink.MAV_CMD_NAV_WAYPOINT,
                0, 0,
                0, WAYPOINT_ACCEPTANCE_RADIUS, 0, 0, point.latitude, point.longitude, DRONE_ALTITUDE
            ))

        cmds.add(Command(
            0, 0, 0,
            mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,
            mavutil.mavlink.MAV_CMD_NAV_LAND,
            0, 0,
            0, 0, 0, 0, home.lat, home.lon, DRONE_ALTITUDE
        ))

        self.print("⌛ Uploading mission")
        cmds.upload()
        self.print("✅ Mission Uploaded. Commands: %i. Starting at: %i." % (cmds.count, cmds.next))
        
    def distance_between_locations(self, loc1: LocationGlobal, loc2: LocationGlobal) -> float:
        return geopy.distance.geodesic((loc1.lat, loc1.lon), (loc2.lat, loc2.lon)).meters
