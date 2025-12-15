-- Payments table
CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    terminal_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    tip_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    transaction_id VARCHAR(255),
    authorization_code VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    synced BOOLEAN NOT NULL DEFAULT FALSE,
    synced_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'DECLINED', 'CANCELLED', 'REFUNDED', 'FAILED')),
    CONSTRAINT chk_payment_method CHECK (method IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'MOBILE_PAYMENT', 'GIFT_CARD'))
);

-- Payment splits table
CREATE TABLE payment_splits (
    id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    CONSTRAINT fk_splits_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    CONSTRAINT chk_split_method CHECK (method IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'MOBILE_PAYMENT', 'GIFT_CARD'))
);

-- Offline payment queue table
CREATE TABLE offline_payment_queue (
    id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    payment_data TEXT NOT NULL,
    queued_at TIMESTAMP NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT chk_queue_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Indexes for Payment Service
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_synced ON payments(synced, created_at);
CREATE INDEX idx_payments_idempotency ON payments(idempotency_key);
CREATE INDEX idx_payment_splits_payment_id ON payment_splits(payment_id);
CREATE INDEX idx_offline_queue_status ON offline_payment_queue(status, next_retry_at);
CREATE INDEX idx_offline_queue_payment_id ON offline_payment_queue(payment_id);