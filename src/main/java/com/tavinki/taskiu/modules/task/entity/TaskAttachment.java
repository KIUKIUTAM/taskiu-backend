package com.tavinki.taskiu.modules.task.entity;
import java.time.LocalDateTime;

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

@Getter
@Setter
@Builder

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_attachments")
@SQLDelete(sql = "UPDATE task_attachments SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // 上傳者
    @Column(name = "uploader_id", nullable = false)
    private String uploaderId;

    
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column
    private String mimeType;

    // 檔案大小 (bytes)
    @Column
    private Long fileSize;

    @Builder.Default
    @Column(nullable = false)
    private boolean archived = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
