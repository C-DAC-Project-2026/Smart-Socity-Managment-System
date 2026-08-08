# Database Schema Changes — Single-Tenant → Multi-Tenant

`spring.jpa.hibernate.ddl-auto=update` will apply these automatically against
`smart_society_db` on next boot, but review the generated DDL against a
staging copy first if you have existing production data — see "Migrating
existing data" below.

## New table

```sql
CREATE TABLE societies (
    society_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    society_code   VARCHAR(30)  NOT NULL UNIQUE,
    address        VARCHAR(255) NOT NULL,
    city           VARCHAR(100),
    state          VARCHAR(100),
    pincode        VARCHAR(20),
    contact_email  VARCHAR(150) NOT NULL,
    contact_phone  VARCHAR(15),
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at     DATETIME NOT NULL,
    updated_at     DATETIME
);
```

## Altered tables

Every one of these gets a new `society_id BIGINT NOT NULL` column with an FK
to `societies(society_id)` (nullable only on `users`, for `SUPER_ADMIN`):

- `users` — also gets `active BOOLEAN NOT NULL DEFAULT TRUE`
- `residents` — the old `UNIQUE (flat_no)` constraint is replaced with
  `UNIQUE (society_id, flat_no)`
- `staff`
- `notices`
- `complaints`
- `maintenance_bills`
- `payments`
- `notifications`

`roles` gets one new row: `ROLE_SUPER_ADMIN` (seeded automatically by
`DataSeeder` on boot — no manual SQL needed).

## Migrating existing single-tenant data

If you have existing rows from the single-tenant version, they predate the
concept of a society, so `society_id` can't be backfilled automatically.
Recommended path for a first deployment with real existing data:

```sql
-- 1. Create one Society row representing your existing, single society
INSERT INTO societies (name, society_code, address, contact_email, status, created_at)
VALUES ('Your Existing Society', 'DEFAULT01', 'Your Address', 'admin@yoursociety.example', 'ACTIVE', NOW());

-- 2. Backfill every existing row to point at it (run once, in this order —
--    parents before children — inside a transaction)
SET @sid = (SELECT society_id FROM societies WHERE society_code = 'DEFAULT01');
UPDATE users              SET society_id = @sid WHERE role_id <> (SELECT role_id FROM roles WHERE role_name = 'ROLE_SUPER_ADMIN');
UPDATE residents           SET society_id = @sid;
UPDATE staff                SET society_id = @sid;
UPDATE notices               SET society_id = @sid;
UPDATE complaints             SET society_id = @sid;
UPDATE maintenance_bills       SET society_id = @sid;
UPDATE payments                  SET society_id = @sid;
UPDATE notifications               SET society_id = @sid;

-- 3. Only then let Hibernate add the NOT NULL constraints (ddl-auto=update
--    won't tighten an existing nullable column to NOT NULL on its own in
--    all MySQL configurations — verify the generated DDL, and if needed
--    apply the NOT NULL + FK constraints manually after the backfill).
```

Take a full backup before running this on a real database.
