# Multi-Tenant Role-Based Access Control (RBAC) System Design

## Overview

This document describes a sophisticated multi-tenant RBAC system designed to support multiple independent projects (tenants) within a single database while maximizing data deduplication and maintaining flexibility in permission management.

## Design Goals

1. **Multi-Tenancy**: Support multiple independent projects in a single database
2. **Data Deduplication**: Maximize reuse of common entities (Resources, Actions, Roles) across tenants
3. **Flexible Role Definitions**: Allow the same role name to have different permissions in different projects
4. **Efficient Permission Checking**: Enable fast permission validation queries
5. **Scalability**: Handle hundreds or thousands of projects efficiently

## Core Architecture

### Tenant Isolation

Each project operates as an independent tenant with its own:
- User base (usernames are scoped per project)
- Role configurations (same role name can have different permissions)
- Permission assignments

**Example Scenario:**
- **Project A (Social Media)**: "EDITOR" role → [read posts, edit posts]
- **Project B (Blog Platform)**: "EDITOR" role → [read posts, edit posts, delete posts]
- Both reuse the global "EDITOR" role entry, but with different permission sets

---

## Schema Components

### 1. Projects & Users Layer

#### Projects Table
Represents independent tenants/applications using the auth system.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique project identifier |
| name | varchar | Human-readable project name |
| created_at | long | Unix timestamp (ms) |
| updated_at | long | Unix timestamp (ms) |

#### ProjectUsers Table
Users scoped to specific projects. A user can exist in multiple projects independently.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique user identifier |
| username | varchar | Username within project |
| fk_project_id | uuid | Reference to Projects |
| created_at | long | Unix timestamp (ms) |

**Key Constraint:** `UNIQUE(username, fk_project_id)` - Ensures username uniqueness within each project

**Example:**
```
alice@ProjectA (user_id: uuid-1) → Different from → alice@ProjectB (user_id: uuid-2)
```

---

### 2. Global Building Blocks (Shared Entities)

These tables store globally shared entities to maximize deduplication across all projects.

#### Resources Table
Defines entities that can be acted upon (e.g., "posts", "comments", "users").

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| name | varchar | Resource name (globally unique) |
| created_at | long | Unix timestamp (ms) |
| updated_at | long | Unix timestamp (ms) |

**Deduplication Benefit:** If 1,000 projects all need a "posts" resource, only 1 row is stored.

#### Actions Table
Defines operations that can be performed (e.g., "read", "write", "delete").

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| name | varchar | Action name (globally unique) |
| created_at | long | Unix timestamp (ms) |
| updated_at | long | Unix timestamp (ms) |

#### Roles Table
Defines role names that can be reused across projects (e.g., "ADMIN", "EDITOR", "USER").

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| name | varchar | Role name (globally unique) |
| created_at | long | Unix timestamp (ms) |
| updated_at | long | Unix timestamp (ms) |

**Important:** The same role name will have different permissions in different projects. This table only stores the role name, not its permissions.

---

### 3. Project Role Configuration

#### ProjectRoles Table
Maps global roles to specific projects, indicating which roles are available per project.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| fk_project_id | uuid | Reference to Projects |
| fk_role_id | uuid | Reference to Roles |
| created_at | long | Unix timestamp (ms) |

**Key Constraint:** `UNIQUE(fk_project_id, fk_role_id)` - Prevents duplicate role assignments

**Example Configuration:**

| Project | Enabled Roles |
|---------|--------------|
| Project A | ADMIN, EDITOR, USER |
| Project B | ADMIN, MODERATOR, USER |
| Project C | ADMIN, USER |

---

### 4. Permission Management System

This is the most sophisticated part of the schema, featuring aggressive deduplication through content hashing.

#### ActionResourceMap Table (GLOBAL)
Combines actions with resources to create permission primitives.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| fk_action_id | uuid | Reference to Actions |
| fk_resource_id | uuid | Reference to Resources |

**Key Constraint:** `UNIQUE(fk_action_id, fk_resource_id)` - Each action-resource pair exists only once

