// src/test/java/org/frcpm/repositories/test/TaskRepositoryTestDouble.java

package org.frcpm.repositories.test;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of TaskRepository for testing.
 * Provides predictable behavior without database dependencies.
 */
public class TaskRepositoryTestDouble implements TaskRepository {
    
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private long nextId = 1;
    
    /**
     * Clears all tasks from the repository.
     */
    public void clear() {
        tasks.clear();
        nextId = 1;
    }
    
    /**
     * Pre-populates the repository with a set of tasks.
     * 
     * @param initialTasks the tasks to add
     */
    public void initialize(Collection<Task> initialTasks) {
        this.clear();
        for (Task task : initialTasks) {
            if (task.getId() == null) {
                task.setId(nextId++);
            } else {
                nextId = Math.max(nextId, task.getId() + 1);
            }
            tasks.put(task.getId(), task);
        }
    }
    
    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }
    
    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }
    
    @Override
    public Task save(Task entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        tasks.put(entity.getId(), entity);
        return entity;
    }
    
    @Override
    public void delete(Task entity) {
        if (entity != null && entity.getId() != null) {
            tasks.remove(entity.getId());
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        return tasks.remove(id) != null;
    }
    
    @Override
    public long count() {
        return tasks.size();
    }
    
    @Override
    public List<Task> findByProject(Project project) {
        if (project == null) {
            return List.of();
        }
        return tasks.values().stream()
                .filter(task -> Objects.equals(task.getProject().getId(), project.getId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findBySubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            return List.of();
        }
        return tasks.values().stream()
                .filter(task -> Objects.equals(task.getSubsystem().getId(), subsystem.getId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByAssignedMember(TeamMember member) {
        if (member == null) {
            return List.of();
        }
        return tasks.values().stream()
                .filter(task -> task.getAssignedTo().stream()
                        .anyMatch(m -> Objects.equals(m.getId(), member.getId())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByCompleted(boolean completed) {
        return tasks.values().stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByEndDateBefore(LocalDate date) {
        if (date == null) {
            return List.of();
        }
        return tasks.values().stream()
                .filter(task -> task.getEndDate() != null && task.getEndDate().isBefore(date))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByPriority(Task.Priority priority) {
        if (priority == null) {
            return List.of();
        }
        return tasks.values().stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }
}