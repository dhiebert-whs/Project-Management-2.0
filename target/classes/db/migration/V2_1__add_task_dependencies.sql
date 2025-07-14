-- src/main/resources/db/migration/V2_1__add_task_dependencies.sql
-- 
-- Database migration for Phase 2E-D: Advanced Task Management
-- Creates task_dependencies table for TaskDependency entity
-- 
-- Version: 2.1
-- Phase: 2E-D - Advanced Task Dependencies
-- Author: FRC Project Management Team
-- Date: January 2025

-- Create the task_dependencies table
CREATE TABLE task_dependencies (
    -- Primary key
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Core relationship fields
    dependent_task_id BIGINT NOT NULL COMMENT 'Task that depends on another (successor)',
    prerequisite_task_id BIGINT NOT NULL COMMENT 'Task that must be completed first (predecessor)',
    
    -- Dependency configuration
    dependency_type VARCHAR(50) NOT NULL DEFAULT 'FINISH_TO_START' COMMENT 'Type of dependency relationship',
    lag_hours INTEGER DEFAULT 0 COMMENT 'Lag time in hours (positive = delay, negative = lead)',
    
    -- Analysis and scheduling fields
    is_critical_path BOOLEAN DEFAULT FALSE COMMENT 'Whether this dependency is on the critical path',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether this dependency is currently active',
    
    -- Documentation and metadata
    notes TEXT COMMENT 'Optional notes explaining the dependency',
    project_id BIGINT COMMENT 'Project both tasks belong to (for validation)',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When dependency was created',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When dependency was last modified',
    
    -- Constraints
    CONSTRAINT uk_task_dependencies_unique UNIQUE (dependent_task_id, prerequisite_task_id) COMMENT 'Prevent duplicate dependencies',
    CONSTRAINT chk_task_dependencies_different_tasks CHECK (dependent_task_id != prerequisite_task_id) COMMENT 'Prevent self-dependencies',
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH', 'BLOCKING', 'SOFT')) COMMENT 'Valid dependency types'
);

-- Add foreign key constraints
ALTER TABLE task_dependencies 
ADD CONSTRAINT fk_task_dependencies_dependent 
FOREIGN KEY (dependent_task_id) REFERENCES tasks(id) 
ON DELETE CASCADE 
COMMENT 'Link to dependent task';

ALTER TABLE task_dependencies 
ADD CONSTRAINT fk_task_dependencies_prerequisite 
FOREIGN KEY (prerequisite_task_id) REFERENCES tasks(id) 
ON DELETE CASCADE 
COMMENT 'Link to prerequisite task';

ALTER TABLE task_dependencies 
ADD CONSTRAINT fk_task_dependencies_project 
FOREIGN KEY (project_id) REFERENCES projects(id) 
ON DELETE CASCADE 
COMMENT 'Link to project for validation';

-- Create performance indexes
-- Primary lookup indexes for dependency traversal
CREATE INDEX idx_task_dependencies_dependent ON task_dependencies(dependent_task_id) COMMENT 'Fast lookup of task dependencies';
CREATE INDEX idx_task_dependencies_prerequisite ON task_dependencies(prerequisite_task_id) COMMENT 'Fast lookup of blocking tasks';

-- Project-scoped queries for dashboard and reporting
CREATE INDEX idx_task_dependencies_project ON task_dependencies(project_id, is_active) COMMENT 'Project-scoped dependency queries';

-- Critical path analysis
CREATE INDEX idx_task_dependencies_critical_path ON task_dependencies(project_id, is_critical_path, is_active) COMMENT 'Critical path analysis queries';

-- Dependency type filtering
CREATE INDEX idx_task_dependencies_type ON task_dependencies(dependency_type, is_active) COMMENT 'Filter by dependency type';

-- Lag time analysis (for scheduling optimization)
CREATE INDEX idx_task_dependencies_lag ON task_dependencies(project_id, lag_hours) WHERE lag_hours IS NOT NULL AND lag_hours != 0 COMMENT 'Tasks with lag time for scheduling';

-- Composite index for advanced queries
CREATE INDEX idx_task_dependencies_advanced ON task_dependencies(project_id, dependency_type, is_active, is_critical_path) COMMENT 'Multi-criteria dependency queries';

-- Insert sample data for testing (optional - remove for production)
-- This data will help verify the migration worked correctly
INSERT INTO task_dependencies (dependent_task_id, prerequisite_task_id, dependency_type, notes, project_id, is_active) 
SELECT 
    t1.id as dependent_task_id,
    t2.id as prerequisite_task_id,
    'FINISH_TO_START' as dependency_type,
    'Sample dependency created by migration' as notes,
    t1.project_id as project_id,
    true as is_active
FROM tasks t1, tasks t2 
WHERE t1.id != t2.id 
  AND t1.project_id = t2.project_id
  AND t1.title LIKE '%Frame%' 
  AND t2.title LIKE '%Design%'
  AND NOT EXISTS (
      SELECT 1 FROM task_dependencies td 
      WHERE td.dependent_task_id = t1.id 
        AND td.prerequisite_task_id = t2.id
  )
LIMIT 3;

-- Verify the migration by checking table structure
SELECT 
    'task_dependencies table created successfully' as migration_status,
    COUNT(*) as sample_dependencies_created
FROM task_dependencies;

-- Create a view for easy dependency analysis (optional but helpful)
CREATE VIEW v_task_dependency_summary AS
SELECT 
    td.id,
    td.dependency_type,
    td.lag_hours,
    td.is_critical_path,
    td.is_active,
    p.name as project_name,
    dt.title as dependent_task_title,
    pt.title as prerequisite_task_title,
    dt.completed as dependent_completed,
    pt.completed as prerequisite_completed,
    CASE 
        WHEN td.dependency_type IN ('FINISH_TO_START', 'BLOCKING') AND pt.completed = false THEN 'BLOCKED'
        WHEN td.dependency_type = 'START_TO_START' AND pt.progress = 0 THEN 'BLOCKED'
        ELSE 'SATISFIED'
    END as dependency_status,
    td.created_at,
    td.updated_at
FROM task_dependencies td
JOIN tasks dt ON td.dependent_task_id = dt.id
JOIN tasks pt ON td.prerequisite_task_id = pt.id
LEFT JOIN projects p ON td.project_id = p.id
WHERE td.is_active = true;

-- Add helpful comments for future developers
COMMENT ON TABLE task_dependencies IS 'Advanced task dependency relationships for project scheduling and critical path analysis. Supports multiple dependency types with lag time for complex build season workflows.';

-- Migration completion message
SELECT 
    'Migration V2_1__add_task_dependencies.sql completed successfully' as status,
    'TaskDependency entity can now be used for advanced task management' as next_step,
    'Run TaskRepositoryTest to verify enhanced queries work correctly' as testing;