# 🎬 Movie Ticket Booking System

A microservices-based movie ticket booking system built with Java Spring Boot, Apache Kafka, Razorpay payment gateway, and Gmail SMTP notifications. Designed with real-world patterns including async event-driven communication, idempotency, duplicate booking prevention, and automated cleanup scheduling.

---

## 🏗️ Architecture Overview

```
Client (Postman / Frontend)
        │
        ▼
   API Gateway (8765)          ← Single entry point, circuit breaker
        │
        ├──► Booking Service (8080)
        │         │
        │         ├── MySQL (bookings DB)
        │         ├── Kafka Producer → payment-request topic
        │         └── Kafka Consumer ← payment-response topic
        │
        ├──► Payment Service (9090)
        │         │
        │         ├── MySQL (payments DB)
        │         ├── Razorpay API (payment link creation)
        │         ├── Kafka Consumer ← payment-request topic
        │         └── Kafka Producer → payment-response topic
        │
        └──► Notification Service (8083)
                  │
                  ├── Kafka Consumer ← payment-response topic
                  └── Gmail SMTP (confirmation emails)

   Eureka Registry (8761)      ← Service discovery
   Kafka Cluster               ← 3-broker Confluent cluster + Zookeeper
```

---

## 🔄 Request Flow

```
1.  POST /bookings
      → Booking Service saves booking as PENDING
      → Publishes BookingDto to payment-request Kafka topic
      → Returns bookingId immediately (async)

2.  Payment Service consumes payment-request
      → Calls Razorpay API → creates payment link (20 min expiry)
      → Saves PaymentEntity to DB
      → Publishes full BookingDto to payment-response topic

3.  Booking Service consumes payment-response
      → Stores paymentLink in bookings DB

4.  Client polls GET /bookings/{id}
      → Receives paymentLink once available
      → User clicks link → completes payment on Razorpay

5.  Razorpay fires callback → GET /payments
      → Payment Service verifies signature
      → Updates PaymentEntity status (APPROVED / FAILED)
      → Publishes CONFIRMED / CANCELLED status to payment-response topic

6.  Two consumers react to payment-response independently:
      → Booking Service → updates booking status in DB
      → Notification Service → sends confirmation email to user

7.  Cleanup Scheduler (every 5 min)
      → Auto-cancels PENDING bookings older than 22 minutes
        (payment link already expired)
```

---

## 🧩 Modules

