package de.filefighter.rest.domain.user.role;

public enum Role {
    FAMILY(0, "Family"),
    ADMIN(1, "Admin");

    private final long roleId;
    private final String displayName;

    Role(long roleId, String displayName) {
        this.roleId = roleId;
        this.displayName = displayName;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
