# ER Diagram — Multi-Tenant Smart Society SaaS

`societies` is the tenant root. Every other business table carries a
`society_id` foreign key (denormalized directly onto the table, not just
reachable via a join) so every query — and every unique constraint — can be
scoped with a single indexed column.

```mermaid
erDiagram
    SOCIETIES ||--o{ USERS : "has"
    SOCIETIES ||--o{ RESIDENTS : "has"
    SOCIETIES ||--o{ STAFF : "has"
    SOCIETIES ||--o{ NOTICES : "has"
    SOCIETIES ||--o{ COMPLAINTS : "has"
    SOCIETIES ||--o{ MAINTENANCE_BILLS : "has"
    SOCIETIES ||--o{ PAYMENTS : "has"
    SOCIETIES ||--o{ NOTIFICATIONS : "has"

    ROLES ||--o{ USERS : "assigned to"

    USERS ||--o| RESIDENTS : "1:1 profile"
    USERS ||--o| STAFF : "1:1 profile"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ NOTICES : "authors"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"

    RESIDENTS ||--o{ COMPLAINTS : "raises"
    RESIDENTS ||--o{ MAINTENANCE_BILLS : "billed"

    STAFF ||--o{ COMPLAINTS : "assigned"

    COMPLAINTS ||--o{ COMPLAINT_HISTORY : "audit trail"

    MAINTENANCE_BILLS ||--o| PAYMENTS : "settled by"

    SOCIETIES {
        bigint society_id PK
        varchar name
        varchar society_code UK
        varchar address
        varchar city
        varchar state
        varchar pincode
        varchar contact_email
        varchar contact_phone
        enum status "PENDING | ACTIVE | SUSPENDED"
        datetime created_at
        datetime updated_at
    }

    ROLES {
        bigint role_id PK
        varchar role_name UK "ROLE_SUPER_ADMIN | ROLE_ADMIN | ROLE_RESIDENT | ROLE_STAFF"
    }

    USERS {
        bigint user_id PK
        varchar name
        varchar email UK "unique GLOBALLY, not per-society"
        varchar password
        bigint role_id FK
        bigint society_id FK "NULL only for ROLE_SUPER_ADMIN"
        boolean active
        datetime created_at
    }

    RESIDENTS {
        bigint resident_id PK
        varchar address
        varchar mobile
        varchar flat_no "unique per (society_id, flat_no), not global"
        bigint user_id FK
        bigint society_id FK
    }

    STAFF {
        bigint staff_id PK
        varchar department
        varchar mobile
        bigint user_id FK
        bigint society_id FK
    }

    NOTICES {
        bigint notice_id PK
        varchar title
        text content
        bigint created_by FK "users.user_id"
        bigint society_id FK
        datetime created_at
        datetime updated_at
    }

    COMPLAINTS {
        bigint complaint_id PK
        varchar title
        text description
        enum status "PENDING|ASSIGNED|IN_PROGRESS|RESOLVED"
        bigint resident_id FK
        bigint assigned_staff_id FK
        bigint society_id FK "denormalized for direct-by-id scoping"
        datetime created_at
        datetime updated_at
    }

    COMPLAINT_HISTORY {
        bigint history_id PK
        bigint complaint_id FK
        enum old_status
        enum new_status
        varchar remarks
        bigint updated_by FK "users.user_id"
        datetime updated_at
    }

    MAINTENANCE_BILLS {
        bigint bill_id PK
        decimal amount
        date due_date
        int month
        int year
        enum status "PENDING|PAID|OVERDUE"
        bigint resident_id FK
        bigint society_id FK
        datetime created_at
    }

    PAYMENTS {
        bigint payment_id PK
        bigint bill_id FK "unique (1:1)"
        decimal amount
        enum payment_mode "ONLINE|CASH|CHEQUE|UPI|NEFT"
        varchar transaction_id UK
        enum status "SUCCESS|FAILED|PENDING"
        bigint society_id FK
        datetime payment_date
    }

    NOTIFICATIONS {
        bigint notification_id PK
        varchar message
        enum type "COMPLAINT|PAYMENT|NOTICE|GENERAL"
        boolean is_read
        bigint user_id FK
        bigint society_id FK
        datetime created_at
    }

    PASSWORD_RESET_TOKENS {
        bigint token_id PK
        varchar token UK
        bigint user_id FK
        datetime expiry_date
    }
```

## Key multi-tenancy design decisions

| Decision | Why |
|---|---|
| `email` is unique **globally**, not per-society | Login only has an email/password, no society selector. A globally unique email lets `findByEmail` resolve the account (and its society) unambiguously. |
| `flat_no` is unique **per (society_id, flat_no)** | Two different societies legitimately both have a "Flat A-101". |
| `society_id` is denormalized onto every business table, not just derived through a join (e.g. `Complaint.society_id` in addition to `Complaint.resident.society_id`) | Every table with its own `/api/.../{id}` endpoint can be scoped with a single indexed equality filter, and stays correct even if a future query forgets to join through the parent. |
| `ComplaintHistory` and `PasswordResetToken` do **not** carry their own `society_id` | Neither has a direct-by-id REST endpoint; they're only ever reached through an already-scoped parent (`Complaint`, `User`), so the parent's scoping is sufficient. |
| `Society.status` gates login via `CustomUserDetails.isEnabled()`, re-checked from the DB on every request | A suspension takes effect immediately — not after the currently-issued JWTs expire. |
