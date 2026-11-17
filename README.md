# 📋 Attendix — Enterprise Employee Attendance & Leave Management System

> **Developed by:** Wind Hands  
> **Version:** 1.0.0  
> **Platform:** Android (Native Kotlin) + Node.js REST API Backend  
> **Database:** PostgreSQL  

---

## 📖 Table of Contents

1. [Project Overview](#-project-overview)
2. [System Architecture](#-system-architecture)
3. [Project Structure](#-project-structure)
4. [Features & Functionalities](#-features--functionalities)
   - [Authentication System](#1-authentication-system)
   - [Employee Dashboard](#2-employee-dashboard)
   - [Attendance Management](#3-attendance-management)
   - [Leave Management](#4-leave-management)
   - [Admin Dashboard](#5-admin-dashboard)
   - [Employee Management (Admin)](#6-employee-management-admin)
   - [Attendance Administration](#7-attendance-administration-admin)
   - [Leave Approvals (Admin)](#8-leave-approvals-admin)
   - [Network / IP Security](#9-network--ip-security)
   - [Face Recognition Verification](#10-face-recognition-verification)
   - [System Settings](#11-system-settings)
   - [Audit Logging](#12-audit-logging)
   - [Reporting](#13-reporting)
   - [Profile Management](#14-profile-management)
5. [Database Schema](#-database-schema)
6. [API Reference](#-api-reference)
7. [Frontend (Android App)](#-frontend-android-app)
8. [Backend Technologies & Libraries](#-backend-technologies--libraries)
9. [Frontend Technologies & Libraries](#-frontend-technologies--libraries)
10. [Security Architecture](#-security-architecture)
11. [Configuration & Environment](#-configuration--environment)
12. [Setup & Deployment](#-setup--deployment)

---

## 🌐 Project Overview

**Attendix** is a full-stack, enterprise-grade Employee Attendance and Leave Management System consisting of:

- **Android Mobile App** — A native Kotlin app for employees and admins to manage attendance and leaves on the go.
- **Node.js REST API Backend** — A robust Express.js server providing secured API endpoints, backed by a PostgreSQL database.

The system supports two primary user roles:
| Role | Access Level |
|------|-------------|
| `employee` | Check-in/Check-out, view own attendance & leaves, apply for leave |
| `admin` | Full access — manage employees, approve/reject leaves, view reports, configure system settings |

The production backend is deployed on **DigitalOcean** and the Android app connects to it through JWT-authenticated REST APIs.

---

## 🏗 System Architecture

```
┌──────────────────────────────────────────┐
│          Android App (Kotlin)             │
│  ┌────────────┐   ┌───────────────────┐  │
│  │  Employee  │   │   Admin Module    │  │
│  │  Screens   │   │   Screens         │  │
│  └─────┬──────┘   └────────┬──────────┘  │
│        │  MVVM + Hilt DI   │             │
│        └─────────┬─────────┘             │
│              Retrofit2 + OkHttp           │
└──────────────────┬───────────────────────┘
                   │ HTTPS / REST API
┌──────────────────▼───────────────────────┐
│         Node.js (Express) Backend         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │  Auth    │ │Attendance│ │  Leave   │ │
│  │  Routes  │ │  Routes  │ │  Routes  │ │
│  └──────────┘ └──────────┘ └──────────┘ │
│  ┌─────────────────────────────────────┐ │
│  │  Middleware: JWT Auth, IP Restrict, │ │
│  │  Face Verification, Audit Log       │ │
│  └─────────────────────────────────────┘ │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│           PostgreSQL Database             │
│  employees | attendance | leave_requests  │
│  roles | system_settings | audit_logs    │
│  allowed_networks | ip_access_logs        │
└──────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
Attendix/
├── app/                                  # Android Application
│   └── src/main/java/com/windhands/attendix/
│       ├── AttendixApp.kt                # Hilt Application class
│       ├── data/
│       │   ├── local/
│       │   │   └── SessionManager.kt    # DataStore-based session/token storage
│       │   ├── remote/
│       │   │   ├── AuthApiService.kt    # Retrofit API for auth endpoints
│       │   │   ├── AttendanceApiService.kt
│       │   │   ├── LeaveApiService.kt
│       │   │   ├── AdminApiService.kt
│       │   │   └── dto/                 # Data Transfer Objects (Kotlin Serialization)
│       │   │       ├── AuthDto.kt
│       │   │       └── AdminDto.kt
│       │   └── repository/
│       │       ├── AuthRepository.kt
│       │       ├── AttendanceRepository.kt
│       │       ├── LeaveRepository.kt
│       │       └── AdminRepository.kt
│       ├── di/
│       │   └── NetworkModule.kt         # Hilt DI – Retrofit, OkHttp, API services
│       ├── ui/
│       │   ├── MainActivity.kt          # Single-activity host with Navigation Component
│       │   ├── splash/                  # SplashScreen
│       │   ├── auth/
│       │   │   ├── LoginFragment.kt + LoginViewModel.kt
│       │   │   ├── ForgotPasswordFragment.kt + ForgotPasswordViewModel.kt
│       │   ├── employee/
│       │   │   ├── EmployeeContainerFragment.kt   # Bottom-nav host
│       │   │   ├── EmployeeDashboardFragment.kt + EmployeeDashboardViewModel.kt
│       │   │   ├── AttendanceHistoryFragment.kt + AttendanceHistoryViewModel.kt
│       │   │   ├── LeaveManagementFragment.kt + LeaveManagementViewModel.kt
│       │   │   ├── ApplyLeaveFragment.kt + ApplyLeaveViewModel.kt
│       │   │   └── adapter/             # RecyclerView adapters
│       │   ├── admin/
│       │   │   ├── AdminContainerFragment.kt       # Admin bottom-nav host
│       │   │   ├── AdminDashboardFragment.kt + AdminDashboardViewModel.kt
│       │   │   ├── EmployeeManagementFragment.kt + EmployeeManagementViewModel.kt
│       │   │   ├── AddEditEmployeeFragment.kt + AddEditEmployeeViewModel.kt
│       │   │   ├── AttendanceManagementFragment.kt + AttendanceManagementViewModel.kt
│       │   │   ├── LeaveApprovalsFragment.kt + LeaveApprovalsViewModel.kt
│       │   │   └── adapter/
│       │   ├── common/
│       │   │   ├── ProfileFragment.kt + ProfileViewModel.kt
│       │   │   └── ChangePasswordFragment.kt + ChangePasswordViewModel.kt
│       │   ├── base/
│       │   │   ├── BaseFragment.kt      # Common fragment base class
│       │   │   └── BaseViewModel.kt     # Common ViewModel base class
│       │   └── restricted/              # Access-restricted screens
│       └── util/
│           ├── Constants.kt
│           ├── DateUtils.kt
│           ├── ErrorMessage.kt
│           ├── Extensions.kt
│           ├── NetworkUtils.kt
│           └── Resource.kt              # Sealed class for API state
│
├── backend/                             # Node.js API Server
│   ├── src/
│   │   ├── server.js                   # Entry point, Express app setup
│   │   ├── config/
│   │   │   ├── config.js               # Environment configuration
│   │   │   └── database.js             # PostgreSQL connection pool (pg)
│   │   ├── controllers/
│   │   │   ├── authController.js       # Login, logout, password management
│   │   │   ├── attendanceController.js # Check-in, check-out, history, stats
│   │   │   ├── leaveController.js      # Apply, review, cancel leaves
│   │   │   ├── adminController.js      # Employee CRUD, reports, dashboard KPIs
│   │   │   ├── faceController.js       # Face descriptor enroll/verify/remove
│   │   │   ├── networkController.js    # Allowed networks CRUD, IP access logs
│   │   │   └── settingsController.js   # System settings management
│   │   ├── middleware/
│   │   │   ├── auth.js                 # JWT protect + role authorize middleware
│   │   │   ├── dbIpRestriction.js      # DB-driven IP allowlist for attendance
│   │   │   ├── faceVerification.js     # Face descriptor matching middleware
│   │   │   ├── auditLog.js             # Audit trail creation helper
│   │   │   ├── errorHandler.js         # Centralized error handler
│   │   │   └── selectiveIpRestriction.js
│   │   ├── routes/
│   │   │   ├── authRoutes.js
│   │   │   ├── attendanceRoutes.js
│   │   │   ├── leaveRoutes.js
│   │   │   └── adminRoutes.js
│   │   ├── api/
│   │   └── utils/
│   │       ├── jwt.js                  # Token generation & verification helpers
│   │       ├── email.js                # Nodemailer for welcome & reset emails
│   │       ├── helpers.js              # Date utils, business days, employee ID gen
│   │       ├── response.js             # Standardized sendSuccess/sendError helpers
│   │       └── settingsCache.js        # In-memory cache for system settings
│   └── database/
│       ├── schema.sql                  # Full database schema
│       ├── seed.sql                    # Seed data for roles and settings
│       └── migration_ip_security.sql  # Migration for IP restriction tables
```

---

## ✨ Features & Functionalities

### 1. Authentication System

#### Employee Login
- Login using **Employee ID** (format: `EMP001`) and **password**.
- JWT token issued on successful login; stored securely in **DataStore** (Android Jetpack).
- Session persists across app restarts (Remember Me functionality).
- Deactivated accounts are blocked from logging in.

#### Forgot / Reset Password
- Employee enters their registered email address.
- A **password reset link with a time-limited JWT token** (15-minute expiry) is sent via email.
- The reset token is stored in the database alongside an expiry timestamp.
- On successful reset, both the hashed password and the `temporary_password` field are updated.

#### Change Password
- Authenticated users can change their password by providing the current password.
- Minimum password length of 6 characters enforced.
- Audit log created on every password change.

#### Logout
- Client-side token removed via DataStore clear; server logs the logout action.

---

### 2. Employee Dashboard

The home screen for employees showing a real-time overview of their workday:

| Feature | Description |
|---------|-------------|
| **Time-based Greeting** | "Good Morning / Afternoon / Evening, [Name]" |
| **Live Clock & Date** | Current time and formatted date displayed |
| **Check-In / Check-Out Button** | Single button that toggles state based on today's attendance |
| **Confirmation Dialog** | MaterialAlertDialog shown before marking attendance |
| **Check-In / Check-Out Times** | Displays recorded times (e.g., `09:05 AM / 06:30 PM`) |
| **Live Working Hours Timer** | Real-time counter updated every second while checked in |
| **Leave Balance Cards** | Horizontal scrolling list showing Paid, Unpaid, Annual leave balances |
| **Recent Activity Feed** | Shows recent check-in/check-out and leave events |
| **Loading Overlay** | Full-screen loading indicator during API calls |
| **Error/Success Snackbars** | Feedback messages for all actions |

The live working hours timer is implemented in the ViewModel using a Kotlin Coroutine `Job` that updates every 1 second and correctly parses ISO 8601 timestamps with timezone awareness.

---

### 3. Attendance Management

#### Employee Attendance
- **Check In**: Records check-in time, auto-calculates late status vs. configured office start time.
- **Check Out**: Records check-out time, auto-calculates total hours worked and determines status (`present`, `half-day`, `late`).
- **Today's Status**: Returns whether the employee has checked in/out for the day.
- **Current Session**: Returns live session data including `liveDurationSeconds` for the real-time timer.
- **Attendance History**: Paginated list of past attendance records with filters (date range, status).
- **Attendance Statistics**: Aggregated counts — present, absent, late, half-day, leave days, average hours.

#### Status Classification
| Status | Condition |
|--------|-----------|
| `present` | Checked in on time, worked ≥ full day hours (default: 8h) |
| `late` | Checked in after start time + late threshold (default: 30 min) |
| `half-day` | Total hours < half-day threshold (default: 4h) |
| `absent` | No check-in record for the day |
| `on-leave` | Leave approved for this date |

---

### 4. Leave Management

#### Employee Leave Portal
- **Apply for Leave**: Select start date, end date, and enter a reason. The system auto-determines:
  - **Leave Type**: `paid` if sufficient paid leave balance; otherwise `unpaid`
  - **Business Days**: Calculated to exclude weekends from leave balance deduction
  - **Total Calendar Days**: Stored in the DB for display purposes
- **My Leave Requests**: Paginated list of personal leave requests with statuses (`pending`, `approved`, `rejected`) and reviewer notes.
- **Leave Balance**: Displays current paid leave balance, total annual entitlement, unpaid leaves taken, and pending/approved request counts.
- **Cancel Leave**: Employees can cancel only pending leave requests.

#### Leave Balance Logic
- Annual paid leaves default to **7 days per year** (configurable).
- Only **business days** (Mon–Fri) are deducted from the leave balance.
- Weekend-only date ranges are stored as `unpaid` and don't affect paid balance.

---

### 5. Admin Dashboard

The home screen for admins showing organization-wide KPIs:

| Metric Card | Description |
|-------------|-------------|
| **Total Employees** | Count of all active employees |
| **Present Today** | Count of employees with `present` or `late` status today |
| **On Leave Today** | Count of employees with `on-leave` status today |
| **Late Arrivals** | Count of employees marked `late` today |

- **Pending Leave Approvals**: List of pending leave requests with inline Approve/Reject buttons (confirmation dialogs before action).
- **Quick Navigation Cards**: Direct links to Employee Management and Attendance Management sections.

---

### 6. Employee Management (Admin)

Full CRUD for managing the employee roster:

| Action | Description |
|--------|-------------|
| **List Employees** | Paginated table with filters: department, status (active/inactive), search (name, email, employee ID) |
| **View Employee** | Detailed profile including role, joining date, leave balances, manager |
| **Add Employee** | Form with validation — auto-generates sequential Employee ID (`EMP001`, `EMP002`, …) and a temporary password |
| **Edit Employee** | Update designation, department, role, reporting manager, leave balance, and optionally reset password |
| **Activate / Deactivate** | Soft-deactivate (sets `is_active = false`) or hard delete |
| **Welcome Email** | Sent asynchronously to new employees with their credentials |

**Auto-generated Employee ID** logic queries the last `EMP`-prefixed ID and increments it.

---

### 7. Attendance Administration (Admin)

- **View All Attendance**: Admin can view attendance records across all employees with filters (date, status, department).
- **Revoke Check-Out**: Admin can revert a completed check-out, reopening the attendance session so the employee can check out again. Useful for corrections.
- **Attendance Reports**: Aggregate summary per employee showing present/absent/late/half-day counts and average hours worked.

---

### 8. Leave Approvals (Admin)

- **All Leave Requests**: Admin views all pending, approved, and rejected leave requests with employee and department filters.
- **Review Leave Request**: Admin approves or rejects with optional review remarks.
  - On **approval**: 
    - Employee's leave balance is automatically deducted (paid or unpaid based on type and business days)
    - Attendance records are created/updated as `on-leave` for all dates in the approved range
    - This is done atomically using PostgreSQL transactions (with `ROLLBACK` on failure)
  - On **rejection**: No balance changes; just status update with remarks.

---

### 9. Network / IP Security

A comprehensive, database-driven IP restriction system that restricts attendance marking to the office network:

#### Allowed Networks (Admin-managed)
- Admin can add, update, delete, and toggle **IP addresses or CIDR ranges** (e.g., `192.168.1.0/24`).
- Each network entry has a **label** (e.g., "Main Office"), IP/CIDR, and active status.
- No `.env` file changes needed — all managed from the admin dashboard.

#### How It Works
1. On every `check-in` or `check-out` request, the `dbIpRestriction` middleware fires.
2. It reads the client's IP (handling `X-Forwarded-For` proxies, IPv6-mapped IPv4 addresses).
3. The IP is validated against all active rows in `allowed_networks` (exact match or CIDR range).
4. If no match → `403 IP_RESTRICTED` response.
5. Every attempt (allowed and blocked) is logged to `ip_access_logs` for auditing.

#### IP Access Logs
- Admin can view the full log of all check-in/check-out attempts with IP, result (allowed/blocked), reason, user, and timestamp.
- Filterable by allowed status, action, and date range.

> **Note:** The IP check applies **only** to `/attendance/check-in` and `/attendance/check-out`. Leave requests, profile access, and reports remain accessible from any network.

---

### 10. Face Recognition Verification

An optional biometric layer using 128-dimensional face descriptors:

#### Enrollment
- Employee can enroll their face from the Profile page; the 128-float descriptor is sent to the backend and stored in the `face_descriptor` column (JSONB).
- Admin can enroll/re-enroll any employee's face.
- Both employee and admin can remove face enrollment.

#### Verification on Check-In
- If `ENABLE_FACE_VERIFICATION` system setting is `true`, the `faceVerification` middleware runs before the `checkIn` controller.
- The incoming `faceDescriptor` (128 floats) is compared against the stored descriptor using **Euclidean distance**.
- If distance > configurable threshold (`FACE_MATCH_THRESHOLD`, default `0.50`), check-in is rejected with `FACE_MISMATCH`.
- If the employee has no face enrolled, check-in is rejected with `FACE_NOT_ENROLLED`.
- Face verification can be disabled globally from System Settings without code changes.

---

### 11. System Settings

Admin-configurable system-wide parameters stored in the `system_settings` table:

| Setting Key | Default | Description |
|-------------|---------|-------------|
| `CHECK_IN_START_TIME` | `09:00` | Official office start time |
| `CHECK_IN_END_TIME` | `09:30` | Grace period end time |
| `LATE_THRESHOLD_MINUTES` | `30` | Minutes after start to mark as late |
| `HALF_DAY_HOURS` | `4` | Minimum hours for half-day |
| `FULL_DAY_HOURS` | `8` | Minimum hours for full-day |
| `ANNUAL_PAID_LEAVES` | `7` | Annual paid leave entitlement per employee |
| `ENABLE_FACE_VERIFICATION` | `false` | Toggle face recognition for check-in |
| `FACE_MATCH_THRESHOLD` | `50` (= 0.50) | Euclidean distance threshold |

- **Single setting update** or **bulk update** supported.
- **Reset to defaults** resets all settings at once.
- Settings are cached in-memory (with cache invalidation on update) for performance.

---

### 12. Audit Logging

Every significant action is recorded in the `audit_logs` table:

| Logged Actions |
|----------------|
| `LOGIN`, `LOGOUT` |
| `FORGOT_PASSWORD_REQUEST`, `PASSWORD_RESET`, `PASSWORD_CHANGED` |
| `CHECK_IN`, `CHECK_OUT`, `CHECK_OUT_REVOKED` |
| `LEAVE_APPLIED`, `LEAVE_APPROVED`, `LEAVE_REJECTED`, `LEAVE_CANCELLED` |
| `EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED`, `EMPLOYEE_DELETED`, `EMPLOYEE_ACTIVATED`, `EMPLOYEE_DEACTIVATED` |
| `PROFILE_UPDATED` |
| `FACE_ENROLLED`, `FACE_REMOVED`, `FACE_ENROLLED_BY_ADMIN`, `FACE_REMOVED_BY_ADMIN` |
| `ADD_NETWORK`, `UPDATE_NETWORK`, `DELETE_NETWORK`, `TOGGLE_NETWORK` |
| `SETTING_UPDATED`, `SETTINGS_BULK_UPDATED`, `SETTINGS_RESET` |

Each log entry captures: employee ID, action, entity type, entity ID, JSON details payload, IP address, and user agent.

---

### 13. Reporting

Admin-only report generation endpoints:

| Report | Details |
|--------|---------|
| **Attendance Report** | Per-employee summary: total days, present, late, absent, half-day, on-leave counts, and average hours. Filterable by date range, department, and employee ID. |
| **Leave Report** | Per-employee summary: leave balance, unpaid days taken, total/pending/approved/rejected request counts. Filterable by department and status. |

Reports are returned as JSON arrays and can be further processed (e.g., exported to Excel or PDF via `exceljs` / `pdfkit` on the backend).

---

### 14. Profile Management

Both employees and admins can:
- **View Profile**: See personal details — name, employee ID, email, phone, department, designation, joining date, reporting manager, role, and leave balance.
- **Update Profile**: Update phone number and profile photo URL.
- **Change Password**: Verified current password, new password minimum 6 characters.

---

## 🗄 Database Schema

The PostgreSQL database has the following tables:

| Table | Purpose |
|-------|---------|
| `roles` | Defines user roles: `admin`, `hr`, `employee` |
| `employees` | All employee data including profile, role, leave balance, and face descriptor |
| `attendance` | Daily check-in/out records per employee |
| `leave_requests` | Leave applications with status and approval details |
| `system_settings` | Key-value store for system-wide configuration |
| `audit_logs` | Immutable trail of all system actions |
| `allowed_networks` | Admin-managed IP/CIDR allowlist for attendance |
| `ip_access_logs` | Log of every attendance attempt with IP and result |

### Key Database Views
- `employee_attendance_summary` — Aggregated present/absent/late counts per employee
- `employee_leave_summary` — Leave balance and request counts per employee

### Auto-Update Triggers
PostgreSQL triggers automatically update `updated_at` timestamps on the `employees`, `attendance`, `leave_requests`, and `system_settings` tables.

---

## 🔌 API Reference

**Production Base URL:** `https://attendance-app-jpkbq.ondigitalocean.app/api/`

### Authentication Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/auth/login` | Public | Login with employee ID + password |
| `POST` | `/auth/forgot-password` | Public | Request password reset email |
| `POST` | `/auth/reset-password` | Public | Reset password with token |
| `GET` | `/auth/me` | 🔒 | Get current user profile |
| `PUT` | `/auth/profile` | 🔒 | Update profile (phone, photo) |
| `POST` | `/auth/change-password` | 🔒 | Change password |
| `POST` | `/auth/logout` | 🔒 | Logout (audit log) |
| `POST` | `/auth/face/enroll` | 🔒 | Enroll face descriptor |
| `DELETE` | `/auth/face/enroll` | 🔒 | Remove face enrollment |
| `GET` | `/auth/face/status` | 🔒 | Check face enrollment status |

### Attendance Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/attendance/check-in` | 🔒 🌐 👁 | Check in (IP + Face verified) |
| `POST` | `/attendance/check-out` | 🔒 🌐 | Check out (IP verified) |
| `GET` | `/attendance/today` | 🔒 | Today's attendance status |
| `GET` | `/attendance/current-session` | 🔒 | Live session for timer |
| `GET` | `/attendance/history` | 🔒 | Paginated attendance history |
| `GET` | `/attendance/stats` | 🔒 | Personal attendance statistics |
| `GET` | `/attendance/all` | 🔒 👑 | All employees' attendance (admin) |
| `POST` | `/attendance/:id/revoke-check-out` | 🔒 👑 | Revoke check-out (admin) |

### Leave Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/leave/apply` | 🔒 | Apply for leave |
| `GET` | `/leave/my-requests` | 🔒 | My leave requests |
| `GET` | `/leave/balance` | 🔒 | My leave balance |
| `DELETE` | `/leave/:id` | 🔒 | Cancel pending leave |
| `GET` | `/leave/all-requests` | 🔒 👑 | All leave requests (admin) |
| `PUT` | `/leave/:id/review` | 🔒 👑 | Approve or reject leave |

### Admin Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/admin/dashboard` | 🔒 👑 | Dashboard KPIs |
| `GET` | `/admin/employees` | 🔒 👑 | All employees (with filters) |
| `GET` | `/admin/employees/:id` | 🔒 👑 | Employee by ID |
| `POST` | `/admin/employees` | 🔒 👑 | Create employee |
| `PUT` | `/admin/employees/:id` | 🔒 👑 | Update employee |
| `DELETE` | `/admin/employees/:id` | 🔒 👑 | Delete employee |
| `POST` | `/admin/employees/:id/activate` | 🔒 👑 | Activate employee |
| `POST` | `/admin/employees/:id/deactivate` | 🔒 👑 | Deactivate employee |
| `POST` | `/admin/employees/:id/face` | 🔒 👑 | Admin enroll face |
| `DELETE` | `/admin/employees/:id/face` | 🔒 👑 | Admin remove face |
| `GET` | `/admin/reports/attendance` | 🔒 👑 | Attendance report |
| `GET` | `/admin/reports/leave` | 🔒 👑 | Leave report |
| `GET` | `/admin/networks` | 🔒 👑 | List allowed networks |
| `POST` | `/admin/networks` | 🔒 👑 | Add network |
| `PUT` | `/admin/networks/:id` | 🔒 👑 | Update network |
| `DELETE` | `/admin/networks/:id` | 🔒 👑 | Delete network |
| `PATCH` | `/admin/networks/:id/toggle` | 🔒 👑 | Toggle network active status |
| `GET` | `/admin/ip-logs` | 🔒 👑 | IP access logs |
| `GET` | `/admin/settings` | 🔒 👑 | All system settings |
| `PUT` | `/admin/settings/:key` | 🔒 👑 | Update a setting |
| `PUT` | `/admin/settings-bulk/update` | 🔒 👑 | Bulk update settings |
| `POST` | `/admin/settings/reset` | 🔒 👑 | Reset to defaults |

**Legend:** 🔒 = JWT required | 👑 = Admin role | 🌐 = IP restriction applied | 👁 = Face verification (when enabled)

---

## 📱 Frontend (Android App)

### Architecture Pattern
The Android app follows **MVVM (Model-View-ViewModel)** architecture with:
- **Single Activity** (`MainActivity`) hosting all fragments via Android **Navigation Component**.
- **Base classes**: `BaseFragment<VB>` and `BaseViewModel` for shared logic (loading states, error/success events).
- **StateFlow / SharedFlow** for reactive UI updates (replaces LiveData for coroutine-friendly state management).
- **Hilt** for dependency injection across all layers.

### Navigation Graph
- `SplashFragment` → checks login status → routes to `LoginFragment` or `EmployeeContainerFragment` / `AdminContainerFragment`.
- **Employee Flow**: Bottom navigation with Dashboard, Attendance History, Leave Management, Profile.
- **Admin Flow**: Bottom navigation with Dashboard, Employee Management, Attendance, Leave Approvals, Profile.

### UI Screens

| Screen | Role | Description |
|--------|------|-------------|
| Splash | All | Checks session; auto-routes to appropriate screen |
| Login | All | Employee ID + password login form |
| Forgot Password | All | Email-based password reset flow |
| Employee Dashboard | Employee | Live check-in/out, working hours timer, leave balance cards |
| Attendance History | Employee | Paginated list with date/status filters |
| Leave Management | Employee | List of leave requests; link to apply |
| Apply Leave | Employee | Date picker + reason form |
| Admin Dashboard | Admin | KPI metric cards + pending approval list |
| Employee Management | Admin | Searchable employee list + add/edit/deactivate |
| Add/Edit Employee | Admin | Full employee form with role and manager selection |
| Attendance Management | Admin | All-employee attendance table with filters |
| Leave Approvals | Admin | All leave requests with approve/reject inline actions |
| Profile | Both | Personal info display + change password |

---

## 🖥 Backend Technologies & Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **express** | ^4.18.2 | Web framework and HTTP server |
| **pg** | ^8.11.3 | PostgreSQL client (connection pooling) |
| **jsonwebtoken** | ^9.0.2 | JWT token generation and verification |
| **bcrypt** | ^6.0.0 | Password hashing (bcrypt, saltRounds=10) |
| **cors** | ^2.8.5 | Cross-Origin Resource Sharing middleware |
| **helmet** | ^7.1.0 | HTTP security headers middleware |
| **morgan** | ^1.10.0 | HTTP request logging (dev mode) |
| **dotenv** | ^16.3.1 | Environment variable management |
| **express-validator** | ^7.0.1 | Request body validation |
| **nodemailer** | ^8.0.1 | Email delivery (password reset, welcome emails) |
| **exceljs** | ^4.3.0 | Excel report generation |
| **pdfkit** | ^0.13.0 | PDF report generation |
| **nodemon** | ^3.0.2 | Dev server hot-reload (devDependency) |

---

## 📱 Frontend Technologies & Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **Kotlin** | 2.x | Primary programming language |
| **Hilt (Dagger)** | Latest | Dependency injection framework |
| **Retrofit2** | Latest | Type-safe HTTP client for REST APIs |
| **OkHttp3** | Latest | HTTP client layer for Retrofit (with logging interceptor) |
| **Kotlinx Serialization** | Latest | JSON serialization/deserialization |
| **Kotlin Coroutines** | Latest | Async/concurrent programming |
| **AndroidX Lifecycle (ViewModel + LiveData)** | Latest | MVVM lifecycle-aware components |
| **Navigation Component** | Latest | Single-activity fragment navigation with SafeArgs |
| **DataStore Preferences** | Latest | Secure key-value persistent storage (replaces SharedPreferences) |
| **Material Components (MDC)** | Latest | Material Design UI components |
| **ConstraintLayout** | Latest | Flexible UI layouts |
| **Coil** | Latest | Image loading and caching |
| **SwipeRefreshLayout** | Latest | Pull-to-refresh on lists |
| **SplashScreen API** | Latest | Animated app launch screen |
| **Fragment KTX** | Latest | Kotlin extensions for fragments |
| **StateFlow / SharedFlow** | Kotlin stdlib | Reactive state and one-time events |

### Build Configuration
- **compileSdk**: 36 | **minSdk**: 26 (Android 8.0+) | **targetSdk**: 36
- **ViewBinding**: Enabled (no `findViewById`)
- **BuildConfig**: Enabled (for `BASE_URL` injection at compile time)
- **Proguard/R8**: Enabled in release builds (minify + shrink resources)
- **Java/Kotlin**: JVM target 21

---

## 🔒 Security Architecture

| Layer | Mechanism | Details |
|-------|-----------|---------|
| **Authentication** | JWT Bearer tokens | 7-day expiry; validated on every protected request |
| **Authorization** | Role-based middleware | `protect` (JWT verify) + `authorize('admin')` |
| **Password Storage** | bcrypt | saltRounds=10; never stored in plain text |
| **IP Restriction** | DB-driven allowlist | CIDR + exact IP matching; admin-managed, no redeploy needed |
| **Face Verification** | Euclidean distance | 128-dim descriptor comparison; configurable threshold |
| **HTTP Security** | Helmet.js | Sets secure HTTP headers (XSS, CSRF, clickjacking protection) |
| **CORS** | Whitelist-based | Specific origin allowlist; mobile apps pass as no-origin |
| **Input Validation** | express-validator | On all POST/PUT routes with data binding |
| **Request Size Limit** | express.json | Body limit: 2MB (for face descriptor base64 payloads) |
| **Audit Trail** | `audit_logs` table | Every state change logged with actor, IP, and timestamp |
| **Fail-Secure IP Check** | DB error → deny | If DB query fails during IP check, access is denied |
| **Token Reset** | DB-stored tokens | Password reset tokens have 15-minute expiry stored in DB |
| **Proxy Support** | `trust proxy: true` | Correctly reads client IP behind Render/Railway/Heroku proxies |

---

## ⚙ Configuration & Environment

Create a `.env` file in the `backend/` directory (see `.env.example`):

```env
# Server
PORT=5000
NODE_ENV=production

# Database
DATABASE_URL=postgresql://username:password@host:5432/database?sslmode=require

# JWT
JWT_SECRET=your_very_long_random_secret
JWT_EXPIRE=7d
JWT_RESET_EXPIRE=15m

# IP Security (selective — attendance only)
ENABLE_SELECTIVE_IP_RESTRICTION=true

# Email (for password reset & welcome emails)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_SECURE=false
EMAIL_USER=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=Attendix <noreply@attendix.com>

# Frontend URL (for CORS whitelist)
FRONTEND_URL=https://your-frontend-url.com
```

### Android App Configuration
The backend URL is configured in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://your-backend-url/api/\"")
```
And overridden in `NetworkModule.kt` with a hardcoded DigitalOcean URL for the production build.

---

## 🚀 Setup & Deployment

### Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Set up PostgreSQL database
psql -U your_user -d your_database -f database/schema.sql
psql -U your_user -d your_database -f database/seed.sql

# Create an admin account
node create-admin.js

# Start development server
npm run dev

# Start production server
npm start
```

### Android App Setup

1. Open the project in **Android Studio**.
2. Update `BASE_URL` in `app/build.gradle.kts` to point to your backend.
3. Sync Gradle dependencies.
4. Build and run on a device or emulator (minSdk 26 / Android 8.0+).

### Database Migration (IP Security tables)

```bash
psql -U your_user -d your_database -f database/migration_ip_security.sql
```

### Utility Scripts

| Script | Purpose |
|--------|---------|
| `create-admin.js` | Create the initial admin account |
| `clear-database.js` | Wipe all data (dev use) |
| `fix-attendance-status.js` | Fix attendance record statuses |
| `run-face-migration.js` | Add face descriptor columns |
| `run-ip-migration.js` | Add IP security tables |
| `generate-hashes.js` | Hash plaintext passwords |

---

## 📄 Additional Documentation

- [`backend/API_DOCUMENTATION.md`](./backend/API_DOCUMENTATION.md) — Full API docs with request/response examples
- [`backend/DEPLOYMENT.md`](./backend/DEPLOYMENT.md) — Step-by-step deployment guide
- [`backend/SELECTIVE_IP_RESTRICTION_GUIDE.md`](./backend/SELECTIVE_IP_RESTRICTION_GUIDE.md) — Detailed IP security guide

---

*© 2025 Wind Hands. All rights reserved.*
