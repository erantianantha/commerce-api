# Minimal E-Commerce Backend API (Spring Boot + MongoDB + Razorpay)

This project is a **minimal e-commerce backend** that supports:

- Products: create + list + search
- Cart: add items, view cart, clear cart
- Orders: create order from cart, view order, user orders, cancel order
- Payments: **Razorpay order creation** + **webhook callback** updates payment & order status

> Built for the in-class assignment spec.

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Data MongoDB
- Razorpay Java SDK

---

## Run Locally

### 1) Start MongoDB

```bash
mongod
```

Default DB URI used:

```
mongodb://localhost:27017/ecommerce_db
```

### 2) Set Razorpay env variables (recommended)

```bash
export RAZORPAY_KEY_ID="<your_key_id>"
export RAZORPAY_KEY_SECRET="<your_key_secret>"
export RAZORPAY_WEBHOOK_SECRET="<your_webhook_secret>"
```

### 3) Start app

```bash
mvn spring-boot:run
```

App runs on: `http://localhost:8080`

---

## API Endpoints

### Products

- `POST /api/products`
- `GET /api/products`
- `GET /api/products/search?q=laptop`

### Cart

- `POST /api/cart/add`
- `GET /api/cart/{userId}`
- `DELETE /api/cart/{userId}/clear`

### Orders

- `POST /api/orders`
- `GET /api/orders/{orderId}`
- `GET /api/orders/user/{userId}`
- `POST /api/orders/{orderId}/cancel`

### Payments

- `POST /api/payments/create`

### Webhook

- `POST /api/webhooks/payment`

> For Razorpay webhook testing, expose local server using **ngrok** and configure webhook URL:

```
https://<ngrok-domain>/api/webhooks/payment
```

---

## Full Flow (Postman)

1. Create products
2. Add to cart
3. Create order
4. Create payment
5. Complete payment in Razorpay checkout
6. Razorpay sends webhook
7. Fetch order → status should become **PAID**

---

## Notes

- Stock is reduced when an order is created.
- Cancel order restores stock (only if not PAID).
- Webhook signature verification supported via `RAZORPAY_WEBHOOK_SECRET`.

