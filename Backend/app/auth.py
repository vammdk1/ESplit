from datetime import datetime, timedelta

from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from sqlmodel import Session

from app.database import get_session
from app.models import User, Payment

SECRET_KEY = "TU_CLAVE_SECRETA"
ALGORITHM = "HS256"
TOKEN_EXPIRY_HOURS = 1

security = HTTPBearer()

# Función para crear un token JWT para un usuario
def create_token(user_id: int) -> str:
    expiry = datetime.utcnow() + timedelta(hours=TOKEN_EXPIRY_HOURS)

    return jwt.encode(
        {
            "user_id": user_id,
            "exp": expiry
        },
        SECRET_KEY,
        algorithm=ALGORITHM
    )

# Función para obtener el usuario actual a partir del token JWT
def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    session: Session = Depends(get_session)
) -> User:

    token = credentials.credentials

    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Token expirado o inválido")

    user = session.get(User, user_id)

    if not user:
        raise HTTPException(status_code=401, detail="Usuario no encontrado")

    if user.actual_token != token:
        raise HTTPException(status_code=401, detail="Sesión iniciada en otro dispositivo")

    return user

# Función para verificar si un usuario es miembro de una sala de pago
def verify_room_membership(payment_id: int, current_user: User, session: Session):
    payment = session.get(Payment, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")
    participant_ids = [p.user_id for p in payment.participants]
    if current_user.id not in participant_ids:
        raise HTTPException(status_code=403, detail="No eres participante de esta sala")
    return payment