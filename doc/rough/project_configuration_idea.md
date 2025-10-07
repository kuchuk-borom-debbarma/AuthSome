# Authsome Configuration System - MVP

## Configuration Levels

Authsome supports a two-tier configuration hierarchy:

1. **Project-level** - Default settings applied to all users in the project
2. **Role-level** - Overrides for specific roles (e.g., Admin, Moderator, User)

**Precedence Rule:** Role-level configs override Project-level configs when set.

---

## Core Configuration Settings

### Session & Token Management

| Setting | Description | Default | Configurable At |
|---------|-------------|---------|-----------------|
| Max concurrent sessions | Maximum number of active sessions per user | 3 | Project, Role |
| Access token expiry | Duration before access token expires (seconds) | 900 (15 min) | Project, Role |
| Refresh token expiry | Duration before refresh token expires (seconds) | 604800 (7 days) | Project, Role |
| Refresh token rotation | Whether to rotate refresh tokens on use | Yes | Project, Role |
| Session timeout | Automatic logout after inactivity (seconds) | 1800 (30 min) | Project, Role |

---

### Password Policies

| Setting | Description | Default | Configurable At |
|---------|-------------|---------|-----------------|
| Minimum password length | Minimum characters required | 8 | Project, Role |
| Require uppercase | Must contain uppercase letter | No | Project, Role |
| Require number | Must contain at least one number | No | Project, Role |
| Require special character | Must contain special character | No | Project, Role |
| Max failed login attempts | Login attempts before account lockout | 5 | Project, Role |
| Lockout duration | How long account stays locked (minutes) | 15 | Project, Role |
| Password reset link expiry | Time before reset link expires (minutes) | 60 | Project |

---

### Registration & User Management

| Setting | Description | Default | Configurable At |
|---------|-------------|---------|-----------------|
| Email verification required | New users must verify email | Yes | Project |
| Auto-approve registrations | Automatically approve new users | Yes | Project |
| Allowed email domains | Whitelist of allowed email domains | Empty (all allowed) | Project |
| Blocked email domains | Blacklist of blocked email domains | Empty | Project |
| Block disposable emails | Reject temporary/disposable email addresses | No | Project |

---

### Security & Rate Limiting

| Setting | Description | Default | Configurable At |
|---------|-------------|---------|-----------------|
| Max login attempts (IP) | Login attempts per IP per hour | 10 | Project |
| Max registration attempts (IP) | Registration attempts per IP per hour | 3 | Project |
| Max password reset requests | Password reset requests per user per hour | 3 | Project |
| Allowed CORS origins | List of allowed origins for CORS | Empty (none) | Project |

---

## Usage Examples

### Example 1: Social Media App

**Project-level configuration:**
```json
{
  "maxSessions": 5,
  "accessTokenExpiry": 900,
  "passwordMinLength": 8,
  "requireSpecialChar": true,
  "emailVerificationRequired": true,
  "autoApproveRegistrations": true
}
```

**Role-level override (Admin):**
```json
{
  "maxSessions": 10,
  "sessionTimeout": 7200
}
```
*Admins get 10 concurrent sessions and 2-hour timeout, everything else inherited from project.*

---

### Example 2: Internal Employee Dashboard

**Project-level configuration:**
```json
{
  "maxSessions": 2,
  "accessTokenExpiry": 600,
  "sessionTimeout": 900,
  "allowedEmailDomains": ["company.com"],
  "emailVerificationRequired": true,
  "requireUppercase": true,
  "requireNumber": true,
  "requireSpecialChar": true
}
```

*Stricter security for internal tools with email domain restriction.*

---

### Example 3: Blog Platform

**Project-level configuration:**
```json
{
  "maxSessions": 3,
  "passwordMinLength": 8,
  "emailVerificationRequired": false,
  "autoApproveRegistrations": true,
  "blockDisposableEmails": true
}
```

**Role-level override (Free User):**
```json
{
  "maxSessions": 1
}
```

**Role-level override (Premium User):**
```json
{
  "maxSessions": 5
}
```

*Different session limits based on subscription tier.*

---

## What's NOT Included in MVP

These features are planned for future releases:

- JWT type selection (symmetric vs asymmetric) - MVP uses HS256 only
- Custom JWT claims
- Advanced token revocation strategies
- Webhooks for auth events
- IP whitelisting/blacklisting
- Sliding session windows
- Password history tracking
- User-level configuration overrides
- Policy-based (ABAC) configuration rules

---

## Data Model

### Project Configuration Schema
```typescript
interface ProjectConfig {
  projectId: string;
  
  // Sessions & Tokens
  maxSessions: number;
  accessTokenExpiry: number;        // seconds
  refreshTokenExpiry: number;       // seconds
  refreshTokenRotation: boolean;
  sessionTimeout: number;           // seconds
  
  // Password Policies
  passwordMinLength: number;
  requireUppercase: boolean;
  requireNumber: boolean;
  requireSpecialChar: boolean;
  maxFailedLoginAttempts: number;
  lockoutDuration: number;          // minutes
  passwordResetLinkExpiry: number;  // minutes
  
  // Registration & User Management
  emailVerificationRequired: boolean;
  autoApproveRegistrations: boolean;
  allowedEmailDomains: string[];
  blockedEmailDomains: string[];
  blockDisposableEmails: boolean;
  
  // Security & Rate Limiting
  maxLoginAttemptsPerIP: number;
  maxRegistrationAttemptsPerIP: number;
  maxPasswordResetRequests: number;
  allowedCorsOrigins: string[];
}
```

### Role Configuration Schema
```typescript
interface RoleConfig {
  projectId: string;
  roleId: string;
  
  // Only include fields that override project defaults
  maxSessions?: number;
  accessTokenExpiry?: number;
  refreshTokenExpiry?: number;
  refreshTokenRotation?: boolean;
  sessionTimeout?: number;
  passwordMinLength?: number;
  requireUppercase?: boolean;
  requireNumber?: boolean;
  requireSpecialChar?: boolean;
  maxFailedLoginAttempts?: number;
  lockoutDuration?: number;
}
```

---

## Configuration Resolution Logic

When a project user performs an action (login, registration, etc.):

1. Load the user's assigned role(s)
2. Check if role has a specific config for the requested setting
3. If role config exists, use it
4. If not, fall back to project-level config
5. If not set at project level, use system default

**Example:**
```
User "john" with role "admin" tries to login
↓
Check: Does "admin" role have maxSessions configured?
  Yes → Use role value (10)
  No  → Use project value (3)
```

---

## Implementation Notes

### Storage
- Store project configs in a `project_configs` table/collection
- Store role configs in a `role_configs` table/collection (sparse, only overrides)
- Cache resolved configs in Redis for performance

### Validation
- Validate config values on save (e.g., min password length ≥ 4)
- Provide sensible defaults for all fields
- Reject invalid combinations (e.g., access token expiry > refresh token expiry)

### API Design
```
GET    /api/projects/:projectId/config
PUT    /api/projects/:projectId/config
GET    /api/projects/:projectId/roles/:roleId/config
PUT    /api/projects/:projectId/roles/:roleId/config
DELETE /api/projects/:projectId/roles/:roleId/config  # Remove overrides
```

---

*Last updated: 2025-10-08*