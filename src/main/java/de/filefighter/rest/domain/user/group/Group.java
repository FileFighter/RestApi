package de.filefighter.rest.domain.user.group;

public enum Group {
    UNDEFINED(-1, "No group"),
    FAMILY(0, "Family"),
    ADMIN(1, "Admin"),
    SYSTEM(999, "FileFighter System Users");

    private final long groupId;
    private final String displayName;

    Group(long groupId, String displayName) {
        this.groupId = groupId;
        this.displayName = displayName;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
