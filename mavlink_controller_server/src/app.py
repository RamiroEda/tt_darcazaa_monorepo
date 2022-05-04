from time import sleep
from flask import Flask
import socketio
from constants import FLASK_ENV, WEBSOCKET_SERVER
from drone_com import DroneCom

sleep(10)

app = Flask(__name__)
ws = socketio.Client()
mavlink = DroneCom(
    debug = FLASK_ENV != "production"
)

@ws.event(namespace="/routines")
def connect():
    print("✅ Server conection ready")

@ws.event(namespace="/routines")
def connect_error(data):
    print("❌ Server connection failed")

@ws.event(namespace="/routines")
def disconnect():
    print("❌ Server disconected")

@ws.on("*", namespace="/routines")
def mission(event: str, data: any):
    if mavlink.vehicle != None:
        if event == "mission":
            mavlink.run_mission(data)
        elif event == "cancel":
            mavlink.cancel_mission()
        elif event == "translate":
            mavlink.translate_vehicle(data["x"], data["y"], data["z"])
        elif event == "rotate":
            mavlink.rotate_vehicle(data)
        elif event == "change_mode":
            mavlink.set_mode(data)
        elif event == "takeoff":
            mavlink.arm_and_takeoff()
        elif event == "land":
            mavlink.land()
        elif event == "windspeed":
            mavlink.set_windspeed(data)
    
@mavlink.on_send_message
def send_message(event: str, message: any):
    ws.emit(event, message, namespace = "/routines")
    
print("🧩 Connecting to Websocket Server at %s" % WEBSOCKET_SERVER)
ws.connect(
    WEBSOCKET_SERVER, 
    namespaces=["/routines"],
)
