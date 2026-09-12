# BidYatra Staff Management System — Full Stack

Professional company daily activity system for **BIDYATRA MOBILITY TECHNOLOGIES PRIVATE LIMITED**.

## Architecture

```text
Frontend (HTML/CSS/JS)
        |
        | REST API + JWT
        v
Spring Boot Backend
        |
        v
MongoDB
```

## Included

### Frontend
- Professional BidYatra branded dashboard
- BidYatra logo
- Admin/Staff login UI
- Daily activity form
- Staff records table
- Search and date filter
- Dashboard statistics
- Edit/Delete records
- Staff account management UI for Admin
- Excel/PDF export buttons
- Responsive mobile design

### Backend
- Java 17 + Spring Boot
- Spring Security
- JWT authentication
- ADMIN / STAFF roles
- BCrypt password hashing
- MongoDB
- Activity CRUD REST API
- Admin staff account API
- Excel export with Apache POI
- PDF export with OpenPDF
- CORS for local frontend

## Requirements

- Java 17+
- Maven 3.9+
- MongoDB 7/8+ or MongoDB Atlas
- Python 3+ only if you want to serve the frontend with `python -m http.server`

## 1. Start MongoDB

With Docker:

```bash
docker compose up -d
```

Or run MongoDB locally.

## 2. Start Backend

```bash
mvn clean spring-boot:run
```

Backend:
`http://localhost:8080`

## 3. Start Frontend separately

```bash
cd frontend
python -m http.server 5500
```

Open:
`http://localhost:5500`

The frontend calls `http://localhost:8080/api`.

## Default Admin

```text
Username: admin@bidyatra.com
Password: Admin@12345
```

Change the password and JWT secret before production.

## Alternative: single-server mode

The same frontend is also copied into Spring Boot's `src/main/resources/static/` folder. Therefore you can simply run the backend and open:

`http://localhost:8080`

No separate frontend server is required in this mode.

## Production checklist

- HTTPS
- Environment variables for JWT secret and admin credentials
- Strong admin password + password change/reset
- Rate limiting and login lockout
- MongoDB authentication/network restrictions
- Automated database backups
- Audit logging
- Pagination for large records
- Proper domain/CORS configuration
- Reverse proxy such as Nginx
