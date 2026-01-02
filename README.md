# Fast Food Order Taking System

A modern, fast, minimal desktop application for fast food order taking with centralized online database and analytics.

## Architecture

- **Frontend**: Angular 17 with Electron/PWA support
- **Backend**: Java 21 (Spring Boot 3.2) with Clean Architecture
- **Database**: PostgreSQL (online centralized)
- **Security**: JWT authentication
- **Architecture**: Clean Architecture + SOLID + OOP principles

## Features

### Core Functionality
- Fast & reliable order taking
- Support for Table Pickup, Takeaway, and Home Delivery
- Menu management with categories, sizes, and add-ons
- Combo meals support
- Voucher-based discounts
- Cash on spot and cash on delivery payments

### Admin Dashboard
- Sales analytics (current month, last 3 months, last 12 months)
- Graphical charts (line, bar, pie charts)
- Category-wise sales analysis
- Popular items tracking
- Combo performance metrics
- Filter by date range, category, item, and branch

### Security
- JWT authentication with refresh tokens
- Role-based access control (Admin, Branch Manager)
- Secure API endpoints

### Multi-language Support
- English
- Urdu (RTL support)

## Project Structure

```
fast-food-order-api/
├── src/
│   ├── main/
│   │   ├── java/com/fastfood/order/
│   │   │   ├── domain/          # Domain entities
│   │   │   ├── application/     # Application services & DTOs
│   │   │   ├── infrastructure/  # Repositories, security, external services
│   │   │   └── presentation/     # REST controllers
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/
├── database/
│   └── schema.sql               # Database schema SQL script
├── frontend/                     # Angular application
│   └── src/
│       ├── app/
│       │   ├── components/
│       │   └── services/
│       └── assets/
│           └── i18n/             # Translation files
└── pom.xml
```

## Setup Instructions

### Prerequisites
- Java 21 (LTS)
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+ and npm
- Angular CLI 17+

### Backend Setup

1. **Database Setup**
   ```sql
   CREATE DATABASE fastfood_order_db;
   CREATE USER fastfood_user WITH PASSWORD 'fastfood_password';
   GRANT ALL PRIVILEGES ON DATABASE fastfood_order_db TO fastfood_user;
   ```

2. **Create Database Schema**
   - Run the SQL script located at `database/schema.sql` to create all tables and initial data
   ```bash
   psql -U fastfood_user -d fastfood_order_db -f database/schema.sql
   ```

3. **Configure Application**
   - Copy `.env.example` to `.env` (or set environment variables)
   - Update environment variables with your database credentials:
     ```bash
     DATABASE_URL=jdbc:postgresql://localhost:5432/fast-food
     DATABASE_USERNAME=your_username
     DATABASE_PASSWORD=your_password
     JWT_SECRET_KEY=your-strong-secret-key
     API_KEY=your-api-key
     ```
   - For development, you can also use `application-dev.yml` profile
   - For production, use `application-prod.yml` profile (requires environment variables)

4. **Run Application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Default Admin Credentials**
   - Username: `admin`
   - Password: `Admin@123`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Run Development Server**
   ```bash
   ng serve
   ```

3. **Build for Production**
   ```bash
   ng build --configuration production
   ```

4. **Build Electron Desktop App**
   ```bash
   npm run electron:build
   ```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login

### Orders
- `POST /api/orders` - Create new order
- `GET /api/orders` - List orders
- `GET /api/orders/{id}` - Get order details

### Analytics (Admin Only)
- `GET /api/admin/analytics/sales` - Sales analytics

### Menu Management
- `GET /api/menu/categories` - List categories
- `GET /api/menu/items` - List menu items
- `POST /api/menu/items` - Create menu item

## Database Schema

The database schema is defined in `database/schema.sql`. Run this script manually to set up the database.

Key entities:
- **branches** - Branch/location information
- **users** - System users with roles
- **roles** - User roles (ADMIN, BRANCH_MANAGER)
- **menu_categories** - Menu categories
- **menu_items** - Individual menu items
- **orders** - Customer orders
- **order_items** - Items in each order
- **vouchers** - Discount vouchers
- **franchise_inquiries** - Franchise inquiry submissions

**Note**: This project does not use Liquibase. Database migrations should be managed manually using SQL scripts.

## Security

- All API endpoints require JWT authentication (except `/api/auth/**` and `/api/public/**`)
- Role-based access control:
  - `/api/admin/**` - Admin only
  - `/api/branch-manager/**` - Admin and Branch Manager

## Development

### Running Tests
```bash
mvn test
```

### Code Style
- Follow SOLID principles
- Use Clean Architecture layers
- Follow Java naming conventions
- Use Lombok for boilerplate reduction

## Future Enhancements

- Multi-branch support (database ready)
- Online payment gateway integration
- Rider tracking system
- Mobile app integration
- Advanced franchise management

## License

Proprietary - All rights reserved

