from fastapi import FastAPI
from app.database import create_db_and_tables
from app.routers import users, payments, ws

app = FastAPI(title="E-Split API")


@app.on_event("startup")
def on_startup():
    create_db_and_tables()


app.include_router(users.router)
app.include_router(payments.router)
app.include_router(ws.router)


@app.get("/")
def root():
    return {"status": "ok"}