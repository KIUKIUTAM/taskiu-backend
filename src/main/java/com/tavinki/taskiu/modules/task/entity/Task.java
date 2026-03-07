package com.tavinki.taskiu.modules.task.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.tavinki.taskiu.common.enums.task.TaskPriority;
import com.tavinki.taskiu.common.enums.task.TaskStatus;
import com.tavinki.taskiu.common.enums.task.TaskType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    // ==================== 1. Identification and Basic Info ====================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String id;

    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    // ==================== 2. Status and Workflow ====================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    // 0 ~ 100
    @Builder.Default
    @Column(nullable = false)
    @Min(0) @Max(100)
    private Integer progress = 0;

    // soft delete
    @Builder.Default
    @Column(nullable = false)
    private boolean archived = false;

    // ==================== 3. Personnel Assignment ====================

    @Column(name = "assignee_id")
    private String assigneeId;

    @Column(name = "creator_id", nullable = false, updatable = false)
    private String creatorId;

    // ==================== 4. Time Management ====================

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant dueDate;

    @Column
    private Instant startDate;

    @Column
    private Instant completedAt;

    // ==================== 5. Classification and Relations ====================

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id")
    )
    @Builder.Default
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "parent_id")
    private String parentId;

    // ==================== 6. Attachments and Comments ====================

    @Builder.Default
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskAttachment> attachments = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskComment> comments = new ArrayList<>();

    // ==================== Helper Methods ====================

    // Mark as completed
    public void markAsCompleted() {
        this.status = TaskStatus.DONE;
        this.progress = 100;
        this.completedAt = Instant.now();
    }

    // Add comment
    public void addComment(String userId, String content) {
        TaskComment comment = new TaskComment(this, userId, content);
        this.comments.add(comment);
    }
}
