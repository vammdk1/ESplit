from typing import Dict, List
from fastapi import WebSocket


class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[int, List[WebSocket]] = {}

    async def connect(self, payment_id: int, websocket: WebSocket):
        await websocket.accept()
        if payment_id not in self.active_connections:
            self.active_connections[payment_id] = []
        self.active_connections[payment_id].append(websocket)

    def disconnect(self, payment_id: int, websocket: WebSocket):
        if payment_id in self.active_connections:
            self.active_connections[payment_id].remove(websocket)
            if not self.active_connections[payment_id]:
                del self.active_connections[payment_id]

    async def broadcast(self, payment_id: int, message: dict):
        if payment_id in self.active_connections:
            for connection in self.active_connections[payment_id]:
                await connection.send_json(message)


manager = ConnectionManager()
