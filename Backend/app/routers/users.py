from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select, delete
from app.database import get_session
from app.models import User, Participant, Payment
from app.auth import create_token, get_current_user
import bcrypt

router = APIRouter(prefix="/users", tags=["users"])

@router.get("/", response_model=list[User])
def get_users(session: Session = Depends(get_session)):
    return session.exec(select(User)).all()


@router.get("/{user_id}", response_model=User)
def get_user(
    user_id: int,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user)
):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.get("/by-email/{email}", response_model=User)
def get_user_by_email(email: str, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    user = session.exec(select(User).where(User.email == email)).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

# conecta al un usuario como invitado a una salad de pago
@router.get("/{user_id}/pending-payment")
def get_pending_payment(user_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    participant = session.exec(
        select(Participant).where(Participant.user_id == user_id)
        .join(Payment)
        .where(Payment.payment_status == False)
        .order_by(Participant.id.desc())
    ).first()

    if not participant:
        return {"has_invitation": False}

    return {
        "has_invitation": True,
        "payment_id": participant.payment_id,
        "amount": participant.amount,
        "room_amount": participant.payment.total_amount
    }

@router.get("/by-card/{card_number}")
def get_user_by_card(card_number: str, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    user = session.exec(select(User).where(User.card_number == card_number)).first()
    if not user:
        raise HTTPException(status_code=404, detail="Card not found")
    return {"user_id": user.id, "name": user.name}

#Login con token en lugar y contraseña cifrada en el servidor
@router.post("/login")
def login(email: str, password: str, session: Session = Depends(get_session), ):
    user = session.exec(select(User).where(User.email == email)).first()
    if user and bcrypt.checkpw(password.encode(), user.password.encode()):
        token = create_token(user.id)
        user.actual_token = token
        session.add(user)
        session.commit()
        return {"success": True, "token": token, "user_id": user.id}

    return {"success": False}

@router.post("/", response_model=User)
def create_user(user: User, session: Session = Depends(get_session)):
    user.password = bcrypt.hashpw(user.password.encode(), bcrypt.gensalt()).decode()
    session.add(user)
    session.commit()
    session.refresh(user)
    return user

# endpoint de desarrollo para el restablecimientod e usuarios post pruebas
@router.post("/reset")
def reset_users(session: Session = Depends(get_session)):
    session.exec(delete(User))
    session.commit()
    
    raw_users = [
        {"name": "Victor Garcia", "email": "victor@esplit.com", "funds": 125.50, "password": "admin1234"},
        {"name": "Laura Martinez", "email": "laura@esplit.com", "funds": 87.25, "password": "admin1234"},
        {"name": "Carlos Fernandez", "email": "carlos@esplit.com", "funds": 210.00, "password": "admin1234"},
        {"name": "Ana Lopez", "email": "ana@esplit.com", "funds": 54.75, "password": "admin1234"},
        {"name": "123", "email": "123", "funds": 1000, "password": "admin1234"},
        {"name": "MR ADMIN", "email": "admin@mail.com", "funds": 500, "password": "admin1234"},
    ]

    users = [
        User(
            name=u["name"],
            email=u["email"],
            funds=u["funds"],
            password=bcrypt.hashpw(u["password"].encode(), bcrypt.gensalt()).decode()
        )
        for u in raw_users
    ]

    session.add_all(users)
    session.commit()
    return {"success": True, "message": f"{len(users)} usuarios creados"}
    # Eliminar todos los usuarios
    session.exec(delete(User))
    session.commit()


    users = [
        User(
            name="Victor Garcia",
            email="victor@esplit.com",
            funds=125.50,
            password="admin1234"
        ),
        User(
            name="Laura Martinez",
            email="laura@esplit.com",
            funds=87.25,
            password="admin1234"
        ),
        User(
            name="Carlos Fernandez",
            email="carlos@esplit.com",
            funds=210.00,
            password="admin1234"
        ),
        User(
            name="Ana Lopez",
            email="ana@esplit.com",
            funds=54.75,
            password="admin1234"
        ),
        User(
            name="123",
            email="123",
            funds=1000,
            password="admin1234"
        ),
        User(
            name="MR ADMIN",
            email="admin@mail.com",
            funds=500,
            password="admin1234"
        ),
    ]

    session.add_all(users)
    session.commit()

    return {
        "success": True,
        "message": "Test users restored"
    }

# cierre de sesión, invalida el token actual del usuario
@router.post("/logout")
def logout(session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    current_user.actual_token = None
    session.add(current_user)
    session.commit()
    return {"success": True}