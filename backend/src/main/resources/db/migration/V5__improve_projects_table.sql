-- V5: Enhance projects table to support client and internal projects
-- This migration makes account_id nullable and adds new fields for project management

-- 1. Update projects table - make client_id (account_id) nullable for internal projects
ALTER TABLE projects ALTER COLUMN client_id DROP NOT NULL;

-- 2. Rename client_id to account_id for consistency
ALTER TABLE projects RENAME COLUMN client_id TO account_id;

-- 3. Add new columns for enhanced project management
ALTER TABLE projects ADD COLUMN code VARCHAR(255);
ALTER TABLE projects ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'CLIENT';
ALTER TABLE projects ADD COLUMN owner_id BIGINT;
ALTER TABLE projects ADD COLUMN team TEXT;
ALTER TABLE projects ADD COLUMN tags VARCHAR(500);
ALTER TABLE projects ADD COLUMN custom_fields JSONB DEFAULT '{}';
ALTER TABLE projects ADD COLUMN links TEXT;
ALTER TABLE projects ADD COLUMN github_repo_id BIGINT;

-- 4. Generate unique codes for existing projects (slug format from name)
UPDATE projects 
SET code = lower(
    regexp_replace(
        regexp_replace(
            regexp_replace(name, '[^a-zA-Z0-9\s-]', '', 'g'), 
            '\s+', '-', 'g'
        ), 
        '^-+|-+$', '', 'g'
    )
);

-- 5. Handle duplicate codes by appending id
UPDATE projects p1
SET code = code || '-' || id
WHERE EXISTS (
    SELECT 1 FROM projects p2 
    WHERE p2.code = p1.code AND p2.id < p1.id
);

-- 6. Set type based on whether account_id exists
UPDATE projects SET type = CASE 
    WHEN account_id IS NULL THEN 'INTERNAL' 
    ELSE 'CLIENT' 
END;

-- 7. Update status values to new standard (PLANNED, ACTIVE, PAUSED, DONE, ARCHIVED)
-- Map old values to new ones
UPDATE projects SET status = CASE 
    WHEN status = 'DISCOVERY' THEN 'PLANNED'
    WHEN status = 'PLANNING' THEN 'PLANNED'
    WHEN status = 'IN_PROGRESS' THEN 'ACTIVE'
    WHEN status = 'COMPLETED' THEN 'DONE'
    ELSE status
END;

-- 8. Add constraints
ALTER TABLE projects ALTER COLUMN code SET NOT NULL;
ALTER TABLE projects ADD CONSTRAINT uk_projects_code UNIQUE (code);
ALTER TABLE projects ADD CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
-- Note: FK constraint for github_repo_id will be added in V6 after github_repos table is created

-- 9. Add check constraint for type-account_id relationship
ALTER TABLE projects ADD CONSTRAINT chk_projects_type_account 
    CHECK (
        (type = 'CLIENT' AND account_id IS NOT NULL) OR 
        (type = 'INTERNAL' AND account_id IS NULL)
    );

-- 10. Update indexes
DROP INDEX IF EXISTS idx_projects_client_id;
CREATE INDEX idx_projects_account_id ON projects(account_id);
CREATE INDEX idx_projects_type ON projects(type);
CREATE INDEX idx_projects_code ON projects(code);
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_github_repo_id ON projects(github_repo_id);

-- 11. Create GIN index on custom_fields for efficient JSONB queries
CREATE INDEX idx_projects_custom_fields ON projects USING GIN (custom_fields);

-- 12. Add comments for documentation
COMMENT ON COLUMN projects.type IS 'Project type: CLIENT (associated with account) or INTERNAL (company internal project)';
COMMENT ON COLUMN projects.code IS 'Unique project code used for GitHub repository naming (slug format)';
COMMENT ON COLUMN projects.custom_fields IS 'Flexible JSONB field for client-specific or project-specific custom data';
COMMENT ON COLUMN projects.team IS 'JSON array of team member identifiers (user IDs or emails)';
COMMENT ON COLUMN projects.links IS 'JSON array of related links: [{label: string, url: string}]';
