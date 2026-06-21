from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import SQLModel, Session, select
from app.database import get_session
from app.models import Payment, Participant, User
from app.connection_manager import manager

router = APIRouter(prefix="/payments", tags=["payments"])


class PaymentCreateEmpty(SQLModel):
    total_amount: float


class ParticipantAdd(SQLModel):
    user_id: int
    name: str
    amount: float


class PaymentResponse(SQLModel):
    id: int
    payment_status: bool


class PayResponse(SQLModel):
    success: bool
    payment_status: bool

class ParticipantUpdate(SQLModel):
    user_id: int
    amount: float

@router.put("/{payment_id}/participants/{user_id}")
def update_participant_amount(payment_id: int, user_id: int, data: ParticipantUpdate, session: Session = Depends(get_session)):
    participant = session.exec(
        select(Participant).where(
            Participant.payment_id == payment_id,
            Participant.user_id == user_id
        )
    ).first()

    if not participant:
        raise HTTPException(status_code=404, detail="Participant not found")

    participant.amount = data.amount
    participant.confirmation_status = False  # resetea, debe re-confirmar
    session.add(participant)
    session.commit()

    return {"success": True}


@router.post("/", response_model=PaymentResponse)
def create_payment(payment_data: PaymentCreateEmpty, session: Session = Depends(get_session)):
    payment = Payment(total_amount=payment_data.total_amount)
    session.add(payment)
    session.commit()
    session.refresh(payment)
    return payment


@router.post("/{payment_id}/participants", response_model=Participant)
def add_participant(payment_id: int, data: ParticipantAdd, session: Session = Depends(get_session)):
    payment = session.get(Payment, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")

    participant = Participant(
        user_id=data.user_id,
        name=data.name,
        amount=data.amount,
        payment_id=payment_id
    )
    session.add(participant)
    session.commit()
    session.refresh(participant)
    return participant


@router.get("/{payment_id}", response_model=Payment)
def get_payment(payment_id: int, session: Session = Depends(get_session)):
    payment = session.get(Payment, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")
    return payment


@router.post("/{payment_id}/pay", response_model=PayResponse)
def pay(payment_id: int, amount_to_pay: float, session: Session = Depends(get_session)):
    payment = session.get(Payment, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")

    if payment.total_amount != amount_to_pay:
        return PayResponse(success=False, payment_status=payment.payment_status)

    for participant in payment.participants:
        user = session.get(User, participant.user_id)
        if user:
            user.funds -= participant.amount
            session.add(user)

    payment.payment_status = True
    session.add(payment)
    session.commit()
    session.refresh(payment)

    return PayResponse(success=True, payment_status=payment.payment_status)

