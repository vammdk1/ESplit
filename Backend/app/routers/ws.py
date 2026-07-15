from fastapi import APIRouter, Query, WebSocket, WebSocketDisconnect, Depends
from jose import jwt, JWTError
from sqlmodel import select
from app.auth import SECRET_KEY, ALGORITHM
from app.connection_manager import manager
from app.database import get_session
from app.models import Participant

router = APIRouter()


@router.websocket("/ws/payments/{payment_id}")
async def payment_socket(
    websocket: WebSocket, 
    payment_id: int,
    token: str = Query(...)  # Token JWT
):
    # verificar token para aceptar la conexion
    try :
        pyload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = pyload["user_id"]
    except JWTError:
        await websocket.close(code=1008)  # Código de cierre para "Policy Violation"
        return
    
    # Verificar que el usuario es miembro de la sala de pago
    with next(get_session()) as session:
        participant = session.exec(
            select(Participant).where(
                Participant.payment_id == payment_id,
                Participant.user_id == user_id
            )
        ).first()
        if not participant:
            await websocket.close(code=1008)  # Código de cierre para "Policy Violation"
            return
    
    # Conexión original del websocket, ahora que el usuario ha sido verificado
    await manager.connect(payment_id, websocket)
    try:
        while True:
            data = await websocket.receive_json()

            if data.get("type") == "confirm_response":
                # Actualiza el estado en la base de datos
                with next(get_session()) as session:
                    participant = session.exec(
                        select(Participant).where(
                            Participant.payment_id == payment_id,
                            Participant.user_id == data["user_id"]
                        )
                    ).first()
                    if participant:
                        participant.confirmation_status = data["accepted"]
                        session.add(participant)
                        session.commit()

            # Reenvía el mensaje a todos en la sala (host y participantes)
            await manager.broadcast(payment_id, data)
    except WebSocketDisconnect:
        manager.disconnect(payment_id, websocket)