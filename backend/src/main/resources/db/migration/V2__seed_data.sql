-- Insert default admin user
-- Default password is 'admin123' - MUST be changed in production
-- BCrypt hash for 'admin123' (generated with BCrypt rounds=10)
INSERT INTO users (username, email, password_hash, full_name, role, enabled)
VALUES (
    'admin',
    'admin@martiniano.dev',
    '$2b$10$K3r/gVEEdhrVO0wHZn4Ywu8BSHY1c3Bv6gLyfGgRt8R/.YFfh2BT.',
    'Admin User',
    'ADMIN',
    true
) ON CONFLICT (username) DO NOTHING;

-- Insert sample lead data for demo purposes
INSERT INTO leads (name, email, phone, company, budget_range, project_type, message, source, stage, priority)
VALUES 
    ('John Smith', 'john.smith@example.com', '+1234567890', 'Tech Startup Inc', '$5k-$10k', 'Web Application', 'We need a custom CRM solution for our sales team', 'web', 'NEW', 'HIGH'),
    ('Maria Garcia', 'maria@designco.com', '+1234567891', 'DesignCo', '$2k-$5k', 'Landing Page', 'Looking for a modern landing page with animations', 'web', 'CONTACTED', 'MEDIUM'),
    ('David Chen', 'david@ecommerce.com', '+1234567892', 'E-Commerce Plus', '$10k+', 'E-Commerce', 'Need to build a full e-commerce platform', 'email', 'QUALIFIED', 'HIGH')
ON CONFLICT DO NOTHING;
