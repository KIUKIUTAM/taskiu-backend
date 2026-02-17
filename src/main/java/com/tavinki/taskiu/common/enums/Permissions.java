package com.tavinki.taskiu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Permissions {
    
    // ==================== Project Management ====================
    PROJECT_CREATE("project:create", "Create Project"),
    PROJECT_VIEW("project:view", "View Project"),
    PROJECT_EDIT("project:edit", "Edit Project"),
    PROJECT_DELETE("project:delete", "Delete Project"),
    PROJECT_ARCHIVE("project:archive", "Archive Project"),
    PROJECT_RESTORE("project:restore", "Restore Project"),
    
    // ==================== Task Management ====================
    TASK_CREATE("task:create", "Create Task"),
    TASK_VIEW("task:view", "View Task"),
    TASK_VIEW_ASSIGNED("task:view:assigned", "View Assigned Tasks"),
    TASK_EDIT("task:edit", "Edit Task"),
    TASK_EDIT_ASSIGNED("task:edit:assigned", "Edit Assigned Tasks"),
    TASK_DELETE("task:delete", "Delete Task"),
    TASK_ASSIGN("task:assign", "Assign Task"),
    TASK_UPDATE_STATUS("task:update:status", "Update Task Status"),
    TASK_SET_PRIORITY("task:set:priority", "Set Task Priority"),
    TASK_SET_DEADLINE("task:set:deadline", "Set Task Deadline"),
    
    // ==================== Team Member Management ====================
    MEMBER_INVITE("member:invite", "Invite Member"),
    MEMBER_REMOVE("member:remove", "Remove Member"),
    MEMBER_VIEW("member:view", "View Member"),
    MEMBER_EDIT_ROLE("member:edit:role", "Edit Member Role"),
    MEMBER_VIEW_WORKLOAD("member:view:workload", "View Member Workload"),
    
    // ==================== Role & Permission ====================
    ROLE_CREATE("role:create", "Create Role"),
    ROLE_EDIT("role:edit", "Edit Role"),
    ROLE_DELETE("role:delete", "Delete Role"),
    ROLE_ASSIGN("role:assign", "Assign Role"),
    PERMISSION_MANAGE("permission:manage", "Manage Permission"),
    
    // ==================== Milestone ====================
    MILESTONE_CREATE("milestone:create", "Create Milestone"),
    MILESTONE_VIEW("milestone:view", "View Milestone"),
    MILESTONE_EDIT("milestone:edit", "Edit Milestone"),
    MILESTONE_DELETE("milestone:delete", "Delete Milestone"),
    
    // ==================== Comment & Discussion ====================
    COMMENT_CREATE("comment:create", "Create Comment"),
    COMMENT_VIEW("comment:view", "View Comment"),
    COMMENT_EDIT_OWN("comment:edit:own", "Edit Own Comment"),
    COMMENT_EDIT_ALL("comment:edit:all", "Edit All Comments"),
    COMMENT_DELETE_OWN("comment:delete:own", "Delete Own Comment"),
    COMMENT_DELETE_ALL("comment:delete:all", "Delete All Comments"),
    COMMENT_MODERATE("comment:moderate", "Moderate Comments"),
    
    // ==================== File Management ====================
    FILE_UPLOAD("file:upload", "Upload File"),
    FILE_DOWNLOAD("file:download", "Download File"),
    FILE_VIEW("file:view", "View File"),
    FILE_DELETE("file:delete", "Delete File"),
    
    // ==================== Report & Analytics ====================
    REPORT_VIEW("report:view", "View Report"),
    REPORT_EXPORT("report:export", "Export Report"),
    REPORT_CREATE_CUSTOM("report:create:custom", "Create Custom Report"),
    ANALYTICS_VIEW("analytics:view", "View Analytics"),
    
    // ==================== Time Management ====================
    TIMESHEET_CREATE("timesheet:create", "Create Timesheet"),
    TIMESHEET_VIEW_OWN("timesheet:view:own", "View Own Timesheet"),
    TIMESHEET_VIEW_ALL("timesheet:view:all", "View All Timesheets"),
    TIMESHEET_EDIT("timesheet:edit", "Edit Timesheet"),
    TIMESHEET_APPROVE("timesheet:approve", "Approve Timesheet"),
    
    // ==================== Notification ====================
    NOTIFICATION_MANAGE("notification:manage", "Manage Notification"),
    NOTIFICATION_SEND("notification:send", "Send Notification"),
    
    // ==================== System Settings ====================
    SETTINGS_VIEW("settings:view", "View Settings"),
    SETTINGS_EDIT("settings:edit", "Edit Settings"),
    SETTINGS_MANAGE_INTEGRATIONS("settings:manage:integrations", "Manage Integrations");
    
    private final String permission;
    private final String description;
    
    public static Permissions fromString(String permission) {
        for (Permissions p : Permissions.values()) {
            if (p.permission.equals(permission)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + permission);
    }
}
