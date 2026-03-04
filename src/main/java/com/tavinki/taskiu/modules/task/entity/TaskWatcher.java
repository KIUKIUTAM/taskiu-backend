package com.tavinki.taskiu.modules.task.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_watchers")
@SQLDelete(sql = "UPDATE task_watchers SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskWatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Builder.Default
    @Column(nullable = false)
    private boolean archived = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public TaskWatcher(Task task, String userId) {
        this.task = task;
        this.userId = userId;
    }
}
