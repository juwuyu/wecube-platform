package com.webank.wecube.platform.core.support.authserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wecube.core.authserver")
public class AuthServerRestClientProperties {
    private String host = "localhost";
    private String httpScheme = "http";
    private int port = 9090;

    private String pathRegisterLocalUser = "/auth/v1/users";
    private String pathRetrieveAllUserAccounts = "/auth/v1/users";
    private String pathDeleteUserAccountByUserId = "/auth/v1/users/{user-id}";
    private String pathRetrieveGrantedRolesByUsername = "/auth/v1/users/{username}/roles";
    private String pathRetrieveRoleById = "/auth/v1/roles/{role-id}";
    private String pathRetrieveRoleByName = "/auth/v1/roles/name/{role-name}";
    private String pathRegisterLocalRole = "/auth/v1/roles";
    private String pathUpdateLocalRole = "/auth/v1/roles/update";
    private String pathRetrieveAllRoles = "/auth/v1/roles";
    private String pathDeleteLocalRoleByRoleId = "/auth/v1/roles/{role-id}";
    private String pathRetrieveAllUsersBelongsToRoleId = "/auth/v1/roles/{role-id}/users";
    private String pathConfigureRoleForUsers = "/auth/v1/roles/{role-id}/users";
    private String pathRevokeRoleFromUsers = "/auth/v1/roles/{role-id}/users/revoke";

    private String pathConfigureRolesForUser = "/auth/v1/users/{user-id}/roles";
    private String pathRevokeRolesFromUser = "/auth/v1/users/{user-id}/roles/revoke";

    private String pathRevokeAuthoritiesFromRole = "/auth/v1/roles/{role-id}/authorities/revoke";
    private String pathConfigureRoleAuthorities = "/auth/v1/roles/{role-id}/authorities";
    private String pathConfigureRoleAuthoritiesWithRoleName = "/auth/v1/roles/authorities-grant";
    private String pathRevokeRoleAuthoritiesWithRoleName = "/auth/v1/roles/authorities-revocation";

    private String pathUserChangePassword = "/auth/v1/users/change-password";
    private String pathUserResetPassword = "/auth/v1/users/reset-password";
    private String pathGetUserByUserId = "/auth/v1/users/{user-id}";

    private String pathHealthCheck = "/auth/v1/health-check";

    private String pathRegisterSubSystem = "/auth/v1/sub-systems";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHttpScheme() {
        return httpScheme;
    }

    public void setHttpScheme(String httpScheme) {
        this.httpScheme = httpScheme;
    }

    public String getPathRegisterLocalUser() {
        return pathRegisterLocalUser;
    }

    public void setPathRegisterLocalUser(String pathRegisterLocalUser) {
        this.pathRegisterLocalUser = pathRegisterLocalUser;
    }

    public String getPathRetrieveAllUserAccounts() {
        return pathRetrieveAllUserAccounts;
    }

    public void setPathRetrieveAllUserAccounts(String pathRetrieveAllUserAccounts) {
        this.pathRetrieveAllUserAccounts = pathRetrieveAllUserAccounts;
    }

    public String getPathDeleteUserAccountByUserId() {
        return pathDeleteUserAccountByUserId;
    }

    public void setPathDeleteUserAccountByUserId(String pathDeleteUserAccountByUserId) {
        this.pathDeleteUserAccountByUserId = pathDeleteUserAccountByUserId;
    }

    public String getPathRetrieveGrantedRolesByUsername() {
        return pathRetrieveGrantedRolesByUsername;
    }

    public void setPathRetrieveGrantedRolesByUsername(String pathRetrieveGrantedRolesByUsername) {
        this.pathRetrieveGrantedRolesByUsername = pathRetrieveGrantedRolesByUsername;
    }

    public String getPathRetrieveRoleById() {
        return pathRetrieveRoleById;
    }

    public void setPathRetrieveRoleById(String pathRetrieveRoleById) {
        this.pathRetrieveRoleById = pathRetrieveRoleById;
    }

    public String getPathRegisterLocalRole() {
        return pathRegisterLocalRole;
    }

    public void setPathRegisterLocalRole(String pathRegisterLocalRole) {
        this.pathRegisterLocalRole = pathRegisterLocalRole;
    }

    public String getPathRetrieveAllRoles() {
        return pathRetrieveAllRoles;
    }

    public void setPathRetrieveAllRoles(String pathRetrieveAllRoles) {
        this.pathRetrieveAllRoles = pathRetrieveAllRoles;
    }

    public String getPathDeleteLocalRoleByRoleId() {
        return pathDeleteLocalRoleByRoleId;
    }

