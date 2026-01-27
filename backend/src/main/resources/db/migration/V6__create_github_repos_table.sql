-- V6: Create github_repos table for GitHub integration metadata
-- This migration creates a table to store GitHub repository information linked to projects

-- 1. Create github_repos table
CREATE TABLE github_repos (
    id BIGSERIAL PRIMARY KEY,
    repo_full_name VARCHAR(255) NOT NULL UNIQUE,
    repo_url VARCHAR(500) NOT NULL,
    default_branch VARCHAR(100) DEFAULT 'main',
    status VARCHAR(50) NOT NULL,
    provisioning_error TEXT,
    provisioned_at TIMESTAMP,
    last_sync_at TIMESTAMP,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure repo_full_name follows 'owner/repo' format
    CONSTRAINT chk_github_repos_full_name CHECK (repo_full_name ~ '^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$')
);

-- 2. Create indexes for performance
CREATE INDEX idx_github_repos_status ON github_repos(status);
CREATE INDEX idx_github_repos_full_name ON github_repos(repo_full_name);
CREATE INDEX idx_github_repos_provisioned_at ON github_repos(provisioned_at);

-- 3. Create GIN index on metadata for efficient JSONB queries
CREATE INDEX idx_github_repos_metadata ON github_repos USING GIN (metadata);

-- 4. Add comments for documentation
COMMENT ON TABLE github_repos IS 'GitHub repository metadata for projects. Tracks provisioning status and sync information.';
COMMENT ON COLUMN github_repos.repo_full_name IS 'Full repository name in format owner/repo (e.g., myorg/project-name)';
COMMENT ON COLUMN github_repos.status IS 'Repository status: PROVISIONING, ACTIVE, FAILED, ARCHIVED';
COMMENT ON COLUMN github_repos.provisioning_error IS 'Error message if repository provisioning failed';
COMMENT ON COLUMN github_repos.provisioned_at IS 'Timestamp when repository was successfully created';
COMMENT ON COLUMN github_repos.last_sync_at IS 'Timestamp of last metadata synchronization from GitHub';
COMMENT ON COLUMN github_repos.metadata IS 'Additional GitHub metadata: stars, forks, language, topics, etc.';

-- 5. Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_github_repos_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_github_repos_updated_at
    BEFORE UPDATE ON github_repos
    FOR EACH ROW
    EXECUTE FUNCTION update_github_repos_updated_at();

-- 6. Add FK constraint from projects to github_repos (now that table exists)
ALTER TABLE projects ADD CONSTRAINT fk_projects_github_repo 
    FOREIGN KEY (github_repo_id) REFERENCES github_repos(id) ON DELETE SET NULL;
