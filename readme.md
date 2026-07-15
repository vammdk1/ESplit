# ESplit — Gestión de pagos grupales mediante NFC

ESplit es una aplicación Android para la gestión de pagos grupales. Permite fragmentar automáticamente un monto total entre varios participantes, coordinar confirmaciones en tiempo real y procesar el cobro mediante NFC simulando el flujo de un TPV real.

---

## Estructura del repositorio

```
ESplit/
├── Android/        # Aplicación Android principal
├── AndroidTPV/     # Aplicación TPV simulada
└── Backend/        # API REST + WebSockets (FastAPI + SQLite)
```

---

## Requisitos previos

### Backend
- Sistema operativo Linux (Ubuntu 24.04 o superior)
- Python 3.12 o superior
- Puerto 8000 abierto en la máquina anfitriona
- Conexión de red local entre el servidor y los dispositivos móviles

### Aplicaciones Android
- Android Studio actualizado
- Android SDK API 34 o superior
- Dispositivo físico Android con soporte NFC (requerido para el handshake entre participantes)
- El emulador de Android Studio **no soporta NFC**

---

## Configuración del backend

### 1. Instalar dependencias

Desde la carpeta `Backend/`:

```bash
pip install fastapi uvicorn sqlmodel bcrypt python-jose[cryptography]
```

### 2. Iniciar el servidor

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

La base de datos SQLite se genera automáticamente en el primer arranque. Para partir de un estado limpio, elimina el archivo `app.db` antes de iniciar — el sistema lo regenerará automáticamente.

### 3. Crear usuarios de prueba

Una vez arrancado el servidor, accede a Swagger UI y llama a:

```
POST /users/reset
```

---

## Configuración de red

La IP del servidor está configurada en `ApiClient.java` de ambas aplicaciones Android:

```
192.168.1.194
```

Si el servidor corre en una IP distinta, actualiza ese valor en `ApiClient.java` antes de compilar.

La migración a infraestructura cloud (AWS) está contemplada como trabajo futuro.

| Recurso | URL |
|---|---|
| API | `http://192.168.1.194:8000` |
| Swagger UI | `http://192.168.1.194:8000/docs` |
| WebSocket | `ws://192.168.1.194:8000/ws/payments/{id}` |

---

## Despliegue de las aplicaciones Android

1. Abrir `Android/` o `AndroidTPV/` como proyecto independiente en Android Studio
2. Sincronizar dependencias Gradle
3. Verificar la IP del backend en `ApiClient.java`
4. Compilar e instalar en un dispositivo físico con NFC habilitado
5. Asegurarse de que todos los dispositivos están en la misma red local que el servidor

---

## Ejecución del sistema

1. Iniciar el servidor backend y verificar acceso a `http://192.168.1.194:8000/docs`
2. Crear usuarios de prueba con `POST /users/reset`
3. Iniciar sesión en la app Android principal (ej. `admin@mail.com` / `admin1234`)
4. Crear una sala de pago introduciendo el monto total
5. Incorporar participantes acercando los dispositivos mediante NFC
6. Confirmar los importes — cada participante recibe una notificación para aceptar o rechazar
7. Una vez todos confirmados, iniciar el proceso de pago
8. Acercar el dispositivo del host al TPV simulado (`AndroidTPV`) para procesar la transacción
9. Verificar que los saldos se actualizan y las pantallas de sala se cierran automáticamente

---

## Autenticación

Los endpoints de la API están protegidos mediante JWT. Para probarlos desde Swagger UI:

1. Llamar a `POST /users/login` con las credenciales de un usuario
2. Copiar el token de la respuesta
3. Pulsar el botón **Authorize** en Swagger UI e introducir: `Bearer <token>`

---

## Seguridad implementada

| Medida | Estado |
|---|---|
| Contraseñas hasheadas con bcrypt | ✅ Implementado |
| Autenticación JWT con expiración | ✅ Implementado |
| Sesión única por usuario | ✅ Implementado |
| Logout activo con invalidación de token | ✅ Implementado |
| Verificación de pertenencia a sala | ✅ Implementado |
| Token en conexión WebSocket | ✅ Implementado |

---

## Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Backend | Python + FastAPI |
| Base de datos | SQLite (via SQLModel) |
| Comunicación en tiempo real | WebSockets |
| Autenticación | JWT (python-jose) + bcrypt |
| App Android | Java (Android SDK) |
| Identificación de usuarios | NFC (HCE + ReaderMode) |