# ShopSphere

A full-stack e-commerce platform built with React and Spring Boot.

ShopSphere provides a complete e-commerce workflow including authentication,
product management, shopping cart, checkout, order management, and a
simulated payment system.

## 🚀 Features

### Customer

- User registration and login
- JWT-based authentication
- Browse products
- Search products
- Filter products by category
- Product images
- Shopping cart
- Quantity management
- Checkout
- Shipping address
- Mock payment flow
- Payment success/failure simulation
- Order history
- Order details

### Admin

- Admin authentication
- Product management
- Category management
- Product inventory management
- Order management

### Payment

ShopSphere includes a simulated payment workflow for demonstration purposes.

Supported demo payment methods:

- UPI
- Credit/Debit Card
- Net Banking

No real money is processed.

## 🛠️ Tech Stack

### Frontend

- React
- React Router
- Axios
- CSS

### Backend

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- REST APIs

### Database

- PostgreSQL
- Redis

### DevOps

- Docker
- Docker Compose
- Maven

## 🏗️ Architecture

```text
React Frontend
       |
       | REST API
       ↓
Spring Boot Backend
       |
       ├── Spring Security + JWT
       |
       ├── PostgreSQL
       |
       └── Redis