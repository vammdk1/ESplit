from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import SQLModel, Session
from app.database import get_session
from app.models import Payment, Participant, PaymentCreate, User

router = APIRouter(prefix="/payments", tags=["payments"])


class PaymentResponse(SQLModel):
    id: int
    payment_status: bool


class PayResponse(SQLModel):
    success: bool
    payment_status: bool


@router.post("/", response_model=PaymentResponse)
def create_payment(payment_data: PaymentCreate, session: Session = Depends(get_session)):
    payment = Payment(total_amount=payment_data.total_amount)
    session.add(payment)
    session.commit()
    session.refresh(payment)

    for p in payment_data.participants:
        participant = Participant(
            user_id=p.user_id,
            name=p.name,
            amount=p.amount,
            payment_id=payment.id
        )
        session.add(participant)

    session.commit()
    session.refresh(payment)
    return payment


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