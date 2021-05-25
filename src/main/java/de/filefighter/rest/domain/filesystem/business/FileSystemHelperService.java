package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDownloadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Group;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService.DELETION_FAILED_MSG;

@Service
@Log4j2
public class FileSystemHelperService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final UserBusinessService userBusinessService;
    private final MongoTemplate mongoTemplate;
    private final IdGenerationService idGenerationService;

    public FileSystemHelperService(FileSystemRepository fileSystemRepository, FileSystemTypeRepository fileSystemTypeRepository, UserBusinessService userBusinessService, MongoTemplate mongoTemplate, IdGenerationService idGenerationService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.userBusinessService = userBusinessService;
        this.mongoTemplate = mongoTemplate;
        this.idGenerationService = idGenerationService;
    }

    public FileSystemEntity sumUpAllPermissionsOfFileSystemEntities(FileSystemEntity parentFileSystemEntity, List<FileSystemEntity> fileSystemEntities) {
        HashSet<Long> visibleForUserIds = new HashSet<>();
        HashSet<Long> visibleForGroupIds = new HashSet<>();
        HashSet<Long> editableForUserIds = new HashSet<>();
        HashSet<Long> editableGroupIds = new HashSet<>();

        for (FileSystemEntity entity : fileSystemEntities) {
            addPermissionsToSets(visibleForUserIds, visibleForGroupIds, editableForUserIds, editableGroupIds, entity);
        }

        parentFileSystemEntity.setVisibleForUserIds(this.transformLongCollectionTolongArray(visibleForUserIds));
        parentFileSystemEntity.setVisibleForGroupIds(this.transformLongCollectionTolongArray(visibleForGroupIds));
        parentFileSystemEntity.setEditableForUserIds(this.transformLongCollectionTolongArray(editableForUserIds));
        parentFileSystemEntity.setEditableFoGroupIds(this.transformLongCollectionTolongArray(editableGroupIds));
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

    public void removeVisibilityRightsOfFileSystemEntityForUser(FileSystemEntity entity, User authenticatedUser) {
        Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(entity.getFileSystemId()));
        Update newUpdate = new Update();

        // user is either directly in the visible ids or in a group that is visible.
        long[] newIdsWithoutCurrentUserId = Arrays.stream(entity.getVisibleForUserIds()).filter(userId -> userId != authenticatedUser.getUserId()).toArray();
        if (newIdsWithoutCurrentUserId.length != entity.getVisibleForUserIds().length) {
            // apply it.
            newUpdate.set("visibleForUserIds", newIdsWithoutCurrentUserId);
        }

        // or user is in a group that can see the filesystem entity.
        long[] newGroupIds = entity.getVisibleForGroupIds();
        if (newGroupIds.length != 0) {
            for (Group group : authenticatedUser.getGroups()) {
                newGroupIds = Arrays.stream(newGroupIds).filter(id -> id != group.getGroupId()).toArray();
            }
            newUpdate.set("visibleForGroupIds", newGroupIds);
        }
        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

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

        // user own the file.
        if (fileSystemEntity.getOwnerId() == authenticatedUser.getUserId())
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
            ownerOfFileSystemItem = userBusinessService.findUserById(fileSystemEntity.getOwnerId());
            lastUpdatedByUser = userBusinessService.findUserById(fileSystemEntity.getLastUpdatedBy());
        } catch (UserNotFoundException exception) {
            log.debug("Found UserNotFoundException in createDTO. Entity: {}.", fileSystemEntity);
            throw new FileFighterDataException("Owner or auther of last change could not be found.");
        }

        boolean isShared = ownerOfFileSystemItem.getUserId() != RestConfiguration.RUNTIME_USER_ID
                && ownerOfFileSystemItem.getUserId() != authenticatedUser.getUserId();
        FileSystemType type = fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId());
        boolean isAFolder = type == FileSystemType.FOLDER && !fileSystemEntity.isFile();
        String entityName = fileSystemEntity.getName();

        if (absolutePathWithUsername != null) {
            if (absolutePathWithUsername.equals("/")) {
                absolutePathWithUsername = (absolutePathWithUsername + ownerOfFileSystemItem.getUsername()).toLowerCase(); // this is only for the case of the path = "/"
                entityName = ownerOfFileSystemItem.getUsername();
            } else {
                absolutePathWithUsername = this.removeTrailingBackSlashes(absolutePathWithUsername).toLowerCase();
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
                .fileSystemId(idGenerationService.consumeNext())
                .ownerId(registeredUserEntity.getUserId())
                .lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID)
                .lastUpdated(getCurrentTimeStamp())
                .typeId(FileSystemType.FOLDER.getId())
                .isFile(false)
                .name("HOME_" + registeredUserEntity.getUserId())
                .path("/")
                .lastUpdated(Instant.now().getEpochSecond())
                .size(0)
                .mimeType(null)
                .build());
    }

    public void deleteAndUnbindFileSystemEntity(FileSystemEntity fileSystemEntity) {
        Long countDeleted = fileSystemRepository.deleteByFileSystemId(fileSystemEntity.getFileSystemId());
        if (countDeleted != 1)
            throw new FileFighterDataException(DELETION_FAILED_MSG + fileSystemEntity.getFileSystemId());

        Query query = new Query().addCriteria(Criteria.where("itemIds").is(fileSystemEntity.getFileSystemId()));
        Update newUpdate;

        // TODO: fix this sizing.
        // only reduce size if the entity is a file.
        if (fileSystemEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId()) != FileSystemType.FOLDER) {
            newUpdate = new Update().pull("itemIds", fileSystemEntity.getFileSystemId()).inc("size", fileSystemEntity.getSize() * -1); // hacky stuff.
        } else {
            newUpdate = new Update().pull("itemIds", fileSystemEntity.getFileSystemId());
        }
        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    public void recursivlyUpdateTimeStamps(FileSystemEntity currentEntity, User autheticatedUser, long currentTimeStamp) {
        Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(currentEntity.getFileSystemId()));
        Update update = new Update().set("lastUpdated", currentTimeStamp).set("lastUpdatedBy", autheticatedUser.getUserId());
        mongoTemplate.findAndModify(query, update, FileSystemEntity.class);

        Query queryParentEntity = new Query().addCriteria(Criteria.where("itemIds").is(currentEntity.getFileSystemId()));
        List<FileSystemEntity> parentFileSystemEntities = mongoTemplate.find(queryParentEntity, FileSystemEntity.class);

        if (parentFileSystemEntities.isEmpty()) {
            // no parents found -> either root folder or an exception
            boolean isFile = currentEntity.isFile() && currentEntity.getTypeId() != FileSystemType.FOLDER.getId();
            boolean isRootFolder = !isFile && currentEntity.getPath().equals("/");
            if (!isRootFolder) {
                log.debug("Found no parent entity for a non root entity: " + currentEntity);
                throw new FileFighterDataException("Found no parent entity for a non root entity.");
            }
            // else return.
        } else {
            if (parentFileSystemEntities.size() > 1) {
                log.debug("Found more than one parent entity for entity: " + currentEntity);
                throw new FileFighterDataException("Found more than one parent entity for entity.");
            }

            recursivlyUpdateTimeStamps(parentFileSystemEntities.get(0), autheticatedUser, currentTimeStamp);
        }
    }

    public String[] splitPathIntoEnitityPaths(String path, String basePath) {
        Object[] paths = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray();
        String[] returnString = new String[paths.length];

        // if the path is empty or null make it look like its a "/"
        if (null == basePath || basePath.isEmpty() || basePath.isBlank()) {
            basePath = "/";
        }
        StringBuilder pathStringBuilder = new StringBuilder(basePath);
        for (int i = 0; i < paths.length; i++) {
            if (pathStringBuilder.toString().charAt(pathStringBuilder.toString().length() - 1) != '/') {
                pathStringBuilder.append("/");
            }
            pathStringBuilder.append(paths[i]);
            returnString[i] = pathStringBuilder.toString();
        }
        return returnString;
    }

    public String getEntityNameFromPath(String path) {
        String[] splittedPath = path.split("/");
        try {
            return splittedPath[splittedPath.length - 1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.debug("path was {}.", path);
            throw new FileFighterDataException("Path to check was not valid");
        }
    }

    public String getParentPathFromPath(String path) {
        String entityName = getEntityNameFromPath(path);
        String parent = path.substring(0, path.length() - entityName.length() - 1);
        return parent.equals("") ? "/" : parent;
    }

    public void getContentsOfFolderRecursivly(List<FileSystemItem> listToAdd, FileSystemEntity currentEntitiy, User authenticatedUser, String relativePath, boolean multipleEntitiesInCurrentEntity) {
        if (currentEntitiy.isFile() || currentEntitiy.getTypeId() != FileSystemType.FOLDER.getId()) {
            listToAdd.add(this.createDTO(currentEntitiy, authenticatedUser, relativePath + currentEntitiy.getName()));
        } else {
            List<FileSystemEntity> folderContents = this.getFolderContentsOfEntityAndPermissions(currentEntitiy, authenticatedUser, true, false);
            if (currentEntitiy.getItemIds().length == 0) return;

            if (null == folderContents || folderContents.isEmpty())
                throw new FileFighterDataException("Found no children for FileSystemEntity with id " + currentEntitiy.getFileSystemId());

            boolean currentRunIsTheFirst = relativePath.equals("");
            boolean currentEntityIsInRoot = currentEntitiy.getPath().equals("/");

            String nextRelativePath = getNextRelativePath(currentEntitiy, relativePath, multipleEntitiesInCurrentEntity, currentRunIsTheFirst, currentEntityIsInRoot);

            folderContents.stream()
                    .filter(nextEntity -> this.userIsAllowedToInteractWithFileSystemEntity(nextEntity, authenticatedUser, InteractionType.READ))
                    .forEach(nextEntity -> getContentsOfFolderRecursivly(listToAdd, nextEntity, authenticatedUser, nextRelativePath, folderContents.size() > 1));
        }

    }

    private String getNextRelativePath(FileSystemEntity currentEntitiy, String relativePath, boolean multipleEntitiesInCurrentEntity, boolean currentRunIsTheFirst, boolean currentEntityIsInRoot) {
        String nextRelativePath = "";
        if (currentRunIsTheFirst) {
            if (multipleEntitiesInCurrentEntity) {
                if (currentEntityIsInRoot) {
                    nextRelativePath = relativePath + this.getOwnerUsernameForEntity(currentEntitiy) + "/";
                } else {
                    nextRelativePath = relativePath + currentEntitiy.getName() + "/";
                }
            }
        } else {
            nextRelativePath = relativePath + currentEntitiy.getName() + "/";
        }
        return nextRelativePath;
    }

    public String getNameOfZipWhenMultipleEntitiesNeedToBeDownloaded(List<FileSystemEntity> entities, boolean allEntitiesAreInRoot) {
        if (allEntitiesAreInRoot) {
            StringBuilder sb = new StringBuilder();
            entities.stream()
                    .map(this::getOwnerUsernameForEntity)
                    .distinct()
                    .forEach(sb::append);
            return sb.toString();
        }

        Optional<String> parentEntityName = entities.stream()
                .map(entity -> this.getParentNameEntity().apply(entity)).findFirst();

        if (parentEntityName.isEmpty())
            throw new FileSystemItemCouldNotBeDownloadedException("FileSystemEntity need to have a common parent entity.");

        return parentEntityName.get();
    }

    public String getNameOfZipWhenOnlyOneEntityNeedsToBeDownloaded(FileSystemEntity currentEntity, boolean allEntitiesAreInRoot) {
        String zipName = null;

        // if it is a file we dont need to set the header.
        if (!currentEntity.isFile()) {
            if (allEntitiesAreInRoot) {
                // get owner name and set it as header.
                zipName = this.getOwnerUsernameForEntity(currentEntity);
            } else {
                zipName = currentEntity.getName();
            }
        }

        return zipName;
    }

    public Function<FileSystemEntity, String> getParentNameEntity() {
        return entity -> {
            if (!entity.isFile() && entity.getPath().equals("/")) return null;

            FileSystemEntity parent = fileSystemRepository.findByItemIdsContaining(entity.getFileSystemId());
            if (null == parent)
                throw new FileFighterDataException("Couldn't find the parent of the fileSystemEntity with id " + entity.getFileSystemId());

            if (parent.getPath().equals("/"))
                return this.getOwnerUsernameForEntity(parent);

            return parent.getName();
        };
    }

    public String getOwnerUsernameForEntity(FileSystemEntity entity) {
        User owner;
        try {
            owner = userBusinessService.findUserById(entity.getOwnerId());
        } catch (UserNotFoundException ex) {
            throw new FileFighterDataException("Owner for id " + entity.getOwnerId() + " could not be found.");
        }
        return owner.getUsername();
    }

    public FileSystemEntity getRootEntityForUser(User userWithTheName) {
        List<FileSystemEntity> rootForUser = fileSystemRepository.findByPath("/")
                .stream()
                .filter(entity -> entity.getOwnerId() == userWithTheName.getUserId())
                .collect(Collectors.toList());

        if (rootForUser.isEmpty())
            throw new FileFighterDataException("Found not root folder for user with id: " + userWithTheName.getUserId());

        if (rootForUser.size() > 1)
            throw new FileFighterDataException("Found more than one root folder for user with id: " + userWithTheName.getUserId());

        return rootForUser.get(0);
    }

    public double getTotalFileSize() {
        List<FileSystemEntity> entities = fileSystemRepository.findByPath("/");
        if (null == entities)
            throw new FileFighterDataException("Couldn't find any Home directories!");

        double size = 0;
        for (FileSystemEntity entity : entities) {
            size += entity.getSize();
        }
        return size;
    }

    public long[] addLongToLongArray(long[] array, long newLong) {
        long[] newArray = new long[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = newLong;
        return newArray;
    }

    public Long[] transformlongArrayToLong(long[] arrayToTransform) {
        Long[] longArgument = new Long[arrayToTransform.length];
        int i = 0;

        for (long temp : arrayToTransform) {
            longArgument[i++] = temp;
        }
        return longArgument;
    }

    public String removeLeadingSlash(String path) {
        if (!InputSanitizerService.stringIsValid(path))
            throw new IllegalArgumentException("Couldn't remove leading slash because the path was not a valid String.");

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    public long[] transformLongCollectionTolongArray(Collection<Long> collectionToTransform) {
        return Arrays.stream(collectionToTransform.toArray(new Long[0])).mapToLong(Long::longValue).toArray();
    }

    public long getCurrentTimeStamp() {
        return Instant.now().getEpochSecond();
    }
}
