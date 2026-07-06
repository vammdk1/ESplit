from datetime import datetime, timedelta

from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from sqlmodel import Session

from app.database import get_session
from app.models import User

SECRET_KEY = "TU_CLAVE_SECRETA"
ALGORITHM = "HS256"
TOKEN_EXPIRY_HOURS = 1

security = HTTPBearer()


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