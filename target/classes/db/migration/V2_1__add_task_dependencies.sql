-- Create the task_dependencies table for advanced dependency management
CREATE TABLE task_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dependent_task_id BIGINT NOT NULL,
    prerequisite_task_id BIGINT NOT NULL,
    dependency_type VARCHAR(50) NOT NULL DEFAULT 'FINISH_TO_START',
    lag_hours INTEGER DEFAULT 0,
    is_critical_path BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    project_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_task_dependencies_unique UNIQUE (dependent_task_id, prerequisite_task_id),
    CONSTRAINT chk_task_dependencies_different_tasks CHECK (dependent_task_id != prerequisite_task_id),
    CONSTRAINT fk_task_dependencies_dependent FOREIGN KEY (dependent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_dependencies_prerequisite FOREIGN KEY (prerequisite_task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_task_dependencies_dependent ON task_dependencies(dependent_task_id);
CREATE INDEX idx_task_dependencies_prerequisite ON task_dependencies(prerequisite_task_id);
CREATE INDEX idx_task_dependencies_project ON task_dependencies(project_id, is_active);