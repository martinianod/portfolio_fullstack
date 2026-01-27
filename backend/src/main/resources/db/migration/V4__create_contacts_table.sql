-- V4: Create contacts table for 1-to-many relationship with accounts
-- This migration creates the contacts table and migrates existing primary_contact_name data

-- 1. Create contacts table
CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    position VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to accounts with cascade delete
    CONSTRAINT fk_contacts_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- 2. Create indexes for performance
CREATE INDEX idx_contacts_account_id ON contacts(account_id);
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_is_primary ON contacts(account_id, is_primary) WHERE is_primary = true;

-- 3. Migrate existing primary_contact_name to contacts table
-- Only migrate if primary_contact_name is not null and not empty
INSERT INTO contacts (account_id, first_name, last_name, email, phone, is_primary, created_at, updated_at)
SELECT 
    id as account_id,
    -- Split name on first space, take first part as first_name
    CASE 
        WHEN position(' ' IN primary_contact_name) > 0 
        THEN substring(primary_contact_name FROM 1 FOR position(' ' IN primary_contact_name) - 1)
        ELSE primary_contact_name
    END as first_name,
    -- Split name on first space, take rest as last_name (or empty string)
    CASE 
        WHEN position(' ' IN primary_contact_name) > 0 
        THEN substring(primary_contact_name FROM position(' ' IN primary_contact_name) + 1)
        ELSE ''
    END as last_name,
    email,
    phone,
    true as is_primary,
    created_at,
    updated_at
FROM accounts
WHERE primary_contact_name IS NOT NULL 
  AND trim(primary_contact_name) != '';

-- 4. Remove the old primary_contact_name column from accounts
ALTER TABLE accounts DROP COLUMN IF EXISTS primary_contact_name;

-- 5. Add comment to table for documentation
COMMENT ON TABLE contacts IS 'Contacts associated with accounts. Each account can have multiple contacts, with one marked as primary.';
COMMENT ON COLUMN contacts.is_primary IS 'Only one contact per account should be marked as primary at any given time.';
