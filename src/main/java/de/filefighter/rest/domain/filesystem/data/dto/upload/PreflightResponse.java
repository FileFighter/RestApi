package de.filefighter.rest.domain.filesystem.data.dto.upload;

import lombok.Getter;

@Getter
public enum PreflightResponse {
    // TODO make this better with an id or smth.
    NAME_WAS_NOT_VALID(false, false, true),
    STATEMENT_CANNOT_BE_MADE(true, false, true),

    FILE_CAN_BE_CREATED(true, false, true),
    FOLDER_CAN_BE_CREATED(true, false, true),

    FOLDER_CAN_BE_MERGED(true, true, true),
    FILE_CAN_BE_OVERWRITEN(true, true, true),

    FILE_CANT_BE_CREATED(true, false, false),
    FOLDER_CANT_BE_CREATED(true, false, false),

    FILE_CANT_BE_OVERWRITTEN(true, true, false),
    FOLDER_CANT_BE_MERGED(true, true, false);

    private final boolean nameIsValid;
    private final boolean nameAlreadyInUse;
    private final boolean permissionIsSufficient;

    PreflightResponse(boolean nameIsValid, boolean nameAlreadyInUse, boolean permissionIsSufficient) {
        this.nameIsValid = nameIsValid;
        this.nameAlreadyInUse = nameAlreadyInUse;
        this.permissionIsSufficient = permissionIsSufficient;
    }
}