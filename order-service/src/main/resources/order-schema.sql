-- Orders table
CREATE TABLE orders (
    id VARCHAR(255) PRIMARY KEY,
    table_id VARCHAR(255) NOT NULL,
    server_id VARCHAR(255) NOT NULL,
    terminal_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    synced BOOLEAN NOT NULL DEFAULT FALSE,
    synced_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_order_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'IN_PROGRESS', 'READY', 'DELIVERED', 'PAID', 'CANCELLED'))
);

-- Order items table
CREATE TABLE order_items (
    id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    menu_item_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    course_type VARCHAR(50),
    sent_to_kitchen_at TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_item_status CHECK (status IN ('PENDING', 'SENT_TO_KITCHEN', 'PREPARING', 'READY', 'SERVED', 'CANCELLED'))
);

-- Item modifications table
CREATE TABLE item_modifications (
    id VARCHAR(255) PRIMARY KEY,
    order_item_id VARCHAR(255) NOT NULL,
    modification_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price_adjustment DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    special_instructions TEXT,
    CONSTRAINT fk_modifications_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE
);

-- Indexes for Order Service
CREATE INDEX idx_orders_table_id ON orders(table_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_synced ON orders(synced, created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_status ON order_items(status);
