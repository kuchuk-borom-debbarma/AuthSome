# Project structure
The project follows a loose variant of Domain Driven Design (DDD) with it's main goal being that everything is self contained within it's domain and only exposes DTOs and APIs. This is to make every module independent making it easy to follow and de-couple into independent microservice if required in future.
# Modules
## Cloud
Will contain all cloud related code and integrations.
## Core
Will contains all core auth code and logic.
## Orchestrator
Orchestrator will contain all the code to orchestrate between different modules.