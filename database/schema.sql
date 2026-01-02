-- Fast Food Order System Database Schema
-- Run this script manually to create the database schema

-- ============================================
-- TABLES
-- ============================================

-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'Pakistan',
    phone VARCHAR(20),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE branches IS 'Stores branch/location information for multi-branch support';

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE roles IS 'User roles: ADMIN, BRANCH_MANAGER';

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role_id BIGINT NOT NULL,
    branch_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_branch_id ON users(branch_id);

-- Menu Categories table
CREATE TABLE IF NOT EXISTS menu_categories (
    id BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_ur VARCHAR(255),
    description_en TEXT,
    description_ur TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_menu_categories_display_order ON menu_categories(display_order);
CREATE INDEX IF NOT EXISTS idx_menu_categories_active ON menu_categories(is_active);

-- Menu Items table
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    name_ur VARCHAR(255),
    description_en TEXT,
    description_ur TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    image_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_combo BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_menu_items_category FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);

CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_available ON menu_items(is_available);
CREATE INDEX IF NOT EXISTS idx_menu_items_display_order ON menu_items(display_order);

-- Menu Item Sizes table
CREATE TABLE IF NOT EXISTS menu_item_sizes (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    size_code VARCHAR(10) NOT NULL,
    size_name_en VARCHAR(50) NOT NULL,
    size_name_ur VARCHAR(50),
    price_modifier DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_available BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_item_sizes_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT uk_menu_item_size UNIQUE (menu_item_id, size_code)
);

