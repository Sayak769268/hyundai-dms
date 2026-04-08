<<<<<<< HEAD
# crud-application



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.haei.net/GDC-Interns-2026/crud-application.git
git branch -M master
git push -uf origin master
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitlab.haei.net/GDC-Interns-2026/crud-application/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
=======
# Hyundai Dealer Management System (DMS)

A full-stack, enterprise-grade web application built to streamline operations across Hyundai dealerships. The system provides a unified platform for managing inventory, sales, customer relationships, employees, and analytics — with a multi-tenant architecture that isolates each dealership's data.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Role-Based Access Control](#role-based-access-control)
- [Screenshots](#screenshots)

---

## Overview

The Hyundai DMS is designed for OEM-level administration and dealership-level operations. It supports three user roles — **Admin**, **Dealer**, and **Employee** — each with scoped access to relevant modules. The admin has a global view across all dealerships, while dealers and employees operate within their own isolated data silo.

Key capabilities:
- Real-time business dashboard with KPIs, sales trends, and alerts
- Full CRM pipeline from lead to invoice
- Inventory management with stock tracking and low-stock alerts
- Sales order lifecycle management (Pending → Confirmed → Invoiced)
- Audit trail for all system actions
- Advanced filtering and server-side pagination across all modules

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 3.2.4 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | ORM and data access |
| Hibernate | 6.4 | JPA implementation |
| MySQL | 8.x | Relational database |
| JJWT | 0.11.5 | JWT token generation & validation |
| HikariCP | 5.x | Connection pooling |
| Lombok | Latest | Boilerplate reduction |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18.2 | UI framework |
| TypeScript | 5.2 | Type safety |
| Vite | 5.2 | Build tool & dev server |
| Tailwind CSS | 3.4 | Utility-first styling |
| React Router | 6.x | Client-side routing |
| Axios | 1.6 | HTTP client |
| React Hook Form | 7.x | Form management |
| Zod | 3.x | Schema validation |
| Recharts | 3.x | Data visualization |
| Lucide React | 0.368 | Icon system |
| React Select | 5.x | Enhanced dropdowns |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     React Frontend                       │
│         (Vite + TypeScript + Tailwind CSS)               │
│                  localhost:5173                          │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP/REST (Axios + JWT)
┌─────────────────────▼───────────────────────────────────┐
│                  Spring Boot Backend                     │
│                   localhost:8081                         │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │Controller│→ │ Service  │→ │Repository│→ │  JPA   │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────┘  │
│                                                          │
│  Spring Security → JWT Filter → Role-Based Access        │
└─────────────────────┬───────────────────────────────────┘
                      │ JDBC (HikariCP)
┌─────────────────────▼───────────────────────────────────┐
│                   MySQL Database                         │
│                    dms_db                                │
│              21+ tables, multi-tenant                    │
└─────────────────────────────────────────────────────────┘
```

The backend follows a clean layered architecture:
- **Controller** — REST endpoints, request/response handling
- **Service** — Business logic, transaction management
- **Repository** — Data access via Spring Data JPA
- **Entity** — JPA domain models
- **DTO** — Data transfer objects (entities never exposed directly)
- **Security** — JWT filter, custom UserDetailsService, method-level `@PreAuthorize`

---

## Features

### Admin Dashboard
- Global KPIs: total dealers, users, monthly sales, revenue
- Dealership leaderboard ranked by revenue
- Top and worst performing dealer cards
- Grouped inventory shortage alerts (per dealer)
- Impersonate any dealer's dashboard via "View Data"

### Dealer Dashboard
- Real-time business snapshot: customers, stock, sales, revenue
- 6-month sales trend bar chart
- Recent orders table
- Critical alerts: low stock, pending orders
- Top-selling model showcase
- Today's orders and revenue

### Customer CRM
- Full customer lifecycle: New → Interested → Booked → Lost
- Assign customers to employees
- Schedule follow-up dates
- Search by name, email, phone
- Filter by status and assigned employee
- Archive inactive customers

### Inventory Management
- Vehicle catalog with model, variant, year, price, stock
- Dynamic model/variant dropdowns (populated from dealer's own inventory)
- Stock status: Available / Low Stock / Out of Stock
- Stock adjustment (add/reduce)
- Filters: search, status, price range, year
- Low stock alerts on dashboard

### Sales Orders
- Create orders linked to customer + vehicle
- Automatic stock reduction on order creation
- Status workflow: Pending → Confirmed → Invoiced → Cancelled
- Filters: search, status, date range, amount range
- Dashboard shortcuts: click "Pending Orders" → filtered sales view

### Employee Management
- Register employees under a dealership
- Search by name, email, designation
- Toggle active/inactive status
- Auto-creates Employee record on ROLE_EMPLOYEE registration

### Dealer Management (Admin only)
- View all registered dealerships
- Activate / Deactivate dealers
- Deactivating a dealer disables all their users automatically
- Search by name or location

### Audit Logs (Admin only)
- Complete action trail: LOGIN, CREATE, UPDATE, DELETE, FAILED_LOGIN
- Filter by dealer, action type, keyword
- Paginated (20 per page)

### Authentication & Security
- JWT-based stateless authentication (24h expiry)
- BCrypt password hashing
- Role-based method-level security (`@PreAuthorize`)
- Auto-redirect to login on session expiry
- Multi-tenant data isolation by `dealer_id`

---

## Project Structure

```
hyundai-dms/
├── backend/
│   ├── src/main/java/com/hyundai/dms/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   │   └── impl/
│   │   ├── repository/          # Spring Data JPA repos
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── security/            # JWT, filters, config
│   │   ├── exception/           # Global error handling
│   │   └── config/              # App configuration
│   └── src/main/resources/
│       └── application.yml      # App config
├── frontend/
│   └── src/
│       ├── pages/               # Route-level components
│       ├── components/          # Shared components
│       └── lib/
│           └── api.ts           # Axios instance + interceptors
├── database/
│   ├── init-schema.sql          # Full DB schema + seed data
│   ├── sample_data.sql          # Additional sample data
│   ├── kiran_motors_data.sql    # Kiran Motors dealer data
│   └── fix_schema.sql           # Schema migration fixes
└── README.md
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
-- In MySQL Workbench or CLI
SOURCE database/init-schema.sql;
```

### 2. Backend Setup

```bash
cd backend

# Configure database credentials in src/main/resources/application.yml
# Update: spring.datasource.username and spring.datasource.password

mvn spring-boot:run
```

Backend starts on `http://localhost:8081`

**First-time setup** — after the backend starts, call this endpoint to initialize the admin account:
```
GET http://localhost:8081/api/auth/dev/reset-admin
```

**Fix legacy DB constraints** (required once):
```
GET http://localhost:8081/api/auth/dev/fix-db
```

### 3. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:5173`

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@1234` |
| Dealer (Kiran Motors) | `kiran_motors` | `Kiran@2024` |

> New dealers can self-register via the Register page. A dealership is automatically created for each new dealer account.

---

## API Reference

All endpoints are prefixed with `/api`.

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/login` | Login and receive JWT | Public |
| POST | `/auth/register` | Register new dealer account | Public |
| GET | `/auth/check-username` | Check username availability | Public |
| GET | `/auth/check-email` | Check email availability | Public |
| GET | `/auth/dev/reset-admin` | Reset admin credentials | Public |
| GET | `/auth/dev/fix-db` | Fix legacy DB constraints | Public |

### Customers
| Method | Endpoint | Description |
|---|---|---|
| GET | `/customers` | List customers (search, status, employee filter, paginated) |
| GET | `/customers/{id}` | Get customer details |
| POST | `/customers` | Create customer |
| PUT | `/customers/{id}` | Update customer |
| PATCH | `/customers/{id}/archive` | Archive customer |

### Inventory (Vehicles)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/vehicles` | List vehicles (search, status, price, year filter) |
| GET | `/vehicles/{id}` | Get vehicle |
| POST | `/vehicles` | Add vehicle |
| PUT | `/vehicles/{id}` | Update vehicle |
| PATCH | `/vehicles/{id}/stock` | Adjust stock |
| DELETE | `/vehicles/{id}` | Remove vehicle |
| GET | `/vehicles/models` | Distinct model names for current dealer |
| GET | `/vehicles/variants?modelName=X` | Variants for a given model |

### Sales Orders
| Method | Endpoint | Description |
|---|---|---|
| GET | `/sales` | List orders (search, status, date range, amount range) |
| GET | `/sales/{id}` | Get order details |
| POST | `/sales` | Create order |
| PATCH | `/sales/{id}/status` | Update order status |

### Employees
| Method | Endpoint | Description |
|---|---|---|
| GET | `/employees` | List employees (search, designation filter) |
| GET | `/employees/{id}` | Get employee |

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| GET | `/dashboard/stats` | Dealer dashboard stats |
| GET | `/dashboard/stats?dealerId=X` | Admin impersonation view |

### Admin
| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin/dashboard` | Global admin dashboard |
| POST | `/admin/dealer/{id}/toggle` | Toggle dealer active status |

### Audit Logs
| Method | Endpoint | Description |
|---|---|---|
| GET | `/audit` | List audit logs (dealer, action, keyword filter) |

---

## Database Schema

The database consists of 21+ tables organized into modules:

| Module | Tables |
|---|---|
| Auth & Users | `users`, `roles`, `permissions`, `user_roles`, `role_permissions` |
| Dealers | `dealers`, `branches`, `departments` |
| Employees | `employees` |
| CRM | `customers`, `customer_leads`, `follow_ups` |
| Inventory | `vehicles`, `vehicle_variants`, `vehicle_models`, `inventory` |
| Sales | `sales_orders` |
| Payments | `payments` |
| Test Drives | `test_drives` |
| Exchanges | `exchanges` |
| Accessories | `accessories` |
| Audit | `audit_logs` |
| Navigation | `menus`, `role_menus` |

---

## Role-Based Access Control

| Feature | Admin | Dealer | Employee |
|---|---|---|---|
| Global Dashboard | ✅ | ❌ | ❌ |
| Dealer Dashboard | ✅ (impersonate) | ✅ | ✅ |
| Manage Dealers | ✅ | ❌ | ❌ |
| Audit Logs | ✅ | ❌ | ❌ |
| Manage Employees | ❌ | ✅ | ❌ |
| Inventory | ✅ (view) | ✅ (full) | ✅ (view) |
| Customers CRM | ✅ | ✅ | ✅ (own) |
| Sales Orders | ✅ (all) | ✅ (own) | ✅ (own) |

---

## Screenshots

> Login page, Dashboard, Inventory, Sales, CRM, Admin — all accessible after setup.

---

## Notes

- The `application.yml` contains hardcoded DB credentials — move to environment variables before any production deployment
- The `/dev/*` endpoints are for development only — remove or secure them before going to production
- JWT expiry is set to 24 hours (`86400000ms`) — users must re-login after backend restarts
>>>>>>> main