**Example Mappings:**

| ID | Action | Resource | Represents |
|----|--------|----------|------------|
| uuid-1 | read | posts | "read posts" permission |
| uuid-2 | write | posts | "write posts" permission |
| uuid-3 | delete | comments | "delete comments" permission |
| uuid-4 | read | users | "read users" permission |

---

#### PermissionSets Table (Content Hash Deduplication)

This is where the magic happens for storage optimization.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| content_hash | varchar | SHA-256 hash of sorted permission IDs (unique) |
| created_at | long | Unix timestamp (ms) |

#### The Content Hash System

**What is it?**

The `content_hash` is a SHA-256 cryptographic hash computed from the sorted list of `ActionResourceMap` IDs that make up a permission set. It serves as a fingerprint that uniquely identifies the exact combination of permissions.

**How is it computed?**

1. Collect all `ActionResourceMap` IDs for a permission set
2. Sort them alphanumerically (ensures consistency)
3. Concatenate them with a delimiter (e.g., comma)
4. Apply SHA-256 hashing

```
Example:
Permission Set = [read->posts, write->posts, delete->comments]
ActionResourceMap IDs = [uuid-1, uuid-2, uuid-3]
Sorted IDs = "uuid-1,uuid-2,uuid-3"
content_hash = SHA256("uuid-1,uuid-2,uuid-3") = "abc123def456..."
```

**Why is it useful?**

1. **Automatic Deduplication Detection**: Before creating a new permission set, check if a set with the same hash already exists
2. **Storage Efficiency**: Identical permission sets across projects share a single row
3. **Performance**: Hash comparison is much faster than comparing entire permission lists
4. **Immutability**: Permission sets become immutable; any change creates a new set with a new hash

**How is it used in practice?**

**Scenario: Creating a new USER role for Project C**

1. Define desired permissions: [read->posts, write->comments]
2. Get ActionResourceMap IDs: [uuid-1, uuid-5]
3. Sort and hash: `SHA256("uuid-1,uuid-5")` = "xyz789abc..."
4. Check if hash exists in PermissionSets table:
    - **If YES**: Reuse existing PermissionSet ID
    - **If NO**: Create new PermissionSet with this hash
5. Link ProjectRole to PermissionSet

**Real-World Impact Example:**

Imagine 500 projects all define a basic "USER" role with identical permissions:

**Without Hash Deduplication:**
- 500 rows in PermissionSets table
- Each with 3 associated PermissionSetItems
- Total: 500 + (500 × 3) = 2,000 rows

**With Hash Deduplication:**
- 1 row in PermissionSets table (same hash detected)
- 3 associated PermissionSetItems (shared)
- Total: 1 + 3 = 4 rows
- **Storage reduction: 99.8%**

---

#### PermissionSetItems Table

Links permission sets to their constituent action-resource mappings.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| fk_permission_set_id | uuid | Reference to PermissionSets |
| fk_action_resource_map_id | uuid | Reference to ActionResourceMap |

**Key Constraint:** `UNIQUE(fk_permission_set_id, fk_action_resource_map_id)` - Prevents duplicate permissions

**Example: PermissionSet "set-123" contains [read->posts, write->posts, delete->comments]**

| ID | fk_permission_set_id | fk_action_resource_map_id | Represents |
|----|---------------------|---------------------------|------------|
| item-1 | set-123 | uuid-1 | read->posts |
| item-2 | set-123 | uuid-2 | write->posts |
| item-3 | set-123 | uuid-3 | delete->comments |

---

#### ProjectRolePermissions Table

Maps project-specific roles to their permission sets. This is where differentiation happens.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| fk_project_role_id | uuid | Reference to ProjectRoles (unique) |
| fk_permission_set_id | uuid | Reference to PermissionSets |
| created_at | long | Unix timestamp (ms) |

**Key Constraint:** `UNIQUE(fk_project_role_id)` - One permission set per project-role

**Example: Different "EDITOR" roles across projects**