CREATE INDEX IF NOT EXISTS idx_menu_item_sizes_item ON menu_item_sizes(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_sizes_available ON menu_item_sizes(is_available);

-- Add-ons table
CREATE TABLE IF NOT EXISTS add_ons (
    id BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_ur VARCHAR(255),
    description_en TEXT,
    description_ur TEXT,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_available BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_add_ons_available ON add_ons(is_available);
CREATE INDEX IF NOT EXISTS idx_add_ons_display_order ON add_ons(display_order);

-- Menu Item Add-ons table
CREATE TABLE IF NOT EXISTS menu_item_add_ons (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    add_on_id BIGINT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_item_add_ons_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_item_add_ons_addon FOREIGN KEY (add_on_id) REFERENCES add_ons(id) ON DELETE CASCADE,
    CONSTRAINT uk_menu_item_addon UNIQUE (menu_item_id, add_on_id)
);

CREATE INDEX IF NOT EXISTS idx_menu_item_add_ons_item ON menu_item_add_ons(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_add_ons_addon ON menu_item_add_ons(add_on_id);

-- Combos table
CREATE TABLE IF NOT EXISTS combos (
    id BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_ur VARCHAR(255),
    description_en TEXT,
    description_ur TEXT,
    combo_price DECIMAL(10, 2) NOT NULL,
    image_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_combos_available ON combos(is_available);
CREATE INDEX IF NOT EXISTS idx_combos_display_order ON combos(display_order);

-- Combo Items table
CREATE TABLE IF NOT EXISTS combo_items (
    id BIGSERIAL PRIMARY KEY,
    combo_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_combo_items_combo FOREIGN KEY (combo_id) REFERENCES combos(id) ON DELETE CASCADE,
    CONSTRAINT fk_combo_items_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT uk_combo_item UNIQUE (combo_id, menu_item_id, display_order)
);

CREATE INDEX IF NOT EXISTS idx_combo_items_combo ON combo_items(combo_id);
CREATE INDEX IF NOT EXISTS idx_combo_items_item ON combo_items(menu_item_id);

-- Vouchers table
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description_en TEXT,
    description_ur TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2),
    max_discount_amount DECIMAL(10, 2),
    usage_limit INTEGER,
    used_count INTEGER NOT NULL DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_vouchers_code ON vouchers(code);
CREATE INDEX IF NOT EXISTS idx_vouchers_active ON vouchers(is_active);
CREATE INDEX IF NOT EXISTS idx_vouchers_valid_dates ON vouchers(valid_from, valid_until);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    branch_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    table_number VARCHAR(10),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    delivery_address TEXT,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    voucher_id BIGINT,
    voucher_code VARCHAR(50),
    total_amount DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_orders_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_branch ON orders(branch_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_created_by ON orders(created_by);
CREATE INDEX IF NOT EXISTS idx_orders_date_status ON orders(order_date, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_branch_date ON orders(branch_id, order_date);

-- Order Items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT,
    combo_id BIGINT,
    item_name_en VARCHAR(255) NOT NULL,
    item_name_ur VARCHAR(255),
    size_code VARCHAR(10),
    size_name_en VARCHAR(50),
    size_name_ur VARCHAR(50),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    CONSTRAINT fk_order_items_combo FOREIGN KEY (combo_id) REFERENCES combos(id),
    CONSTRAINT chk_order_item_source CHECK (menu_item_id IS NOT NULL OR combo_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item ON order_items(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_order_items_combo ON order_items(combo_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_menu ON order_items(order_id, menu_item_id);

-- Order Item Add-ons table
CREATE TABLE IF NOT EXISTS order_item_add_ons (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    add_on_id BIGINT NOT NULL,
    add_on_name_en VARCHAR(255) NOT NULL,
    add_on_name_ur VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_add_ons_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_add_ons_addon FOREIGN KEY (add_on_id) REFERENCES add_ons(id)
);

CREATE INDEX IF NOT EXISTS idx_order_item_add_ons_order_item ON order_item_add_ons(order_item_id);
CREATE INDEX IF NOT EXISTS idx_order_item_add_ons_addon ON order_item_add_ons(add_on_id);

-- Franchise Inquiries table
CREATE TABLE IF NOT EXISTS franchise_inquiries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city VARCHAR(100),
    province VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Pakistan',
    investment_range VARCHAR(50),
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_franchise_inquiries_status ON franchise_inquiries(status);
CREATE INDEX IF NOT EXISTS idx_franchise_inquiries_created_at ON franchise_inquiries(created_at);

-- Brand Config table
CREATE TABLE IF NOT EXISTS brand_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_brand_config_key ON brand_config(config_key);

-- Settings table
CREATE TABLE IF NOT EXISTS settings (
    id BIGSERIAL PRIMARY KEY,
    brand_name VARCHAR(255) NOT NULL,
    brand_logo_url TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_settings_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_settings_created_by ON settings(created_by);
CREATE INDEX IF NOT EXISTS idx_settings_updated_by ON settings(updated_by);

COMMENT ON TABLE settings IS 'Stores brand settings including name, logo, and contact information';

-- ============================================
-- INITIAL DATA
-- ============================================

-- Insert roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'System administrator with full access'),
('BRANCH_MANAGER', 'Branch manager with order and menu visibility')
ON CONFLICT (name) DO NOTHING;

-- Insert initial branch
INSERT INTO branches (name, address, city, province, country, phone, email, is_active) VALUES
('Main Branch', '123 Main Street', 'Gujranwala', 'Punjab', 'Pakistan', '+92-300-1234567', 'main@fastfood.com', true)
ON CONFLICT DO NOTHING;

-- Insert admin user (Password: Admin@123 - BCrypt hash)
INSERT INTO users (username, email, password_hash, full_name, role_id, branch_id, is_active) VALUES
('admin', 'admin@fastfood.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 
 (SELECT id FROM roles WHERE name = 'ADMIN'), NULL, true)
ON CONFLICT (username) DO NOTHING;

-- Insert menu categories
INSERT INTO menu_categories (name_en, name_ur, description_en, display_order) VALUES
('Burgers', 'برگر', 'Delicious burgers', 1),
('Pizzas', 'پیزا', 'Fresh pizzas', 2),
('Fries', 'فرائز', 'Crispy fries', 3),
('Drinks', 'مشروبات', 'Cold drinks', 4),
('Deals / Combos', 'ڈیلز / کمبو', 'Special combo deals', 5)
ON CONFLICT DO NOTHING;

-- Insert brand config
INSERT INTO brand_config (config_key, config_value, description) VALUES
('BRAND_NAME', 'Fast Food Express', 'Brand name'),
('BRAND_LOCATION', 'Gujranwala, Pakistan', 'Brand location'),
('DEFAULT_CURRENCY', 'PKR', 'Default currency'),
('TAX_RATE', '0', 'Tax rate (included in price)'),
('DELIVERY_CHARGE_INCLUDED', 'true', 'Whether delivery charges are included in item prices')
ON CONFLICT (config_key) DO NOTHING;

