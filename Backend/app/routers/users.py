from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select
from app.database import get_session
from app.models import User, Participant, Payment

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/", response_model=list[User])
def get_users(session: Session = Depends(get_session)):
    return session.exec(select(User)).all()


@router.get("/{user_id}", response_model=User)
def get_user(user_id: int, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.get("/by-email/{email}", response_model=User)
def get_user_by_email(email: str, session: Session = Depends(get_session)):
    user = session.exec(select(User).where(User.email == email)).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.post("/login")
def login(email: str, password: str, session: Session = Depends(get_session)):
    user = session.exec(select(User).where(User.email == email)).first()
    if user and user.password == password:
        return {"success": True}
    return {"success": False}


@router.post("/", response_model=User)
def create_user(user: User, session: Session = Depends(get_session)):
    session.add(user)
    session.commit()
    session.refresh(user)
    return user

# conecta al un usuario como invitado a una salad de pago
@router.get("/{user_id}/pending-payment")
def get_pending_payment(user_id: int, session: Session = Depends(get_session)):
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
        "amount": participant.amount
    }