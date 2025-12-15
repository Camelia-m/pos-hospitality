-- Kitchen tickets table
CREATE TABLE kitchen_tickets (
    id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    table_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    station_id VARCHAR(255) NOT NULL,
    received_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_minutes INTEGER,
    CONSTRAINT chk_ticket_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'READY', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_ticket_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'RUSH'))
);

-- Ticket items table
CREATE TABLE ticket_items (
    id VARCHAR(255) PRIMARY KEY,
    ticket_id VARCHAR(255) NOT NULL,
    order_item_id VARCHAR(255) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    course_type VARCHAR(50),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_ticket_items_ticket FOREIGN KEY (ticket_id) REFERENCES kitchen_tickets(id) ON DELETE CASCADE,
    CONSTRAINT chk_ticket_item_status CHECK (status IN ('PENDING', 'PREPARING', 'READY', 'SERVED'))
);

-- Ticket item modifications table (for display purposes)
CREATE TABLE ticket_item_modifications (
    ticket_item_id VARCHAR(255) NOT NULL,
    modifications VARCHAR(500),
    CONSTRAINT fk_modifications_ticket_item FOREIGN KEY (ticket_item_id) REFERENCES ticket_items(id) ON DELETE CASCADE
);

-- Indexes for Kitchen Service
CREATE INDEX idx_tickets_status ON kitchen_tickets(status);
CREATE INDEX idx_tickets_station ON kitchen_tickets(station_id, status);
CREATE INDEX idx_tickets_priority ON kitchen_tickets(priority DESC, received_at ASC);
CREATE INDEX idx_tickets_order_id ON kitchen_tickets(order_id);
CREATE INDEX idx_ticket_items_ticket_id ON ticket_items(ticket_id);
