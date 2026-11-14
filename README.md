# Habits_Service
This is the service for managing habits

## Running locally

Java 21. Start MySQL with `docker compose up -d` (from `Habits/`, port 3310), then
`./gradlew bootRun`. The service listens on **8085**.

Set `JWT_SECRET` to the same Base64 key the user, task and goal services use — tokens
are validated with the shared contract (`security/JwtContract.java`: issuer, audience,
numeric `id`, uppercase `roles`). Optional: `JWT_ISSUER`, `JWT_AUDIENCE`,
`CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`).
