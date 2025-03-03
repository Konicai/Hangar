package io.papermc.hangar.service.internal.users.invites;

import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.HangarProjectInvite;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService.ProjectNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectInviteService extends InviteService<ProjectContext, ProjectRole, ProjectRoleTable, ProjectTable> {

    private final ProjectService projectService;
    private final ProjectsDAO projectsDAO;
    private final ProjectFiles projectFiles;

    @Autowired
    public ProjectInviteService(ProjectRoleService roleService, ProjectMemberService memberService, ProjectNotificationService projectNotificationService, final ProjectService projectService, final ProjectsDAO projectsDAO, final ProjectFiles projectFiles) {
        super(roleService, memberService, projectNotificationService, "project.settings.error.members.");
        this.projectService = projectService;
        this.projectsDAO = projectsDAO;
        this.projectFiles = projectFiles;
    }

    public List<HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.getProjectInvites(getHangarPrincipal().getId());
    }

    @Override
    protected ProjectRole getOwnerRole() {
        return ProjectRole.PROJECT_OWNER;
    }

    @Override
    protected ProjectRole getAdminRole() {
        return ProjectRole.PROJECT_ADMIN;
    }

    @Override
    LogAction<ProjectContext> getInviteSentAction() {
        return LogAction.PROJECT_INVITES_SENT;
    }

    @Override
    public ProjectTable getJoinable(final long id) {
        return projectService.getProjectTable(id);
    }

    @Override
    protected void updateOwnerId(final ProjectTable project, final UserTable newOwner) {
        final String oldOwnerName = project.getOwnerName();
        project.setOwnerId(newOwner.getUserId());
        project.setOwnerName(newOwner.getName());
        projectsDAO.updateOwner(project);
        projectFiles.transferProject(oldOwnerName, newOwner.getName(), project.getSlug());
        projectService.refreshHomeProjects();
    }

    @Override
    LogAction<ProjectContext> getInviteAcceptAction() {
        return LogAction.PROJECT_MEMBER_ADDED;
    }

    @Override
    LogAction<ProjectContext> getInviteDeclineAction() {
        return LogAction.PROJECT_INVITE_DECLINED;
    }
}
