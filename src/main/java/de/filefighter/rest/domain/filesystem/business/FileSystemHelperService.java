package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Group;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class FileSystemHelperService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final UserBusinessService userBusinessService;

    public FileSystemHelperService(FileSystemRepository fileSystemRepository, FileSystemTypeRepository fileSystemTypeRepository, UserBusinessService userBusinessService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.userBusinessService = userBusinessService;
    }

    public FileSystemEntity sumUpAllPermissionsOfFileSystemEntities(FileSystemEntity parentFileSystemEntity, List<FileSystemEntity> fileSystemEntities) {
        HashSet<Long> visibleForUserIds = new HashSet<>();
        HashSet<Long> visibleForGroupIds = new HashSet<>();
        HashSet<Long> editableForUserIds = new HashSet<>();
        HashSet<Long> editableGroupIds = new HashSet<>();

        for (FileSystemEntity entity : fileSystemEntities) {
            addPermissionsToSets(visibleForUserIds, visibleForGroupIds, editableForUserIds, editableGroupIds, entity);
        }

        parentFileSystemEntity.setVisibleForUserIds(Arrays.stream(visibleForUserIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setVisibleForGroupIds(Arrays.stream(visibleForGroupIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setEditableForUserIds(Arrays.stream(editableForUserIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setEditableFoGroupIds(Arrays.stream(editableGroupIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        return parentFileSystemEntity;
    }

    public void addPermissionsToSets(Set<Long> visibleForUserIds, Set<Long> visibleForGroupIds, Set<Long> editableForUserIds, Set<Long> editableGroupIds, FileSystemEntity fileSystemEntity) {
        for (long i : fileSystemEntity.getVisibleForUserIds()) {
            visibleForUserIds.add(i);
        }
        for (long i : fileSystemEntity.getVisibleForGroupIds()) {
            visibleForGroupIds.add(i);
        }
        for (long i : fileSystemEntity.getEditableForUserIds()) {
            editableForUserIds.add(i);
        }
        for (long i : fileSystemEntity.getEditableFoGroupIds()) {
            editableGroupIds.add(i);
        }
    }

    // TODO: refactor this to a list of interaction types.
    public List<FileSystemEntity> getFolderContentsOfEntityAndPermissions(FileSystemEntity fileSystemEntity, User authenticatedUser, boolean needsToBeVisible, boolean needsToBeEditable) {
        long[] folderContentItemIds = fileSystemEntity.getItemIds();
        List<FileSystemEntity> fileSystemEntities = new ArrayList<>(folderContentItemIds.length);

        // check if the contents are visible / editable.
        for (long fileSystemId : folderContentItemIds) {
            FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

            if (null == fileSystemEntityInFolder)
                throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + fileSystemId + " but was empty.");

            if (needsToBeVisible && !needsToBeEditable && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.READ)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeEditable && !needsToBeVisible && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.CHANGE)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeVisible && needsToBeEditable && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.READ) && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.CHANGE)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (!needsToBeVisible && !needsToBeEditable) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
        }
        return fileSystemEntities;
    }

    @SuppressWarnings("java:S3776")
    public boolean userIsAllowedToInteractWithFileSystemEntity(FileSystemEntity fileSystemEntity, User authenticatedUser, InteractionType interaction) {
        // file was created by runtime user.
        if ((interaction == InteractionType.DELETE)
                && fileSystemEntity.getLastUpdatedBy() == RestConfiguration.RUNTIME_USER_ID)
            return false;

        // user created the item
        if (fileSystemEntity.getLastUpdatedBy() == authenticatedUser.getUserId())
            return true;

        // user got the item shared.
        if (interaction == InteractionType.READ) {
            for (long userId : fileSystemEntity.getVisibleForUserIds()) {
                if (userId == authenticatedUser.getUserId())
                    return true;
            }

            // user is in group that got the item shared.
            long[] fileIsSharedToGroups = fileSystemEntity.getVisibleForGroupIds();
            for (Group group : authenticatedUser.getGroups()) {
                for (long groupId : fileIsSharedToGroups) {
                    if (groupId == group.getGroupId())
                        return true;

                }
            }
        }
        if (interaction == InteractionType.CHANGE || interaction == InteractionType.DELETE) {
            for (long userId : fileSystemEntity.getEditableForUserIds()) {
                if (userId == authenticatedUser.getUserId())
                    return true;
            }

            // user is in group that got the item shared.
            long[] fileIsSharedToGroups = fileSystemEntity.getEditableFoGroupIds();
            for (Group group : authenticatedUser.getGroups()) {
                for (long groupId : fileIsSharedToGroups) {
                    if (groupId == group.getGroupId())
                        return true;

                }
            }
        }
        return false;
    }

    public String removeTrailingBackSlashes(String pathToFind) {
        char[] chars = pathToFind.toCharArray();
        // for the case of "/"
        if (chars.length != 1 && chars[chars.length - 1] == '/') {
            chars = Arrays.copyOf(chars, chars.length - 1);
            return new String(chars);
        }
        return pathToFind;
    }

    public FileSystemItem createDTO(FileSystemEntity fileSystemEntity, User authenticatedUser, String absolutePathWithUsername) {
        // for better responses and internal problem handling.
        User ownerOfFileSystemItem;
        User lastUpdatedByUser;
        try {
            ownerOfFileSystemItem = userBusinessService.getUserById(fileSystemEntity.getOwnerId());
            lastUpdatedByUser = userBusinessService.getUserById(fileSystemEntity.getLastUpdatedBy());
        } catch (UserNotFoundException exception) {
            throw new FileFighterDataException("Owner or auther of last change could not be found. Entity: " + fileSystemEntity);
        }

        boolean isShared = ownerOfFileSystemItem.getUserId() != RestConfiguration.RUNTIME_USER_ID
                && ownerOfFileSystemItem.getUserId() != authenticatedUser.getUserId();
        FileSystemType type = fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId());
        boolean isAFolder = type == FileSystemType.FOLDER && !fileSystemEntity.isFile();
        String entityName = fileSystemEntity.getName();

        if (absolutePathWithUsername != null) {
            if (absolutePathWithUsername.equals("/")) {
                absolutePathWithUsername = absolutePathWithUsername + ownerOfFileSystemItem.getUsername(); // this is only for the case of the path = "/"
                entityName = ownerOfFileSystemItem.getUsername();
            } else {
                absolutePathWithUsername = this.removeTrailingBackSlashes(absolutePathWithUsername) + "/" + fileSystemEntity.getName();
            }
        }

        return FileSystemItem.builder()
                .lastUpdatedBy(lastUpdatedByUser)
                .fileSystemId(fileSystemEntity.getFileSystemId())
                .owner(ownerOfFileSystemItem)
                .lastUpdated(fileSystemEntity.getLastUpdated())
                .name(entityName)
                .size(fileSystemEntity.getSize())
                .type(isAFolder ? FileSystemType.FOLDER : type)
                .path(absolutePathWithUsername)
                .isShared(isShared)
                .mimeType(fileSystemEntity.getMimeType())
                .build();
    }

    public void createBasicFilesForNewUser(UserEntity registeredUserEntity) {
        fileSystemRepository.save(FileSystemEntity
                .builder()
                .lastUpdatedBy(0)
                .typeId(FileSystemType.FOLDER.getId())
                .isFile(false)
                .name("HOME_" + registeredUserEntity.getUsername())
                .path("/")
                .lastUpdated(Instant.now().getEpochSecond())
                .fileSystemId(generateNextFileSystemId())
                .build());
    }

    public double getTotalFileSize() {
        ArrayList<FileSystemEntity> entities = fileSystemRepository.findByPath("/");
        if (null == entities)
            throw new FileFighterDataException("Couldn't find any Home directories!");

        double size = 0;
        for (FileSystemEntity entity : entities) {
            size += entity.getSize();
        }
        return size;
    }

    public long getFileSystemEntityCount() {
        return fileSystemRepository.count();
    }

    // This will update the field. -> Everytime this function gets called a id gets taken. Which means some ids could be lost, when calling this function and not creating something.
    public long generateNextFileSystemId() {
        return getFileSystemEntityCount() + 1;
    }

    public long getCurrentTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }
}
