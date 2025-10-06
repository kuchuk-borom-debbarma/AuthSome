# Authsome - Brief Overview

## What is Authsome?

Authsome is a **self-hostable authentication SaaS** that can be:
- Hosted by us as a managed service, or
- Self-hosted by organizations on their own infrastructure

Think of it as "authentication-as-a-service" for your applications.

---

## User Types

### 1. **Authsome Users**
These are **your customers** - developers and companies who sign up to use Authsome. They:
- Create an account on Authsome
- Manage their projects
- Configure authentication for their applications
- Get API keys and manage settings

**Example**: Sarah, a developer building a social media app, signs up for Authsome.

### 2. **Project Users**
These are the **end-users** of your customers' applications. They:
- Don't interact with Authsome directly
- Sign up/log in to the application (e.g., the social media app)
- Are managed through Authsome's API by the Authsome User

**Example**: John signs up for Sarah's social media app. John is a "Project User" in Sarah's Authsome project.

---

## Key Concepts

### Projects
A **project** represents one application or service that an Authsome User wants to add authentication to.

- Each Authsome User can create multiple projects
- Each project gets its own API keys
- Project Users are isolated per project

**Example**: Sarah might have:
- Project 1: "Social Media App" (1,000 users)
- Project 2: "E-commerce Store" (500 users)
- Project 3: "Internal Dashboard" (20 users)

---

## Permission Systems (Planned)

### RBAC (Role-Based Access Control)
Coming first - simple role-based permissions:
- Define roles (Admin, Moderator, User, etc.)
- Assign roles to Project Users
- Check permissions based on roles

### ABAC (Attribute-Based Access Control)
Coming later - complex policy-based permissions:
- Define policies based on attributes
- Support conditions like time, location, resource ownership
- More flexible than RBAC for complex scenarios

---

*This is a living document and will be updated as Authsome evolves.*