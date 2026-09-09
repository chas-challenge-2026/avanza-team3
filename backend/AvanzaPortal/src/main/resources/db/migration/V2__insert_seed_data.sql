INSERT INTO users (name, email, password_hash) VALUES
('Anna Lindqvist', 'anna@example.com', '$2y$10$vb7QdzuTR07MtqNjngvG3udHGi9MiEvrNzvOWRJGmRDPG8mUsAZtK'),
('Erik Johansson', 'erik@example.com', '$2y$10$vb7QdzuTR07MtqNjngvG3udHGi9MiEvrNzvOWRJGmRDPG8mUsAZtK');

INSERT INTO accounts (user_id, account_type, account_name) VALUES
(1, 'ISK', 'Anna ISK'),
(1, 'KF', 'Anna KF'),
(1, 'Depa', 'Anna Depå');

INSERT INTO holdings (account_id, ticker, instrument_name, quantity, avg_buy_price, currency) VALUES
(1, 'ERIC-B', 'Ericsson B', 500, 68.50, 'SEK'),
(1, 'VOLV-B', 'Volvo B', 100, 245.00, 'SEK'),
(1, 'AAPL', 'Apple Inc', 50, 165.00, 'USD'),
(2, 'SWED-A', 'Swedbank A', 200, 185.00, 'SEK'),
(3, 'SAND', 'Sandvik', 300, 205.00, 'SEK');

INSERT INTO target_allocations (user_id, account_type, target_pct) VALUES
(1, 'ISK', 60.00),
(1, 'KF', 25.00),
(1, 'Depa', 15.00);

INSERT INTO alerts (user_id, alert_type, message) VALUES
(1, 'DRIFT', 'ISK-allokering avviker 8% från mål (60%). Överväg ombalansering.'),
(1, 'DRIFT', 'KF-allokering avviker 6% från mål (25%).');
