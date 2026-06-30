from typing import Optional, List, Set
from sqlmodel import SQLModel, Field, Relationship
from datetime import datetime
import random


_generated_card_numbers: Set[str] = set()


def generate_fake_card_number() -> str:
    while True:
        number = "".join(str(random.randint(0, 9)) for _ in range(16))
        if number not in _generated_card_numbers:
            _generated_card_numbers.add(number)
            return number


class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    email: str
    funds: float = 0.0
    password: str
    card_number: str = Field(default_factory=generate_fake_card_number)


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


# ---- Modelos solo para entrada/salida de la API ----

class ParticipantCreate(SQLModel):
    user_id: int
    name: str
    amount: float


class PaymentCreate(SQLModel):
    total_amount: float
    participants: List[ParticipantCreate]

# ---- Modelos para gestionar el websocket -----
class PaymentCreateEmpty(SQLModel):
    total_amount: float


class ParticipantAdd(SQLModel):
    user_id: int
    name: str
    amount: float