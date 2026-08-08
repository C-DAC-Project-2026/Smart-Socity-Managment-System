# Project Structure — Multi-Tenant Additions

Architecture is unchanged (Spring Boot monolith + React + MySQL, layered
`controller → service → serviceimpl → repository → entity`). What's new:

```
backend/backend/src/main/java/com/society/
├── entity/
│   └── Society.java                  ← NEW: tenant root
├── repository/
│   └── SocietyRepository.java        ← NEW
├── service/
│   └── SuperAdminService.java        ← NEW
├── serviceimpl/
│   └── SuperAdminServiceImpl.java    ← NEW
├── controller/
│   └── SuperAdminController.java     ← NEW: /api/super-admin/societies/**
├── dto/
│   └── SocietyDTOs.java              ← NEW
├── security/
│   └── SecurityUtils.java            ← NEW: the ONLY safe way for a service
│                                        to learn the caller's societyId
└── config/
    └── DataSeeder.java               ← NEW: idempotent role + bootstrap
                                          SUPER_ADMIN seeding on every boot

docs/
├── ER_DIAGRAM.md                     ← NEW: full ER diagram + design decisions
├── API_FLOW.md                       ← NEW: auth/tenant flow, step by step
├── SCHEMA_MIGRATION.md               ← NEW: DDL + existing-data backfill
└── PROJECT_STRUCTURE.md              ← this file
```

Everything else (existing entities, repositories, services, controllers) was
edited in place, not moved — see the git log for the incremental diffs,
one commit per feature area:

```
1. chore: initial import (baseline)
2. feat(multi-tenancy): Society entity, SUPER_ADMIN role, JWT societyId,
   society FK on all tenant-owned entities
3. feat(multi-tenancy): scope every repository and service to the caller's
   society
4. feat: SUPER_ADMIN society management + idempotent bootstrap seeder
```

Frontend additions:

```
frontend/frontend/src/pages/
├── AddUser.jsx                 ← replaces the old public Register.jsx;
│                                  Society Admin only, adds Residents/Staff
│                                  into their own society
└── SuperAdminSocieties.jsx     ← NEW: register/activate/suspend societies
```

`Register.jsx` and its public `/register` route are removed — registration
is no longer public (see `docs/API_FLOW.md`).
