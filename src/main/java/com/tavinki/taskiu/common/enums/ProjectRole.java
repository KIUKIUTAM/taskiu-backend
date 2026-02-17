package com.tavinki.taskiu.common.enums;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.Collections;
import static com.tavinki.taskiu.common.enums.Permissions.*;

@Getter
@AllArgsConstructor
public enum ProjectRole {
    
    ADMIN("Admin", Set.of(
        // All permissions
        PROJECT_CREATE, PROJECT_VIEW, PROJECT_EDIT, PROJECT_DELETE, PROJECT_ARCHIVE, PROJECT_RESTORE,
        TASK_CREATE, TASK_VIEW, TASK_EDIT, TASK_DELETE, TASK_ASSIGN, TASK_UPDATE_STATUS, TASK_SET_PRIORITY, TASK_SET_DEADLINE,
        MEMBER_INVITE, MEMBER_REMOVE, MEMBER_VIEW, MEMBER_EDIT_ROLE, MEMBER_VIEW_WORKLOAD,
        ROLE_CREATE, ROLE_EDIT, ROLE_DELETE, ROLE_ASSIGN, PERMISSION_MANAGE,
        MILESTONE_CREATE, MILESTONE_VIEW, MILESTONE_EDIT, MILESTONE_DELETE,
        COMMENT_CREATE, COMMENT_VIEW, COMMENT_EDIT_OWN, COMMENT_EDIT_ALL, COMMENT_DELETE_OWN, COMMENT_DELETE_ALL, COMMENT_MODERATE,
        FILE_UPLOAD, FILE_DOWNLOAD, FILE_VIEW, FILE_DELETE,
        REPORT_VIEW, REPORT_EXPORT, REPORT_CREATE_CUSTOM, ANALYTICS_VIEW,
        TIMESHEET_CREATE, TIMESHEET_VIEW_OWN, TIMESHEET_VIEW_ALL, TIMESHEET_EDIT, TIMESHEET_APPROVE,
        NOTIFICATION_MANAGE, NOTIFICATION_SEND,
        SETTINGS_VIEW, SETTINGS_EDIT, SETTINGS_MANAGE_INTEGRATIONS
    )),
    
    PROJECT_MANAGER("Project Manager", Set.of(
        // Project: All permissions
        PROJECT_CREATE, PROJECT_VIEW, PROJECT_EDIT, PROJECT_DELETE, PROJECT_ARCHIVE, PROJECT_RESTORE,
        // Task: All permissions
        TASK_CREATE, TASK_VIEW, TASK_EDIT, TASK_DELETE, TASK_ASSIGN, TASK_UPDATE_STATUS, TASK_SET_PRIORITY, TASK_SET_DEADLINE,
        // Member: View, Assign Role
        MEMBER_VIEW, MEMBER_EDIT_ROLE, MEMBER_VIEW_WORKLOAD,
        // Milestone: All permissions
        MILESTONE_CREATE, MILESTONE_VIEW, MILESTONE_EDIT, MILESTONE_DELETE,
        // Comment: All permissions
        COMMENT_CREATE, COMMENT_VIEW, COMMENT_EDIT_OWN, COMMENT_EDIT_ALL, COMMENT_DELETE_OWN, COMMENT_DELETE_ALL, COMMENT_MODERATE,
        // File: All permissions
        FILE_UPLOAD, FILE_DOWNLOAD, FILE_VIEW, FILE_DELETE,
        // Report: View, Export
        REPORT_VIEW, REPORT_EXPORT, ANALYTICS_VIEW,
        // Timesheet: All permissions
        TIMESHEET_CREATE, TIMESHEET_VIEW_OWN, TIMESHEET_VIEW_ALL, TIMESHEET_EDIT, TIMESHEET_APPROVE,
        // Notification
        NOTIFICATION_MANAGE, NOTIFICATION_SEND
    )),
    
    TEAM_MEMBER("Team Member", Set.of(
        // Project: View
        PROJECT_VIEW,
        // Task: View, Edit (assigned), Update Status
        TASK_VIEW, TASK_VIEW_ASSIGNED, TASK_EDIT_ASSIGNED, TASK_UPDATE_STATUS,
        // Member: View
        MEMBER_VIEW,
        // Milestone: View
        MILESTONE_VIEW,
        // Comment: Create, View, Edit Own
        COMMENT_CREATE, COMMENT_VIEW, COMMENT_EDIT_OWN, COMMENT_DELETE_OWN,
        // File: Upload, Download, View
        FILE_UPLOAD, FILE_DOWNLOAD, FILE_VIEW,
        // Timesheet: Create, View Own
        TIMESHEET_CREATE, TIMESHEET_VIEW_OWN
    )),
    
    GUEST("Guest/Viewer", Set.of(
        // Project: View
        PROJECT_VIEW,
        // Task: View
        TASK_VIEW,
        // Milestone: View
        MILESTONE_VIEW,
        // Comment: View
        COMMENT_VIEW,
        // File: View, Download
        FILE_VIEW, FILE_DOWNLOAD
    ));
    
    private final String displayName;
    private final Set<Permissions> permissions;
    
    public Set<Permissions> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    public boolean hasPermission(Permissions permission) {
        return permissions.contains(permission);
    }
}
