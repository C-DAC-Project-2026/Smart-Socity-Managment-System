# API & Auth Flow — Multi-Tenant Smart Society SaaS

## Roles

| Role | Scope | Created by |
|---|---|---|
| `ROLE_SUPER_ADMIN` | Platform-wide, no society | Seeded once on first boot (`app.super-admin.*` in `application.properties`) |
| `ROLE_ADMIN` (Society Admin) | One society | `SUPER_ADMIN`, via `POST /api/super-admin/societies` |
| `ROLE_RESIDENT` | One society | The society's `ROLE_ADMIN`, via `POST /api/auth/register` |
| `ROLE_STAFF` | One society | The society's `ROLE_ADMIN`, via `POST /api/auth/register` |

There is **no public self-registration**. Every account is created by someone
one level up in the hierarchy who is already authenticated, so the new
account's `society_id` always comes from a trusted source (the creator's own
JWT-derived context), never from client input.

## 1. Platform bootstrap (one-time, per deployment)

```
DataSeeder (CommandLineRunner, runs on every boot, idempotent)
  → seeds ROLE_SUPER_ADMIN / ROLE_ADMIN / ROLE_RESIDENT / ROLE_STAFF if missing
  → seeds one SUPER_ADMIN user from app.super-admin.email / app.super-admin.password
    if that email doesn't already exist
```

Change the bootstrap password immediately after first login in any real
deployment.

## 2. Onboarding a new society

```
SUPER_ADMIN logs in
  POST /api/auth/login  →  { token, role: ROLE_SUPER_ADMIN, ... }

SUPER_ADMIN registers a society + its first Admin, atomically
  POST /api/super-admin/societies
  { name, societyCode, address, contactEmail, ...,
    adminName, adminEmail, adminPassword }
  → creates Society (status = PENDING)
  → creates User (role = ROLE_ADMIN, society = the new society)

SUPER_ADMIN activates the society (their Admin can now log in)
  PUT /api/super-admin/societies/{id}/activate
```

Until activated, the Society Admin's login fails with 403 ("Your account or
society is not active") — `CustomUserDetails.isEnabled()` checks
`society.status == ACTIVE`.

## 3. Society Admin populates their society

```
Society Admin logs in
  POST /api/auth/login  →  { token, role: ROLE_ADMIN, ... }
  JWT payload: { sub: email, userId, role: ROLE_ADMIN, societyId: 42 }

Society Admin adds residents/staff — societyId is NEVER sent by the client
  POST /api/auth/register   (requires Authorization: Bearer <admin JWT>)
  { name, email, password, role: ROLE_RESIDENT, flatNo, address, mobile }
  → AuthServiceImpl.register() calls
    securityUtils.getCurrentSocietyId()   // reads societyId off the caller's
                                            // DB-loaded CustomUserDetails —
                                            // NOT off any request field
  → new User + Resident are created with society = admin's own society
```

If an attacker edits the request body to try to inject a `societyId` field,
it is simply ignored — the DTO doesn't even have that field, and the service
never reads one from the request.

## 4. Every authenticated request, every module

```
Client → Authorization: Bearer <jwt>
  JwtAuthenticationFilter
    → extracts token, validates signature/expiry
    → loads the user FRESH from the DB by email (CustomUserDetailsService)
      (this is what makes suspension/deactivation effective immediately,
       instead of waiting for the JWT to expire)
    → puts CustomUserDetails (with userId, role, societyId) into the
      SecurityContext for this request

Controller
  → @PreAuthorize("hasRole('ADMIN')") etc. — coarse-grained role check
  → delegates to Service, passing only the request's own DTO/path params
    (residentId, billId, ...) — never a societyId

Service
  → Long societyId = securityUtils.getCurrentSocietyId();
  → repository.findXAndSociety_SocietyId(id, societyId)
  → if the id belongs to a different society: NOT FOUND (identical to a
    genuinely missing id — no existence leakage across tenants)
```

## 5. Example: why a tampered request can't cross tenants

Resident A (society 1) tries `GET /api/bills/57`, where bill 57 actually
belongs to society 2:

```
MaintenanceBillServiceImpl.getBillById(57)
  billRepository.findByBillIdAndSociety_SocietyId(57, 1)   // caller's societyId = 1
  → Optional.empty()   (the row exists, but not WHERE society_id = 1)
  → throws ResourceNotFoundException("Bill not found: 57")
  → 404, same as if bill 57 never existed
```

Nothing in the request — path variable, query param, or body — can change
which `societyId` is used, because the service never reads one from the
request in the first place.

## Swagger / OpenAPI

All existing Swagger annotations (`@Tag`, `@Operation`, `@SecurityRequirement`)
are preserved. New endpoints (`/api/super-admin/societies/**`) are documented
the same way and appear under a "Super Admin - Societies" tag in
`/swagger-ui.html`.