| Project | Role | Permission Set | Actual Permissions |
|---------|------|----------------|--------------------|
| Project A | EDITOR | PermSet-1 (hash: abc...) | read->posts, write->posts |
| Project B | EDITOR | PermSet-2 (hash: def...) | read->posts, write->posts, delete->posts |
| Project C | EDITOR | PermSet-1 (hash: abc...) | read->posts, write->posts |

**Note:** Project A and C share the same permission set (detected via hash), demonstrating deduplication across different projects.

---

### 5. User Role Assignments

#### UserRoleAssignments Table

Assigns roles to users within projects. Users can have multiple roles.

| Column | Type | Description |
|--------|------|-------------|
| id | uuid | Unique identifier |
| fk_project_user_id | uuid | Reference to ProjectUsers |
| fk_project_role_id | uuid | Reference to ProjectRoles |
| assigned_at | long | Unix timestamp (ms) |

**Key Constraint:** `UNIQUE(fk_project_user_id, fk_project_role_id)` - Prevents duplicate role assignments

**Example: alice@ProjectA has multiple roles**

| User | Project | Assigned Roles |
|------|---------|----------------|
| alice | Project A | EDITOR, MODERATOR |

This results in 2 rows in UserRoleAssignments, both pointing to alice's ProjectUsers ID.

---

## Permission Checking Flow

**Question: Can user "alice" in "Project A" perform "edit" on "posts"?**

### Step-by-Step Resolution:

1. **Find the user**: Locate alice in ProjectUsers for Project A
2. **Get role assignments**: Find all roles assigned to alice in UserRoleAssignments
3. **Retrieve permission sets**: For each role, get the associated PermissionSet via ProjectRolePermissions
4. **Expand permissions**: Get all PermissionSetItems for each PermissionSet
5. **Match action-resource**: Check if any ActionResourceMap matches "edit" + "posts"
6. **Result**: If found, permission granted; otherwise, denied

### Visual Flow:

```
alice@ProjectA
    ↓
[UserRoleAssignments]
    ↓
[ProjectRoles: EDITOR, MODERATOR]
    ↓
[ProjectRolePermissions]
    ↓
[PermissionSets: set-123, set-456]
    ↓
[PermissionSetItems]
    ↓
[ActionResourceMap: read->posts, edit->posts, delete->comments, ...]
    ↓
Match found: edit->posts ✓
Permission: GRANTED
```

---

## Deduplication Strategy Summary

### What Gets Deduplicated?

| Entity | Scope | Deduplication Method |
|--------|-------|---------------------|
| Resources | Global | By name uniqueness |
| Actions | Global | By name uniqueness |
| Roles | Global | By name uniqueness |
| ActionResourceMap | Global | By action-resource pair |
| PermissionSets | Global | By content hash |

### What Remains Project-Specific?

- ProjectUsers (username scoped per project)
- ProjectRoles (which roles are enabled)
- ProjectRolePermissions (role-to-permissions mapping)
- UserRoleAssignments (user-to-role assignments)

---

## Scalability Characteristics

### Storage Efficiency

**For 1,000 projects with similar permission structures:**

Without deduplication:
- ~1,000 duplicate Resources entries
- ~1,000 duplicate Actions entries
- ~1,000 duplicate Roles entries
- ~50,000 duplicate PermissionSet configurations

With deduplication:
- ~10-20 unique Resources
- ~5-10 unique Actions
- ~5-10 unique Roles
- ~50-100 unique PermissionSets (via content hashing)

**Storage reduction: 95%+ for permission configuration data**

### Query Performance

**Permission check queries:**
- Single user query: 6-7 table joins
- Returns boolean in milliseconds with proper indexing
- Indexes on all foreign keys critical

**Optimization tips:**
- Index: `(username, fk_project_id)` on ProjectUsers
- Index: `(fk_project_user_id, fk_project_role_id)` on UserRoleAssignments
- Consider caching frequent permission checks

---

## Use Case Examples

### Example 1: Setting up a new project

