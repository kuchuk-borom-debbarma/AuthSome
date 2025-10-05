package dev.kuku.auth_some.core_service.access_control.api;

import dev.kuku.auth_some.core_service.access_control.api.dto.ActionToFetch;

import java.util.List;

public interface AccessControl {
    void createResources(String projectId, List<String> resources);

    void createActions(String projectId, List<String> actions);

    void createRole(String proejctId, String roleName, List<String> inheritsFromRoleId, List<ActionToFetch.ActionAndResource> actionAndResources);

    void addRolesForProjectUser(String projectId, String userId, List<String> roleIds);

    boolean canAccess(String projectId, String userId, String actionId, String resourceId);
}