    public void setPathDeleteLocalRoleByRoleId(String pathDeleteLocalRoleByRoleId) {
        this.pathDeleteLocalRoleByRoleId = pathDeleteLocalRoleByRoleId;
    }

    public String getPathRetrieveAllUsersBelongsToRoleId() {
        return pathRetrieveAllUsersBelongsToRoleId;
    }

    public void setPathRetrieveAllUsersBelongsToRoleId(String pathRetrieveAllUsersBelongsToRoleId) {
        this.pathRetrieveAllUsersBelongsToRoleId = pathRetrieveAllUsersBelongsToRoleId;
    }

    public String getPathConfigureRoleForUsers() {
        return pathConfigureRoleForUsers;
    }

    public void setPathConfigureRoleForUsers(String pathConfigureRoleForUsers) {
        this.pathConfigureRoleForUsers = pathConfigureRoleForUsers;
    }

    public String getPathRevokeRoleFromUsers() {
        return pathRevokeRoleFromUsers;
    }

    public void setPathRevokeRoleFromUsers(String pathRevokeRoleFromUsers) {
        this.pathRevokeRoleFromUsers = pathRevokeRoleFromUsers;
    }

    public String getPathRevokeAuthoritiesFromRole() {
        return pathRevokeAuthoritiesFromRole;
    }

    public void setPathRevokeAuthoritiesFromRole(String pathRevokeAuthoritiesFromRole) {
        this.pathRevokeAuthoritiesFromRole = pathRevokeAuthoritiesFromRole;
    }

    public String getPathConfigureRoleAuthorities() {
        return pathConfigureRoleAuthorities;
    }

    public void setPathConfigureRoleAuthorities(String pathConfigureRoleAuthorities) {
        this.pathConfigureRoleAuthorities = pathConfigureRoleAuthorities;
    }

    public String getPathConfigureRoleAuthoritiesWithRoleName() {
        return pathConfigureRoleAuthoritiesWithRoleName;
    }

    public void setPathConfigureRoleAuthoritiesWithRoleName(String pathConfigureRoleAuthoritiesWithRoleName) {
        this.pathConfigureRoleAuthoritiesWithRoleName = pathConfigureRoleAuthoritiesWithRoleName;
    }

    public String getPathRevokeRoleAuthoritiesWithRoleName() {
        return pathRevokeRoleAuthoritiesWithRoleName;
    }

    public void setPathRevokeRoleAuthoritiesWithRoleName(String pathRevokeRoleAuthoritiesWithRoleName) {
        this.pathRevokeRoleAuthoritiesWithRoleName = pathRevokeRoleAuthoritiesWithRoleName;
    }

    public String getPathHealthCheck() {
        return pathHealthCheck;
    }

    public void setPathHealthCheck(String pathHealthCheck) {
        this.pathHealthCheck = pathHealthCheck;
    }

    public String getPathRetrieveRoleByName() {
        return pathRetrieveRoleByName;
    }

    public void setPathRetrieveRoleByName(String pathRetrieveRoleByName) {
        this.pathRetrieveRoleByName = pathRetrieveRoleByName;
    }

    public String getPathUserChangePassword() {
        return pathUserChangePassword;
    }

    public void setPathUserChangePassword(String pathUserChangePassword) {
        this.pathUserChangePassword = pathUserChangePassword;
    }

    public String getPathGetUserByUserId() {
        return pathGetUserByUserId;
    }

    public void setPathGetUserByUserId(String pathGetUserByUserId) {
        this.pathGetUserByUserId = pathGetUserByUserId;
    }

    public String getPathUserResetPassword() {
        return pathUserResetPassword;
    }

    public void setPathUserResetPassword(String pathUserResetPassword) {
        this.pathUserResetPassword = pathUserResetPassword;
    }

    public String getPathRegisterSubSystem() {
        return pathRegisterSubSystem;
    }

    public void setPathRegisterSubSystem(String pathRegisterSubSystem) {
        this.pathRegisterSubSystem = pathRegisterSubSystem;
    }

    public String getPathConfigureRolesForUser() {
        return pathConfigureRolesForUser;
    }

    public void setPathConfigureRolesForUser(String pathConfigureRolesForUser) {
        this.pathConfigureRolesForUser = pathConfigureRolesForUser;
    }

    public String getPathRevokeRolesFromUser() {
        return pathRevokeRolesFromUser;
    }

    public void setPathRevokeRolesFromUser(String pathRevokeRolesFromUser) {
        this.pathRevokeRolesFromUser = pathRevokeRolesFromUser;
    }

    public String getPathUpdateLocalRole() {
        return pathUpdateLocalRole;
    }

    public void setPathUpdateLocalRole(String pathUpdateLocalRole) {
        this.pathUpdateLocalRole = pathUpdateLocalRole;
    }

    
}