1. Create entry in **Projects**: "Blog Platform"
2. Enable roles in **ProjectRoles**: ADMIN, EDITOR, USER
3. Define permission sets:
    - ADMIN: [all permissions]
    - EDITOR: [read->posts, write->posts, edit->posts]
    - USER: [read->posts, write->comments]
4. System checks content_hash for each permission set
5. Reuses existing PermissionSets or creates new ones as needed
6. Links roles to permission sets in **ProjectRolePermissions**

### Example 2: Adding a user with multiple roles

1. Create user in **ProjectUsers**: "bob"
2. Assign roles in **UserRoleAssignments**:
    - bob → EDITOR
    - bob → MODERATOR
3. Bob now has union of permissions from both roles

### Example 3: Changing role permissions

**Important: Permission sets are immutable (due to content hashing)**

To change "EDITOR" permissions in Project A:
1. Define new permission list
2. Compute new content_hash
3. Check if hash exists (reuse) or create new PermissionSet
4. Update ProjectRolePermissions to point to new PermissionSet
5. Old PermissionSet remains if other projects use it

---

## Content Hash Deep Dive

### Hash Computation Algorithm

```
function computePermissionSetHash(actionResourceMapIds):
    sorted_ids = sort(actionResourceMapIds)  // Alphanumeric sort
    concatenated = join(sorted_ids, ",")     // Example: "uuid-1,uuid-2,uuid-3"
    hash = SHA256(concatenated)              // Cryptographic hash
    return hash
```

### Hash Lookup Workflow

**Before creating a new PermissionSet:**

```
Step 1: Collect desired permissions
        [read->posts, write->posts, delete->comments]

Step 2: Get ActionResourceMap IDs
        [uuid-1, uuid-2, uuid-3]

Step 3: Compute hash
        hash = SHA256("uuid-1,uuid-2,uuid-3")
        result: "abc123def456..."

Step 4: Check PermissionSets table
        SELECT id FROM PermissionSets WHERE content_hash = 'abc123def456...'

Step 5a: If found → Reuse existing PermissionSet ID
Step 5b: If not found → Create new PermissionSet with this hash
```

### Why SHA-256?

- **Collision Resistance**: Virtually impossible for two different permission sets to produce the same hash
- **Deterministic**: Same input always produces same hash
- **Fixed Length**: Regardless of permission set size, hash is always 256 bits
- **Fast Comparison**: Comparing two hashes is faster than comparing permission lists

### Hash Benefits in Practice

1. **Instant Duplicate Detection**: O(1) lookup instead of O(n) comparison
2. **Audit Trail**: Hash serves as immutable fingerprint
3. **Change Detection**: Different hash = different permissions
4. **Simplified API**: "What permissions does role X have?" → Just check the hash
5. **Caching Key**: Use hash as cache key for permission lookup results

---

## Best Practices

### 1. Permission Set Management
- Always compute and check content_hash before creating new PermissionSets
- Never modify existing PermissionSets (immutability ensures cache consistency)
- Periodically clean up unused PermissionSets (no ProjectRolePermissions references)

### 2. Role Assignment
- Prefer fewer roles with more permissions over many roles with overlapping permissions
- Document role hierarchies in application layer
- Consider role inheritance in application logic, not database schema

### 3. Performance Optimization
- Cache user permissions in application layer (invalidate on role assignment changes)
- Use database indexes on all foreign key columns
- Consider materialized views for complex permission aggregations

### 4. Multi-Tenancy
- Always include project_id in queries to ensure tenant isolation
- Use row-level security policies if database supports them
- Never trust client-provided project_id; derive from authenticated session

---

## Conclusion

This multi-tenant RBAC system achieves a balance between flexibility and efficiency through:

1. **Global entity sharing** for Resources, Actions, and Roles
2. **Content-hash based deduplication** for PermissionSets
3. **Project-specific configuration** for role-permission mappings
4. **Scalable architecture** supporting thousands of tenants with minimal storage overhead

The content_hash approach is the cornerstone of the deduplication strategy, enabling automatic detection and reuse of identical permission configurations across projects while maintaining data integrity and query performance.