| Module | Port | Description |
|---|---|---|
| `movie.services.registry` | 8761 | Eureka service registry |
| `api-gateway` | 8765 | Spring Cloud Gateway with circuit breaker |
| `booking-service` | 8080 | Booking CRUD, Kafka producer/consumer |
| `payment-service` | 9090 | Razorpay integration, payment lifecycle |
| `notification-service` | 8083 | Gmail SMTP email notifications |
| `commons` | — | Shared DTOs (BookingDto, BookingStatus, ResponseDto) |

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.8+
- Docker Desktop
- MySQL (local or via Docker)
- Razorpay test account → [razorpay.com](https://razorpay.com)
- Gmail account with App Password enabled

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/akhileswarj/movie-ticket-booking-system-kafka.git
cd movie-ticket-booking-system
```

### 2. Start Kafka infrastructure

Navigate to the `infrastructure` folder and run the batch script:

```bash
cd infrastructure
start-kafka.bat
```

Or run manually in order (wait ~10s between steps):

```bash
docker compose -f zookeeper.yml up -d
# wait 10 seconds
docker compose -f kafka_cluster.yml up -d
# wait 20 seconds for brokers to elect leaders
docker compose -f init_kafka.yml up    # creates topics (only needed once)
```

Verify all containers are up:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Expected containers: `zookeeper`, `kafka-broker-1`, `kafka-broker-2`, `kafka-broker-3`, `schema-registry`, `kafka-manager`

### 3. Configure environment variables

**razorpay credentials:**
update the payment-service environment variables:
```
payment-service:
  environment:
    RAZORPAY_KEY_ID: your_razorpay_key_id_here
    RAZORPAY_KEY_SECRET: your_razorpay_key_secret_here
```

**booking-service:**
```
DB_HOST_BOOKING=localhost
DB_USERNAME_BOOKING=<your_db_username>
DB_PASSWORD_BOOKING=<your_db_password>
```

**payment-service:**
```
DB_HOST_PAYMENT=localhost
DB_USERNAME_PAYMENT=<your_db_username>
DB_PASSWORD_PAYMENT=<your_db_password>
```

**notification-service:**
```
MAIL_USERNAME=yourgmail@gmail.com
MAIL_APP_PASSWORD=<16-char-google-app-password>
```

> **Gmail App Password setup:**
> Google Account → Security → Enable 2-Step Verification → App Passwords → Create → copy 16-char password

### 4. Start services in order

```
1. movie.services.registry  (Eureka)
2. api-gateway
3. booking-service
4. payment-service
5. notification-service
```

### 5. Verify services registered

Open Eureka dashboard: `http://localhost:8761`

You should see all 4 services registered: `BOOKING-SERVICE`, `PAYMENT-SERVICE`, `NOTIFICATION-SERVICE`, `API-GATEWAY`

---

## 📮 API Reference

All requests go through the API Gateway at `http://localhost:8765`

---

### Create Booking

```
POST http://localhost:8765/bookings
Content-Type: application/json
```

**Request body:**
```json
{
    "userId": "movieviewer123",
    "userEmail": "user@example.com",
    "movieId": 101,
    "seatsSelected": ["B8", "B9"],
    "showDate": "2026-07-15",
    "showTime": "20:15",
    "bookingAmount": 450
}
```

**Response (202-style, immediate):**
```json
{
    "bookingDto": {
        "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "userId": "movieviewer123",
        "userEmail": "user@example.com",
        "movieId": 101,
        "seatsSelected": ["B8", "B9"],
        "showDate": "2026-07-15",
        "showTime": "20:15",
        "bookingAmount": 450.0,
        "bookingStatus": "PENDING",
        "paymentLink": null
    }
}
```

> `paymentLink` is null initially — poll `GET /bookings/{id}` to get it once Kafka + Razorpay processing completes (~3-5 seconds)

---

### Get Booking by ID

```
GET http://localhost:8765/bookings/{bookingId}
```

**Response (after payment link is generated):**
```json
{
    "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "movieviewer123",
    "userEmail": "user@example.com",
    "movieId": 101,
    "seatsSelected": ["B8", "B9"],
    "showDate": "2026-07-15",
    "showTime": "20:15",
    "bookingAmount": 450.0,
    "bookingStatus": "PENDING",
    "paymentLink": "https://rzp.io/rzp/xxxxxxx"
}
```

---

## 🔁 Idempotency & Duplicate Handling

The system handles repeated requests intelligently based on booking status:

| Scenario | Behaviour |
|---|---|
| Same payload, booking is `PENDING` | Returns existing booking + paymentLink |
| Same payload, booking is `CONFIRMED` | Returns existing confirmed booking (no duplicate) |
| Same payload, booking is `CANCELLED` | Resets existing record to `PENDING`, creates new Razorpay link |
| Different seats, same show | Creates new booking (different seat signature) |

Matching is based on: `userId + movieId + seatsSelected + showDate + showTime`

---

## 📧 Email Notifications

A confirmation email is sent to `userEmail` when booking status becomes `CONFIRMED`.

The email includes:
- Booking ID
- Movie ID
- Selected seats
- Show date and time
- Amount paid

> Only `CONFIRMED` status triggers an email. `CANCELLED` and `PENDING` are silently skipped.

---

## 🧹 Cleanup Scheduler

A background job in `booking-service` runs **every 5 minutes** and auto-cancels stale `PENDING` bookings:

```
Cutoff: bookings created more than 22 minutes ago (Razorpay link expiry: 20 min + 2 min buffer)
```

This handles cases where:
- User never polled for the payment link
- User received the link but never visited it
- Razorpay link expired before payment

---

## 🔧 Kafka Topics

| Topic | Producers | Consumers | Purpose |
|---|---|---|---|
| `payment-request` | booking-service | payment-service | Trigger payment link creation |
| `payment-response` | payment-service | booking-service, notification-service | Broadcast payment result |

**Cluster config:** 3 brokers, replication factor 3, 3 partitions per topic

**Broker ports (local):**
```
kafka-broker-1 → localhost:19092
kafka-broker-2 → localhost:29092
kafka-broker-3 → localhost:39092
```

Kafka Manager UI: `http://localhost:9000`

---

## 👨‍💻 Author

**Akhileswar**  
Built as a hands-on learning project covering microservices, event-driven architecture, payment integration, and async notification systems
