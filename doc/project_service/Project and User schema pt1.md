# Authsome Project Service - Database Schema Documentation (Part 1)

## Overview

This document outlines the database schema for the Authsome Project Service, specifically the project and user management components. The schema uses the `authsome_project` namespace and implements a hierarchical user identity model.

---

## Tables

### 1. `projects`

**Schema**: `authsome_project`

Represents projects created by Authsome Users. Each project is an isolated application namespace.

| Column | Type | Nullable | Updatable | Notes |
|--------|------|----------|-----------|-------|
| `id` | UUID | ❌ | ❌ | Primary Key, auto-generated |
| `fk_authsome_user_id` | UUID | ❌ | ❌ | Foreign Key to AuthsomeUserEntity |
| `name` | String | ❌ | ✅ | Project name |
| `description` | String | ✅ | ✅ | Optional project description |
| `created_at` | Long | ❌ | ❌ | Timestamp (milliseconds) |
| `updated_at` | Long | ❌ | ✅ | Timestamp (milliseconds) |

**Indexes**:
- `idx_authsome_user` on `fk_authsome_user_id`

**Relationships**:
- Many-to-One with `AuthsomeUserEntity` (lazy loaded)

---

### 2. `users` (Project Users)

**Schema**: `authsome_project`

Represents end-users of a project's application. These are managed by the project owner through the Authsome API.

| Column | Type | Nullable | Updatable | Notes |
|--------|------|----------|-----------|-------|
| `id` | UUID | ❌ | ❌ | Primary Key, auto-generated |
| `fk_project_id` | UUID | ❌ | ✅ | Foreign Key to ProjectEntity (One-to-One) |
| `hashed_password` | String | ❌ | ✅ | Bcrypt or similar hashed password |
| `created_at` | Long | ❌ | ❌ | Timestamp (milliseconds) |
| `updated_at` | Long | ❌ | ✅ | Timestamp (milliseconds) |

**Indexes**:
- `idx_project` on `fk_project_id`

**Relationships**:
- One-to-One with `ProjectEntity` (lazy loaded)
- One-to-Many with `ProjectUserIdentityEntity` (inverse)

---

### 3. `user_identities`

**Schema**: `authsome_project`

Links project users to their identities (email, username, phone, etc.). Supports multiple identity types per user.

| Column | Type | Nullable | Updatable | Notes |
|--------|------|----------|-----------|-------|
| `id` | UUID | ❌ | ❌ | Primary Key, auto-generated |
| `fk_project_user_id` | UUID | ❌ | ❌ | Foreign Key to ProjectUserEntity (Many-to-One) |
| `fk_project_identity_id` | UUID | ❌ | ❌ | Foreign Key to GlobalProjectIdentityEntity (Many-to-One) |
| `is_verified` | Boolean | ❌ | ✅ | Whether identity is verified |
| `is_primary` | Boolean | ❌ | ✅ | Whether this is the primary identity |
| `created_at` | Long | ❌ | ❌ | Timestamp (milliseconds) |
| `updated_at` | Long | ❌ | ✅ | Timestamp (milliseconds) |

**Relationships**:
- Many-to-One with `ProjectUserEntity` (lazy loaded)
- Many-to-One with `GlobalProjectIdentityEntity` (lazy loaded)

---

### 4. `global_identities`

**Schema**: `authsome_project`

Global registry of identity values (emails, usernames, phone numbers) across all projects. Prevents duplicate identities.

| Column | Type | Nullable | Updatable | Notes |
|--------|------|----------|-----------|-------|
| `id` | UUID | ❌ | ❌ | Primary Key, auto-generated |
| `identity_type` | Enum | ❌ | ✅ | Type of identity (EMAIL, USERNAME, PHONE, etc.) |
| `identity` | String | ❌ | ✅ | The actual identity value (e.g., "user@example.com") |
| `created_at` | Long | ❌ | ❌ | Timestamp (milliseconds) |
| `updated_at` | Long | ❌ | ✅ | Timestamp (milliseconds) |

**Indexes**:
- `idx_identity_type_identity` on `(identity_type, identity)` - **UNIQUE**

**Relationships**:
- One-to-Many with `ProjectUserIdentityEntity` (inverse)

---

## Relationships Diagram

```
AuthsomeUserEntity (1) ──── (Many) ProjectEntity
                                     │
                                     │ (1-to-1)
                                     │
                            ProjectUserEntity
                                     │
                                     │ (1-to-Many)
                                     │
                           ProjectUserIdentityEntity
                                     │
                                     │ (Many-to-1)
                                     │
                           GlobalProjectIdentityEntity
```

---

## Key Design Notes

1. **Identity Separation**: User identities are stored in a separate table to support multiple identity types per user (email + phone, etc.)

2. **Global Identity Registry**: The `global_identities` table maintains a global registry to prevent duplicate identities across projects.

3. **Lazy Loading**: All relationships use lazy loading (`FetchType.LAZY`) to minimize database overhead.

4. **Immutable Timestamps**: `created_at` columns are immutable (`updatable = false`), while `updated_at` can be updated.

5. **Unique Constraint**: The combination of `identity_type` and `identity` is globally unique, preventing duplicate identities.

---

*This is Part 1 of the schema documentation. Additional tables and services will be documented in subsequent sections.*