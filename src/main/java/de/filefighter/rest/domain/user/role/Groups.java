package de.filefighter.rest.domain.user.role;

public enum Groups {
    UNDEFINED(-1, "No group"),
    FAMILY(0, "Family"),
    ADMIN(1, "Admin");

    private final long groupId;
    private final String displayName;

    Groups(long roleId, String displayName) {
        this.groupId = roleId;
        this.displayName = displayName;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
