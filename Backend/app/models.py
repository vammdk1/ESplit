from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from datetime import datetime


class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    email: str
    funds: float = 0.0
    password: str


class Participant(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="user.id")
    payment_id: Optional[int] = Field(default=None, foreign_key="payment.id")
    name: str
    amount: float
    confirmation_status: bool = False

    payment: Optional["Payment"] = Relationship(back_populates="participants")


class Payment(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    total_amount: float
    created_at: datetime = Field(default_factory=datetime.utcnow)
    payment_status: bool = False

    participants: List[Participant] = Relationship(back_populates="payment")


# ---- Modelos solo para entrada/salida de la API (no son tablas) ----

class ParticipantCreate(SQLModel):
    user_id: int
    name: str
    amount: float


class PaymentCreate(SQLModel):
    total_amount: float
    participants: List[ParticipantCreate]