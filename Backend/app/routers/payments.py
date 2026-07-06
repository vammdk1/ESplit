from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import SQLModel, Session, select
from app.database import get_session
from app.models import Payment, Participant, User
from app.connection_manager import manager
from app.auth import create_token, get_current_user


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

class ParticipantOut(SQLModel):
    user_id: int
    name: str
    amount: float
    confirmation_status: bool

@router.get("/{payment_id}/participants", response_model=list[ParticipantOut])
def get_participants(payment_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    participants = session.exec(
        select(Participant).where(Participant.payment_id == payment_id)
    ).all()
    return [
        ParticipantOut(
            user_id=p.user_id,
            name=p.name,
            amount=p.amount,
            confirmation_status=p.confirmation_status
        )
        for p in participants
    ]

@router.get("/{payment_id}", response_model=Payment)
def get_payment(payment_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    payment = session.get(Payment, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")
    return payment

@router.put("/{payment_id}/participants/{user_id}")
def update_participant_amount(payment_id: int, user_id: int, data: ParticipantUpdate, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
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
def create_payment(payment_data: PaymentCreateEmpty, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
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

#Broadcast de cierre de pago
@router.post("/{payment_id}/pay", response_model=PayResponse)
async def pay(payment_id: int, amount_to_pay: float, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
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

    await manager.broadcast(payment_id, {"type": "payment_completed"})

    return PayResponse(success=True, payment_status=payment.payment_status)

@router.post("/tpv/charge")
async def tpv_charge(card_number: str, amount: float, session: Session = Depends(get_session)):
    # 1. Busca el usuario por card_number
    user = session.exec(select(User).where(User.card_number == card_number)).first()
    if not user:
        raise HTTPException(status_code=404, detail="Card not found")

    # 2. Busca el Payment activo del host con todos los participantes confirmados
    payment = session.exec(
        select(Payment).where(
            Payment.payment_status == False
        )
    ).all()

    # filtra el payment donde el host (user_id) es participante
    active_payment = None
    for p in payment:
        for participant in p.participants:
            if participant.user_id == user.id:
                active_payment = p
                break
        if active_payment:
            break

    if not active_payment:
        raise HTTPException(status_code=404, detail="No active payment found for this card")

    # 3. Verifica que el monto coincide
    if active_payment.total_amount != amount:
        return {"success": False, "reason": "Amount mismatch"}

    # 4. Verifica que todos los participantes han confirmado
    for participant in active_payment.participants:
        if participant.user_id == user.id:
            continue  # el host no necesita confirmarse a sí mismo
        if not participant.confirmation_status:
            return {"success": False, "reason": "Not all participants confirmed"}

    # 5. Procesa el pago
    for participant in active_payment.participants:
        u = session.get(User, participant.user_id)
        if u:
            if u.funds < participant.amount:
                return {"success": False, "reason": f"Insufficient funds for {u.name}"}
            u.funds -= participant.amount
            session.add(u)

    active_payment.payment_status = True
    session.add(active_payment)
    session.commit()

    # 6. Notifica a todos por WebSocket
    await manager.broadcast(active_payment.id, {"type": "payment_completed"})

    return {"success": True, "payment_id": active_payment.id}

@router.delete("/")
def delete_all_payments(session: Session = Depends(get_session)):
    participants = session.exec(select(Participant)).all()
    for participant in participants:
        session.delete(participant)

    payments = session.exec(select(Payment)).all()
    for payment in payments:
        session.delete(payment)

    session.commit()

    return {
        "success": True,
        "message": "All payments deleted"
    }

@router.delete("/{payment_id}")
def delete_payment(payment_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    payment = session.get(Payment, payment_id)

    if not payment:
        raise HTTPException(status_code=404, detail="Payment not found")

    for participant in payment.participants:
        session.delete(participant)

    session.delete(payment)
    session.commit()

    return {
        "success": True,
        "message": f"Payment {payment_id} deleted"
    }

@router.delete("/{payment_id}/participants/{user_id}")
def remove_participant(payment_id: int, user_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    participant = session.exec(
        select(Participant).where(
            Participant.payment_id == payment_id,
            Participant.user_id == user_id
        )
    ).first()

    if not participant:
        raise HTTPException(status_code=404, detail="Participant not found")

    session.delete(participant)
    session.commit()

    return {"success": True}
