package com.tavinki.taskiu.modules.project.entity;


import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tavinki.taskiu.common.enums.role.ProjectRole;
import com.tavinki.taskiu.modules.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "project_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_project_members_project_user",
            columnNames = {"project_id", "user_id"}
        )
    }
)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_project_members_project")
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_project_members_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
