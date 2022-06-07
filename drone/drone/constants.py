import os

CAMERA_INDEX = int(os.environ["CAMERA_INDEX"]) if "CAMERA_INDEX" in os.environ is not None else 2
