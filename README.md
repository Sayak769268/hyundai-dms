# Hyundai Dealer Management System (DMS)

A full-stack, multi-tenant web application built for Hyundai dealerships to manage inventory, sales, customers, employees, and analytics — all from a single unified platform.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Keyboard Shortcuts](#keyboard-shortcuts)
- [Table Sorting](#table-sorting)
- [Responsive Design](#responsive-design)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [API Reference](#api-reference)
- [Role-Based Access Control](#role-based-access-control)
- [Database Schema](#database-schema)

---

## Overview

The Hyundai DMS is designed to digitize and streamline dealership operations. It supports three user roles — **Admin (OEM)**, **Dealer**, and **Employee** — each with scoped access to relevant data and actions.

Key capabilities:
- Multi-tenant architecture with complete dealer data isolation
- Real-time dashboard with KPIs, sales trends, and alerts
- Full CRM pipeline from lead to invoice
- Inventory management with stock tracking
- Role-based access control at both API and UI level
- Audit trail for all critical actions

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 3.2.4 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | ORM / data access |
| Hibernate | 6.4 | JPA implementation |
| MySQL | 8.x | Primary database |
| JJWT | 0.11.5 | JWT token generation |
| HikariCP | 5.x | Connection pooling |
| Lombok | Latest | Boilerplate reduction |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18.2 | UI framework |
| TypeScript | 5.2 | Type safety |
| Vite | 5.2 | Build tool & dev server |
| Tailwind CSS | 3.4 | Styling |
| React Router | 6.x | Client-side routing |
| Axios | 1.6 | HTTP client |
| React Hook Form | 7.x | Form management |
| Zod | 3.x | Schema validation |
| Recharts | 3.x | Data visualization |
| Lucide React | 0.368 | Icon system |
| React Select | 5.x | Searchable dropdowns |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend (Vite)                  │
│  Login │ Dashboard │ Inventory │ Sales │ CRM │ Admin     │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API (JWT Auth)
┌──────────────────────▼──────────────────────────────────┐
│              Spring Boot Backend (Port 8081)              │
│  Controllers → Services → Repositories → Entities        │
│  JWT Filter │ Security Config │ Global Exception Handler  │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA / Hibernate
┌──────────────────────▼──────────────────────────────────┐
│                   MySQL Database                          │
│  dms_db │ 21+ tables │ Multi-tenant by dealer_id         │
└─────────────────────────────────────────────────────────┘
```

**Backend layers:**
1. **Controller** — REST endpoints, request validation, role guards
2. **Service** — Business logic, multi-tenant scoping
3. **Repository** — JPA queries, pagination, filtering
4. **Entity** — JPA domain models with relationships
5. **DTO** — API contracts, no entity exposure
6. **Security** — JWT filter, custom UserDetails, BCrypt

---

## Features

### Admin (OEM Level)
- Global dashboard with cross-dealer KPIs
- Dealer leaderboard with revenue rankings
- Activate / deactivate dealers (cascades to users)
- Impersonate any dealer's dashboard via `?dealerId=`
- Global sales view across all dealers
- System-wide audit logs

### Dealer
- Business snapshot dashboard (sales trend, revenue, alerts)
- Full inventory management (add, edit, stock adjustment)
- Customer CRM with lead pipeline (New → Interested → Booked → Lost)
- Sales order lifecycle (Pending → Confirmed → Invoiced → Cancelled)
- Employee management (create, activate/deactivate)
- Payment recording per order
- Dashboard shortcuts (low stock → filtered inventory, pending orders → filtered sales)

### Employee
- Personal dashboard (assigned customers, own orders)
- View and update assigned customers
- Create and manage test drives
- View-only inventory access
- View own sales orders

### Cross-cutting
- Server-side search + multi-column filtering on all list pages
- **Server-side sorting** on all table columns (click header to toggle asc/desc)
- Pagination on all data tables (configurable page size)
- Date range, price range, status filters on sales
- Employee filter on customers, year/price filter on inventory
- Grouped inventory shortage alerts (one card per dealer)
- Full audit trail (login, create, update, status changes)
- Account lock after failed login attempts
- Stale JWT auto-redirect to login
- **Keyboard shortcuts** for power-user navigation
- **Responsive design** — works on mobile, tablet, and desktop
- New entities appear at the top of tables (default sort: newest first)

---

## Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl + K` | Focus the search bar on the current page |
| `Ctrl + N` | Create new entity (employee, customer, sale, etc.) |
| `Alt + D` | Navigate to Dashboard |
| `Alt + S` | Navigate to Sales |
| `Alt + I` | Navigate to Inventory |
| `Alt + C` | Navigate to Customers |
| `Alt + E` | Navigate to Employees |
| `Esc` | Close any open modal |


---

## Table Sorting

All data tables support **server-side sorting**:

1. Click any sortable column header to sort **ascending**
2. Click again to sort **descending**
3. Click once more to **clear** the sort

Sortable columns show arrow indicators:
- ↕ (neutral) — no sort active
- ↑ (blue) — ascending
- ↓ (blue) — descending

Sort parameters are passed to the backend via Spring's `Pageable` interface:
```
GET /api/customers?page=0&size=15&sort=firstName,asc
GET /api/sales?page=0&size=15&sort=finalAmount,desc
GET /api/admin/dealers?page=0&size=10&sort=totalRevenue,desc
```

**Default sort:** All tables default to `createdAt,desc` (newest first), so newly created entities always appear at the top.

---

## Responsive Design

The UI adapts to all screen sizes:

| Breakpoint | Behavior |
|---|---|
| Desktop (≥1024px) | Full sidebar visible, spacious layout (unchanged) |
| Tablet (768–1023px) | Sidebar collapses to hamburger menu, tables scroll horizontally |
| Mobile (<768px) | Hamburger menu, compact padding, stacked filters, horizontal table scroll |

Key responsive features:
- **Sidebar** collapses into a slide-over panel with hamburger button on mobile/tablet
- **Tables** are horizontally scrollable (`overflow-x-auto`) on small screens
- **Filter bars** stack vertically on narrow viewports
- **Page headers** use responsive flex layouts
- **Desktop view is completely unchanged** — no visual differences on large screens

---

## Project Structure

```
hyundai-dms/
├── backend/
│   ├── src/main/java/com/hyundai/dms/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   │   └── impl/
│   │   ├── repository/          # JPA repositories
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── security/            # JWT, UserDetails, SecurityConfig
│   │   ├── exception/           # Global exception handler
│   │   └── config/              # App configuration
│   └── src/main/resources/
│       └── application.yml      # App config, HikariCP, JWT
├── frontend/
│   └── src/
│       ├── pages/               # Route-level components
│       ├── components/          # Shared components (Sidebar, Pagination, SortableHeader, ModalPortal)
│       ├── hooks/               # Custom React hooks (useKeyboardShortcuts)
│       └── lib/
│           └── api.ts           # Axios instance with JWT interceptor
└── database/
    ├── init-schema.sql          # Full DB schema + seed data
    ├── fix_schema.sql           # Schema migration fixes
    ├── fix_sales_orders.sql     # Legacy column nullable fixes
    └── kiran_motors_data.sql    # Sample data for testing
```

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.x

### 1. Database Setup

```sql
-- In MySQL Workbench, run:
source database/init-schema.sql
```

Then run the schema fixes:
```sql
source database/fix_schema.sql
source database/fix_sales_orders.sql
```

Optionally load sample data:
```sql
source database/kiran_motors_data.sql
```

### 2. Backend Setup

Update `backend/src/main/resources/application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dms_db
    username: your_username
    password: your_password
```

Start the backend:
```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8081`

### 3. Fix DB on First Run

After the backend starts, call this once to fix legacy schema constraints:
```
GET http://localhost:8081/api/auth/dev/fix-db
```

### 4. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@1234` |
| Dealer (sample) | `kiran_motors` | `Kiran@2024` |
| Dealer (sample) | `rajesh_motors` | `Rajesh@2024` |

> To reset admin password at any time: `GET http://localhost:8081/api/auth/dev/reset-admin`

---

## API Reference

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/register` | Register new dealer account |
| GET | `/api/auth/check-username` | Check username availability |
| GET | `/api/auth/check-email` | Check email availability |

### Customers
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/customers` | List customers (paginated, filtered) |
| POST | `/api/customers` | Create customer |
| PUT | `/api/customers/{id}` | Update customer |
| PATCH | `/api/customers/{id}/archive` | Archive customer |

### Inventory
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/vehicles` | List vehicles (paginated, filtered) |
| POST | `/api/vehicles` | Add vehicle |
| PUT | `/api/vehicles/{id}` | Update vehicle |
| PATCH | `/api/vehicles/{id}/stock` | Adjust stock |
| DELETE | `/api/vehicles/{id}` | Remove vehicle |
| GET | `/api/vehicles/models` | Distinct model names for dealer |
| GET | `/api/vehicles/variants?modelName=X` | Variants for a model |

### Sales
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/sales` | List orders (paginated, filtered) |
| POST | `/api/sales` | Create order |
| PATCH | `/api/sales/{id}/status` | Update order status |

### Payments
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/payments/order/{id}` | Get payments for order |
| POST | `/api/payments` | Record payment |

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/stats` | Dealer dashboard stats |
| GET | `/api/dashboard/stats?dealerId=X` | Admin impersonation |
| GET | `/api/admin/dashboard` | Global admin dashboard |

### Employees & Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/employees` | List employees |
| GET | `/api/users` | List users (paginated, filtered) |
| PUT | `/api/users/{id}` | Update user |

### Audit
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/audit` | Audit logs (paginated, filtered) |

---

## Role-Based Access Control

```
ROLE_ADMIN
  ├── All endpoints
  ├── Cross-dealer data access
  └── Dealer management

ROLE_DEALER
  ├── Own dealer data only (dealer_id scoped)
  ├── Full CRUD on inventory, customers, sales
  └── Employee management

ROLE_EMPLOYEE
  ├── Own dealer data only
  ├── Assigned customers only
  ├── Own sales orders only
  └── View-only inventory
```

Permissions are enforced at both:
- **API level** — `@PreAuthorize` on controllers + service-layer dealer scoping
- **UI level** — conditional rendering based on `roles` from JWT

---

## Database Schema

**Core tables:** `users`, `roles`, `permissions`, `dealers`, `branches`, `departments`, `employees`, `customers`, `vehicles`, `sales_orders`, `payments`, `audit_logs`

**Key relationships:**
- Users → Roles (many-to-many via `user_roles`)
- Customers → Dealer (many-to-one, dealer-scoped)
- Vehicles → Dealer (dealer-scoped via `dealer_id`)
- SalesOrders → Customer + Vehicle + Employee + Dealer
- Payments → SalesOrder

**Multi-tenancy:** All data tables include `dealer_id` for complete isolation between dealerships.

---

## Environment Variables

| Variable | Location | Description |
|---|---|---|
| `spring.datasource.password` | `application.yml` | MySQL password |
| `jwt.secret` | `application.yml` | JWT signing secret (Base64) |
| `jwt.expiration` | `application.yml` | Token expiry in ms (default 24h) |
| `spring.datasource.hikari.maximum-pool-size` | `application.yml` | DB connection pool size |

---

## License

This project is built for educational and demonstration purposes.
