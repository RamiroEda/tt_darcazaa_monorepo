from math import sqrt
from threading import Thread
from time import sleep, time
import numpy as np
import cv2
import cv2.aruco as aruco
from dronekit import VehicleMode
from constants import CAMERA_INDEX

class LandingAssistant:
    camera: cv2.VideoCapture = cv2.VideoCapture(CAMERA_INDEX)
    dictionary = aruco.Dictionary_get(aruco.DICT_4X4_250)
    params = aruco.DetectorParameters_create()
    debug: bool
    can_scan: bool = False
    last_marker = None

    def __init__(self, vehicle: any, debug = False) -> None:
        self.debug = debug
        self.vehicle = vehicle
        Thread(target=self.capture_video).start()
        
    def enable(self, enabled: bool):
        print("📷 Scanning status: %s" % enabled)
        self.can_scan = enabled
        
    def find_marker(self, frame):
        grayscale = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        
        (box, ids, _) = aruco.detectMarkers(grayscale, self.dictionary, parameters = self.params)
        if self.debug:
            aruco.drawDetectedMarkers(frame, box, ids)
            
        id = np.amin(ids)
        value = None

        if id is not None:
            indexes = np.where(ids == id)[0]

            if len(indexes) > 0:
                index = indexes[0]
                value = box[index][0]
        
        
        return id, value
        
    def capture_video(self):
        while True:
            if self.can_scan:
                self.capture_frame()
                sleep(0.033)
            else:
                sleep(1)
    
    def capture_frame(self):
        (_, frame) = self.camera.read()
            
        if frame is not None:
            id, marker = self.find_marker(frame)
            
            offset = None
            
            if marker is not None:
                offset = self.marker_offset(frame, marker)
                
                self.center_vehicle(offset, id)
                

            if self.debug:
                cv2.waitKey(1)
                cv2.imshow("frame", frame)

        
        
        
    def marker_offset(self, frame, marker):
        (h, w, _) = frame.shape
        center_marker = [abs((marker[0][0] + marker[2][0]) / 2), abs((marker[0][1] + marker[2][1]) / 2)]
        center_image = [w/2, h/2]
        
        avg_distance = (self.distance_between_vertices(marker[0], marker[1])
            + self.distance_between_vertices(marker[1], marker[2])
            + self.distance_between_vertices(marker[2], marker[3])
            + self.distance_between_vertices(marker[3], marker[0])) / 4
        
        
        offx = center_marker[0] - center_image[0]
        offy = center_marker[1] - center_image[1]
        
        return [(offx * 10) / avg_distance, (offy * 10) / avg_distance]
    
        
    def distance_between_vertices(self, v1, v2) -> float:
        return sqrt(pow(v1[0] - v2[0], 2) + pow(v1[1] - v2[1], 2))
        
    def center_vehicle(self, offset, id):
        vx = 0
        vy = 0
        
        if offset is not None:
            vx = offset[0]
            vy = offset[1]
            
        print("🛬 Landing in %i... (%f, %f)" % (id, vx, vy))
        
        if self.vehicle is not None:
            if self.vehicle.get_mode().name != "LAND":
                self.vehicle.set_mode(VehicleMode("LAND"))
            self.vehicle.send_land_message(vx, vy, time()*1e6)
            

# if __name__ == "__main__":
#     LandingAssistant(None, debug=True).can_scan = True