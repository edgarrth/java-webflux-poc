CREATE TABLE IF NOT EXISTS payments (
    payment_id UUID PRIMARY KEY,
    merchant_id VARCHAR(80) NOT NULL,
    customer_id VARCHAR(80) NOT NULL,
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payments_merchant_id ON payments(merchant_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
