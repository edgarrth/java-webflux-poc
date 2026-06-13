INSERT INTO payments(payment_id, merchant_id, customer_id, amount, currency, status, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'merchant-lima-001', 'customer-001', 120.50, 'PEN', 'RECEIVED', now() - interval '20 minutes'),
('22222222-2222-2222-2222-222222222222', 'merchant-lima-001', 'customer-002', 350.00, 'PEN', 'AUTHORIZED', now() - interval '15 minutes'),
('33333333-3333-3333-3333-333333333333', 'merchant-lima-002', 'customer-003', 5400.00, 'PEN', 'RECEIVED', now() - interval '10 minutes'),
('44444444-4444-4444-4444-444444444444', 'merchant-lima-001', 'customer-004', 80.00, 'USD', 'RECEIVED', now() - interval '5 minutes')
ON CONFLICT (payment_id) DO NOTHING;
