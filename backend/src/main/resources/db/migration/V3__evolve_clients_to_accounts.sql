-- V3: Evolve clients table to accounts with extensibility
-- This migration renames the clients table and adds new fields for better CRM functionality

-- 1. Rename table from clients to accounts
ALTER TABLE clients RENAME TO accounts;

-- 2. Add new columns for enhanced functionality
ALTER TABLE accounts ADD COLUMN slug VARCHAR(255);
ALTER TABLE accounts ADD COLUMN industry VARCHAR(255);
ALTER TABLE accounts ADD COLUMN website VARCHAR(500);
ALTER TABLE accounts ADD COLUMN owner_id BIGINT;
ALTER TABLE accounts ADD COLUMN custom_fields JSONB DEFAULT '{}';

-- 3. Generate slugs from existing names (lowercase, replace non-alphanumeric with hyphens)
UPDATE accounts SET slug = lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9\s-]', '', 'g'), '\s+', '-', 'g'));

-- 4. Clean up slugs (remove leading/trailing hyphens, collapse multiple hyphens)
UPDATE accounts SET slug = regexp_replace(regexp_replace(slug, '^-+|-+$', '', 'g'), '-+', '-', 'g');

-- 5. Handle potential duplicate slugs by appending id
UPDATE accounts a1
SET slug = slug || '-' || id
WHERE EXISTS (
    SELECT 1 FROM accounts a2 
    WHERE a2.slug = a1.slug AND a2.id < a1.id
);

-- 6. Add constraints
ALTER TABLE accounts ALTER COLUMN slug SET NOT NULL;
ALTER TABLE accounts ADD CONSTRAINT uk_accounts_slug UNIQUE (slug);
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

-- 7. Update index names
DROP INDEX IF EXISTS idx_clients_email;
CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_accounts_slug ON accounts(slug);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_owner_id ON accounts(owner_id);

-- 8. Create GIN index on custom_fields for efficient JSONB queries
CREATE INDEX idx_accounts_custom_fields ON accounts USING GIN (custom_fields);

-- 9. Update FK references in projects table (will be done in V5)
-- Note: We keep the column name as client_id for now for backward compatibility
-- It will be renamed to account_id in V5 when we enhance the projects table
