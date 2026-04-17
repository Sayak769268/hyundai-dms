# Business Requirements Document (BRD)

## Hyundai Dealer Management System (DMS)

---

| Field | Details |
|---|---|
| Document Version | 1.0 |
| Status | Final |
| Prepared By | DMS Project Team |
| Date | April 2024 |
| Classification | Internal |

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Objectives](#2-objectives)
3. [Stakeholders](#3-stakeholders)
4. [User Roles & Permissions](#4-user-roles--permissions)
5. [Functional Requirements](#5-functional-requirements)
6. [Non-Functional Requirements](#6-non-functional-requirements)
7. [Data Isolation (Multi-Tenant Architecture)](#7-data-isolation-multi-tenant-architecture)
8. [Assumptions](#8-assumptions)
9. [Constraints](#9-constraints)
10. [Future Enhancements](#10-future-enhancements)

---

## 1. Project Overview

The **Hyundai Dealer Management System (DMS)** is a centralised, web-based platform designed to digitise and streamline the end-to-end operations of Hyundai dealerships across India. The system serves as a single source of truth for all dealership activities — from customer acquisition and inventory management to sales processing and performance analytics.

The platform supports three distinct user types: the **OEM Administrator** (Hyundai corporate), **Dealers** (individual dealership owners or managers), and **Employees** (dealership sales and service staff). Each user type operates within a clearly defined scope of access, ensuring data security and operational clarity.

The system is built on a **multi-tenant architecture**, meaning each dealership operates in a completely isolated data environment. No dealer can access another dealer's customers, inventory, or sales data.

---

## 2. Objectives

The primary business objectives of the Hyundai DMS are:

| # | Objective |
|---|---|
| 1 | Provide a unified platform for all dealership operations, eliminating fragmented spreadsheets and manual processes |
| 2 | Enable real-time visibility into sales performance, inventory levels, and customer pipeline across all dealerships |
| 3 | Enforce strict data isolation between dealerships to protect business-sensitive information |
| 4 | Streamline the customer journey from initial enquiry to final invoice |
| 5 | Empower the OEM (Hyundai) with a global view of all dealer performance and rankings |
| 6 | Reduce order processing errors through a structured, stage-gated sales workflow |
| 7 | Maintain a complete audit trail of all system actions for compliance and accountability |
| 8 | Enable dealership staff to manage their daily tasks through a role-appropriate interface |

---

## 3. Stakeholders

| Stakeholder | Role | Interest |
|---|---|---|
| Hyundai OEM (Admin) | System Owner | Global visibility, dealer performance monitoring, compliance |
| Dealership Owner / Manager | Primary Operator | Day-to-day operations, sales tracking, staff management |
| Employee (Dealership Staff) | End User | Customer management, order processing, test drive scheduling |
| IT / System Administrator | Support | System maintenance, user provisioning |

---

## 4. User Roles & Permissions

The system defines three core roles. Access is strictly enforced — users can only perform actions permitted by their role.

---

### 4.1 Admin (OEM Level)

The Admin represents Hyundai at the corporate level and has **global, read-write access** across all dealerships.

| Capability | Access |
|---|---|
| View all dealers and their performance | ✅ Full Access |
| Activate or deactivate any dealership | ✅ Full Access |
| View global sales, revenue, and rankings | ✅ Full Access |
| Impersonate any dealer's dashboard | ✅ Full Access |
| View system-wide audit logs | ✅ Full Access |
| Manage system users | ✅ Full Access |
| Create or modify dealer data | ✅ Full Access |

> The Admin does not belong to any specific dealership. All data is visible across the entire system.

---

### 4.2 Dealer (Dealership Owner / Manager)

The Dealer manages all operations within their own dealership. They have **full operational access** but are strictly limited to their own dealership's data.

| Capability | Access |
|---|---|
| Manage dealership inventory (add, edit, adjust stock) | ✅ Full Access |
| Manage customers and CRM pipeline | ✅ Full Access |
| Create and manage sales orders | ✅ Full Access |
| Manage employees (create, activate, deactivate) | ✅ Full Access |
| View dealership dashboard and KPIs | ✅ Full Access |
| Record payments against orders | ✅ Full Access |
| View data from other dealerships | ❌ No Access |
| Access system-wide audit logs | ❌ No Access |

---

### 4.3 Employee (Sales / Service Staff)

Employees are dealership staff members who interact with customers and process orders on behalf of the dealer. Their access is **scoped to their assigned work** only.

| Capability | Access |
|---|---|
| View and update assigned customers | ✅ Limited Access |
| Add new customers to the CRM | ✅ Full Access |
| View own sales orders | ✅ View Only |
| Schedule and manage test drives | ✅ Full Access |
| View dealership inventory | ✅ View Only |
| Add or edit inventory | ❌ No Access |
| View other employees' customers or orders | ❌ No Access |
| Access admin or dealer management features | ❌ No Access |

---

## 5. Functional Requirements

### 5.1 Dashboard

**Admin Dashboard**

The Admin dashboard provides a global overview of all dealership performance.

- Display total number of active dealers and system users
- Show global sales volume and revenue for the current month
- Rank all dealerships by revenue (Dealership Leaderboard)
- Highlight the top-performing and lowest-performing dealer
- Display grouped inventory shortage alerts (e.g. "Kiran Motors — 4 low stock vehicles")
- Allow the Admin to click any dealer and view their dashboard in impersonation mode

**Dealer Dashboard**

The Dealer dashboard provides a real-time snapshot of their dealership's performance.

- Display key metrics: total active customers, vehicles in stock, sales this month, monthly revenue
- Show month-over-month growth indicators for sales and revenue
- Display a 6-month sales trend chart
- Show recent sales orders with status
- Alert on low-stock vehicles and pending orders
- Highlight the best-selling vehicle model
- Clicking "Inventory Shortage" navigates directly to the filtered low-stock inventory view
- Clicking "Pending Orders" navigates directly to the filtered pending orders view

**Employee Dashboard**

The Employee dashboard shows only the data relevant to the logged-in staff member.

- Display personal KPIs: my customers, my orders, pending orders, invoiced orders
- Show recent personal sales orders
- Provide quick-action shortcuts to common tasks (add customer, view inventory, schedule test drive)

---

### 5.2 Customer Management (CRM)

The CRM module manages the full customer lifecycle from initial enquiry to final purchase.

**Customer Records**

- Each customer record contains: full name, email, phone, address, notes, lead status, assigned employee, and next follow-up date
- Customers are always associated with a specific dealership

**Lead Status Pipeline**

Customers progress through the following statuses:

```
NEW → INTERESTED → BOOKED → LOST
```

- **NEW** — Customer has been registered in the system
- **INTERESTED** — Customer has shown interest and is being followed up
- **BOOKED** — Customer has placed a booking or confirmed purchase intent
- **LOST** — Customer did not proceed with a purchase

**Capabilities**

| Action | Admin | Dealer | Employee |
|---|---|---|---|
| View all customers (own dealership) | ✅ | ✅ | Assigned only |
| Add new customer | ✅ | ✅ | ✅ |
| Edit customer details | ✅ | ✅ | ✅ |
| Archive (soft-delete) customer | ✅ | ✅ | ❌ |
| Filter by status, employee, search | ✅ | ✅ | ✅ |

**Filters Available**

- Search by name, email, or phone
- Filter by lead status
- Filter by assigned employee

---

### 5.3 Inventory Management

The Inventory module tracks all vehicles available at a dealership.

**Vehicle Record**

Each vehicle entry contains: model name, brand, variant, year, base price, current stock quantity, and stock status.

**Stock Status**

Stock status is automatically calculated based on quantity:

| Stock Level | Status |
|---|---|
| 0 units | Out of Stock |
| 1–2 units | Low Stock |
| 3+ units | Available |

**Capabilities**

| Action | Admin | Dealer | Employee |
|---|---|---|---|
| View inventory | ✅ | ✅ | ✅ (View Only) |
| Add new vehicle | ✅ | ✅ | ❌ |
| Edit vehicle details | ✅ | ✅ | ❌ |
| Adjust stock (add/reduce) | ✅ | ✅ | ❌ |
| Delete vehicle | ✅ | ✅ | ❌ |

**Filters Available**

- Search by model or variant
- Filter by stock status (Available / Low Stock / Out of Stock)
- Filter by price range (min/max)
- Filter by year

> When a sales order is confirmed or invoiced, the vehicle's stock is automatically reduced by one unit.

---

### 5.4 Sales & Orders

The Sales module manages the complete lifecycle of a vehicle sale from initial booking to final invoice.

**Order Workflow**

All sales orders follow a strict, linear progression:

```
PENDING → CONFIRMED → INVOICED → CANCELLED (exit at any stage)
```

| Status | Meaning |
|---|---|
| **PENDING** | Order has been created and is awaiting confirmation |
| **CONFIRMED** | Order has been confirmed by the dealership |
| **INVOICED** | Vehicle has been invoiced and payment recorded |
| **CANCELLED** | Order was cancelled; stock is returned to inventory |

**Order Record**

Each order contains: customer, vehicle, assigned employee, base price, discount applied, final amount, status, and creation date.

**Capabilities**

| Action | Admin | Dealer | Employee |
|---|---|---|---|
| View all orders (own dealership) | ✅ | ✅ | Own orders only |
| Create new order | ✅ | ✅ | ✅ |
| Update order status | ✅ | ✅ | ✅ |
| Cancel order | ✅ | ✅ | ✅ |

**Filters Available**

- Search by customer name or vehicle model
- Filter by status
- Filter by date range (from / to)
- Filter by amount range (min / max)

---

### 5.5 Test Drive Management

The Test Drive module allows employees to schedule and track customer test drives.

**Test Drive Record**

Each test drive contains: customer, vehicle, scheduled date, status, and notes.

**Test Drive Status**

```
SCHEDULED → COMPLETED / CANCELLED / NO-SHOW
```

**Capabilities**

| Action | Admin | Dealer | Employee |
|---|---|---|---|
| Schedule a test drive | ✅ | ✅ | ✅ |
| View scheduled test drives | ✅ | ✅ | ✅ |
| Update test drive status | ✅ | ✅ | ✅ |

---

### 5.6 Employee Management

The Employee Management module allows dealers to manage their dealership staff.

**Capabilities**

| Action | Admin | Dealer | Employee |
|---|---|---|---|
| View all employees | ✅ | ✅ (own dealership) | ❌ |
| Add new employee | ✅ | ✅ | ❌ |
| Activate / deactivate employee | ✅ | ✅ | ❌ |
| Delete employee record | ✅ | ✅ | ❌ |
| Search employees | ✅ | ✅ | ❌ |

> When a dealer is deactivated by the Admin, all associated employee accounts are automatically deactivated as well.

---

### 5.7 Security & Authentication

**User Registration**

- New dealerships register through the public registration page
- A dealer account is automatically created with an associated dealership entity
- Employees are registered by their dealer through the Employee Management module

**Login**

- All users authenticate with a username and password
- Successful login returns a time-limited session token
- The system enforces role-based access from the moment of login

**Session Management**

- Sessions expire after 24 hours
- Expired sessions automatically redirect the user to the login page
- Users can manually sign out at any time

**Password Security**

- All passwords are stored in encrypted form
- Passwords are never returned or displayed in any API response

---

### 5.8 Audit Logs

The Audit Log module provides a complete, tamper-evident record of all significant actions performed in the system.

**Logged Events**

| Event Type | Description |
|---|---|
| LOGIN | Successful user login |
| FAILED_LOGIN | Failed login attempt |
| CREATE | New record created (customer, order, vehicle, etc.) |
| UPDATE | Existing record modified |
| UPDATE_STATUS | Order or entity status changed |
| DELETE | Record removed |

**Each log entry records:**

- Timestamp of the action
- Username of the actor
- Dealership the actor belongs to
- Action type
- Entity type affected (e.g. Customer, Sales Order, Vehicle)
- Description of the action

**Access**

- Only the Admin can view audit logs
- Logs can be filtered by dealer, action type, and keyword search
- Logs are paginated and sorted by most recent first

---

## 6. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | All list pages must load within 2 seconds under normal load. Pagination must be server-side to avoid fetching full datasets. |
| **Scalability** | The system must support multiple concurrent dealerships without performance degradation. |
| **Security** | All data transmission must be encrypted. User sessions must be token-based and time-limited. Passwords must be stored using industry-standard hashing. |
| **Availability** | The system should target 99.5% uptime during business hours. |
| **Usability** | The interface must be intuitive and usable without formal training. Role-appropriate navigation must be displayed automatically based on the logged-in user's role. |
| **Data Integrity** | All financial figures (prices, discounts, final amounts) must be stored with decimal precision. Stock levels must be updated atomically when orders are processed. |
| **Auditability** | Every create, update, and delete action must be logged with the actor's identity and timestamp. |
| **Responsiveness** | The interface must be functional on standard desktop and laptop screen sizes. |
| **Pagination** | All list views (customers, inventory, sales, employees, audit logs) must support server-side pagination with configurable page sizes. |

---

## 7. Data Isolation (Multi-Tenant Architecture)

The Hyundai DMS is a **multi-tenant system**. Each dealership operates as an independent tenant with complete data isolation.

**Core Principles**

- Every data record (customer, vehicle, order, employee) is tagged with the dealership it belongs to
- A dealer can only view, create, and modify records that belong to their own dealership
- Employees can only access data within their assigned dealership
- No cross-dealership data access is possible at any level, except for the Admin

**Isolation Boundaries**

| Data Type | Isolated By |
|---|---|
| Customers | Dealership |
| Inventory / Vehicles | Dealership |
| Sales Orders | Dealership |
| Employees | Dealership |
| Payments | Dealership (via order) |
| Audit Logs | Visible only to Admin |

**Admin Impersonation**

The Admin may view any dealer's dashboard in a read-only impersonation mode for monitoring purposes. This does not grant the Admin the ability to create or modify dealer-specific records on behalf of a dealer.

---

## 8. Assumptions

1. Each dealership will have at least one designated dealer account to manage operations.
2. All users have access to a modern web browser on a desktop or laptop device.
3. Dealerships are responsible for registering and managing their own employee accounts.
4. Vehicle stock levels are managed manually by the dealer; the system does not integrate with external inventory or ERP systems.
5. Payment recording is for internal tracking purposes only; the system does not process actual financial transactions.
6. Each customer belongs to exactly one dealership and cannot be shared across dealerships.
7. The Admin account is provisioned directly and is not created through the public registration flow.
8. Internet connectivity is available at all dealership locations.

---

## 9. Constraints

| Constraint | Description |
|---|---|
| **Single Dealership per User** | Each user account (dealer or employee) is associated with exactly one dealership and cannot be reassigned. |
| **Order Workflow** | Sales orders must follow the defined workflow (Pending → Confirmed → Invoiced). Status cannot be skipped or reversed, except for cancellation. |
| **Stock Enforcement** | A sales order cannot be created for a vehicle with zero stock. |
| **Customer Ownership** | A customer record belongs to one dealership and cannot be transferred. |
| **Audit Log Immutability** | Audit log entries cannot be edited or deleted by any user, including the Admin. |
| **Employee Scope** | Employees can only see customers assigned to them and orders they have personally processed. |
| **Dealer Deactivation** | Deactivating a dealer automatically deactivates all associated user accounts, preventing login. |

---

## 10. Future Enhancements

The following features are identified as potential future additions to the system, subject to business prioritisation:

| # | Enhancement | Business Value |
|---|---|---|
| 1 | **Mobile Application** | Enable sales staff to manage customers and orders from mobile devices in the field |
| 2 | **Exchange / Trade-In Management** | Allow dealers to record and value customer trade-in vehicles as part of a sale |
| 3 | **Accessories & Add-Ons** | Enable employees to attach accessories (insurance, extended warranty, accessories) to sales orders |
| 4 | **Automated Follow-Up Reminders** | Send automated email or SMS reminders to employees for scheduled customer follow-ups |
| 5 | **Finance Integration** | Integrate with third-party finance providers to process loan applications directly within the system |
| 6 | **Advanced Analytics & Reporting** | Provide exportable reports on sales trends, employee performance, and inventory turnover |
| 7 | **Customer Portal** | Allow customers to track their order status and service history through a self-service portal |
| 8 | **Multi-Branch Support** | Allow a single dealership to manage multiple physical branches with separate inventory and staff |
| 9 | **Service Management** | Extend the system to cover post-sale vehicle servicing, job cards, and service history |
| 10 | **API Integration with Hyundai OEM Systems** | Sync vehicle model catalogues, pricing, and allocation data directly from Hyundai's central systems |

---

*End of Document*

---

> **Document Control:** This BRD represents the agreed business requirements for the Hyundai DMS as of the version date above. Any changes to scope must be reviewed and approved before implementation.
