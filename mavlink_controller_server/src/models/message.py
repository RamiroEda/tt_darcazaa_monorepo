import json
import sys

class Message:
    def __init__(self, payload: any = {}):
        self.type = type.value
        self.payload = payload

    def send(self):
        sys.stdout.write(json.dumps({
            'type': self.type,
            'payload': self.payload
        }))
        sys.stdout.flush()

        