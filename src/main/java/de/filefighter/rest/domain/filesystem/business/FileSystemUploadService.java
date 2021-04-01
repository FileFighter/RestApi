package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemsCouldNotBeUploadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class FileSystemUploadService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final MongoTemplate mongoTemplate;
    private final InputSanitizerService inputSanitizerService;

    public FileSystemUploadService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, FileSystemTypeRepository fileSystemTypeRepository, MongoTemplate mongoTemplate, InputSanitizerService inputSanitizerService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.mongoTemplate = mongoTemplate;
        this.inputSanitizerService = inputSanitizerService;
    }

    public FileSystemItem uploadFileSystemItem(long rootItemId, FileSystemUpload fileSystemUpload, User authenticatedUser) {
        /*FileSystemEntity parentFileSystemEntity = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == parentFileSystemEntity)
            throw new FileSystemItemNotFoundException(rootItemId);

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parentFileSystemEntity, authenticatedUser, InteractionType.CHANGE))
            throw new FileSystemItemsCouldNotBeUploadedException(rootItemId);

        if (parentFileSystemEntity.isFile() || parentFileSystemEntity.getTypeId() != FileSystemType.FOLDER.getId())
            throw new FileSystemItemsCouldNotBeUploadedException("The specified rootItemId was a file.");

        // get requests upload paths
        List<String> paths = Arrays.stream(fileSystemUpload.getPath().split("/"))
                .filter(InputSanitizerService::stringIsValid).collect(Collectors.toList());

        // add user to owner list (without duplicate)
        long[] newOwnerIds;
        long[] oldOwnerIds = parentFileSystemEntity.getOwnerIds();

        OptionalLong optionalUserId = Arrays.stream(oldOwnerIds).filter(id -> id == authenticatedUser.getUserId()).findAny();
        if (optionalUserId.isEmpty()) {
            newOwnerIds = new long[oldOwnerIds.length + 1];
            System.arraycopy(oldOwnerIds, 0, newOwnerIds, 0, oldOwnerIds.length);
            newOwnerIds[oldOwnerIds.length] = authenticatedUser.getUserId();
        } else {
            newOwnerIds = oldOwnerIds;
        }

        // Variables needed for the loop
        String folderPathToCheck = parentFileSystemEntity.getPath(); // could be / but also /foo/bar -> not necessarily a trailing backslash.
        long timeStamp = fileSystemHelperService.getCurrentTimeStamp();
        long nextId = fileSystemHelperService.generateNextFileSystemId();
        FileSystemEntity uploadedFile = null;
        long newItemId = -1;

        // loop over the paths, and check if a folder with the same path exists.
        for (int i = 0; i < paths.size(); i++) {
            String currentPath = paths.get(i);

            // checks if the current path is the last split.
            if (i == paths.size() - 1) {
                // check if a file already exists with the same name.
                // check for children with the name to be uploaded
                // only necessary when no new folder was created.
                if (paths.size() == 1) {
                    List<FileSystemEntity> childrenOfTheParentEntity = fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentFileSystemEntity, authenticatedUser, false, false);
                    Optional<FileSystemEntity> optionalEntityWithTheSameName = childrenOfTheParentEntity.stream().filter(children -> children.getName().equals(fileSystemUpload.getName())).findAny();
                    if (optionalEntityWithTheSameName.isPresent())
                        throw new FileSystemItemsCouldNotBeUploadedException("A file with the same name already exists.");
                }

                // create new file
                uploadedFile = FileSystemEntity.builder()
                        .fileSystemId(nextId)
                        .name(fileSystemUpload.getName())
                        .path(null)
                        .typeId(fileSystemTypeRepository.parseMimeType(fileSystemUpload.getMimeType()).getId())
                        .mimeType(fileSystemUpload.getMimeType())
                        .size(fileSystemUpload.getSize())
                        .lastUpdated(timeStamp)
                        .isFile(true)
                        .createdByUserId(authenticatedUser.getUserId())
                        .ownerIds(newOwnerIds)
                        .visibleForUserIds(parentFileSystemEntity.getVisibleForUserIds())
                        .visibleForGroupIds(parentFileSystemEntity.getVisibleForGroupIds())
                        .editableForUserIds(parentFileSystemEntity.getEditableForUserIds())
                        .editableFoGroupIds(parentFileSystemEntity.getEditableFoGroupIds())
                        .build();

                // set newItemId if not already set
                if (newItemId == -1) {
                    newItemId = nextId;
                }

                log.info("Creating new file {} for user {}.", uploadedFile, authenticatedUser.getUserId());
                fileSystemRepository.save(uploadedFile);
            } else {
                // concat pathToCheck with currentPath
                folderPathToCheck += folderPathToCheck.equals("/") ? currentPath : "/" + currentPath;

                // check for existing folder.
                List<FileSystemEntity> possibleExistingEntity = fileSystemRepository.findByPath(folderPathToCheck);
                for (FileSystemEntity entity : possibleExistingEntity) {
                    if (fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ))
                        throw new FileSystemItemsCouldNotBeUploadedException("A folder with the name " + entity.getName() + " already exists.");
                }

                FileSystemEntity newFolder = FileSystemEntity.builder()
                        .fileSystemId(nextId)
                        .name(currentPath)
                        .path(folderPathToCheck)
                        .typeId(FileSystemType.FOLDER.getId())
                        .size(fileSystemUpload.getSize())
                        .lastUpdated(timeStamp)
                        .isFile(false)
                        .createdByUserId(authenticatedUser.getUserId())
                        .ownerIds(newOwnerIds)
                        .visibleForUserIds(parentFileSystemEntity.getVisibleForUserIds())
                        .visibleForGroupIds(parentFileSystemEntity.getVisibleForGroupIds())
                        .editableForUserIds(parentFileSystemEntity.getEditableForUserIds())
                        .editableFoGroupIds(parentFileSystemEntity.getEditableFoGroupIds())
                        .build();

                // set newItemId if not already set
                if (newItemId == -1) {
                    newItemId = nextId;
                }
                nextId = fileSystemHelperService.generateNextFileSystemId() + 1; // this is necessary because the file is not created, thus the count of files will not change -> would lead to the same number.
                newFolder.setItemIds(new long[]{nextId});
                log.info("Creating new folder {} for user {}.", newFolder, authenticatedUser.getUserId());
                fileSystemRepository.save(newFolder);
            }
        }

        // add new file / folder to the itemIds of the the parentFileSystemEntity
        if (newItemId != -1) {
            Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(parentFileSystemEntity.getFileSystemId()));
            Update newUpdate = new Update().push("itemIds", newItemId);
            mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
        }

        String parentPath = folderPathToCheck.equals("/") ? "/" : folderPathToCheck + "/";
        return uploadedFile == null ? null : fileSystemHelperService.createDTO(uploadedFile, authenticatedUser, parentPath);

         */
        return null;
    }

    public List<FileSystemUploadPreflightResponse> preflightUploadFileSystemItem(long rootItemId, List<FileSystemUpload> uploads, User authenticatedUser) {
        FileSystemEntity parent = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == parent)
            throw new FileSystemItemNotFoundException(rootItemId);

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parent, authenticatedUser, InteractionType.CHANGE))
            throw new FileSystemItemsCouldNotBeUploadedException(rootItemId);

        if (parent.isFile() || parent.getTypeId() != FileSystemType.FOLDER.getId())
            throw new FileSystemItemsCouldNotBeUploadedException("The specified rootItemId was a file.");

        // Return Value
        ArrayList<FileSystemUploadPreflightResponse> returnList = new ArrayList<>();

        for (FileSystemUpload upload : uploads) {
            // get requests upload paths
            List<String> paths = Arrays.stream(upload.getPath().split("/"))
                    .filter(InputSanitizerService::stringIsValid).collect(Collectors.toList());

            // Variables needed for the loop
            String parentPathWithTrailingSlash = parent.getPath().equals("/") ? "/" : parent.getPath() + "/";
            String currentRelativePath = "";

            // flag wheter a no permissions.
            boolean noPermissions = false;

            // loop over the paths, and check if a folder with the same path exists.
            for (int i = 0; i < paths.size(); i++) {
                String currentEntityName = paths.get(i);

                // check permissions
                if (noPermissions) {

                }

                // checks if the current path is the last split.
                if (i == paths.size() - 1) {
                    /*log.info("File {} in {}.", currentEntityName, folderPathToCheck);
                    // check if a file already exists with the same name.
                    // check for children with the name to be uploaded
                    // only necessary when no new folder was created.
                    if (paths.size() == 1) {
                        List<FileSystemEntity> childrenOfTheParentEntity = fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parent, authenticatedUser, false, false);
                        Optional<FileSystemEntity> optionalEntityWithTheSameName = childrenOfTheParentEntity.stream().filter(children -> children.getName().equals(upload.getName())).findAny();
                        if (optionalEntityWithTheSameName.isPresent())
                            throw new FileSystemItemsCouldNotBeUploadedException("A file with the same name already exists.");
                    }

                     */
                } else {
                    // concat pathToCheck with currentPath
                    currentRelativePath += currentRelativePath + "/" + currentEntityName;

                    // check is already in returnArray
                    String finalCurrentRelativePath = currentRelativePath;
                    List<FileSystemUploadPreflightResponse> foundElement = returnList.stream().filter(element -> element.getPath().equals(finalCurrentRelativePath)).collect(Collectors.toList());
                    if (foundElement.size() == 1) {
                        if (foundElement.get(0).getPermissionIsSufficient()) {
                            noPermissions = true;
                        }
                        break;
                    } else if (foundElement.size() > 1)
                        throw new FileFighterDataException("Found duplicate in returnList.");

                    // check existing.
                    List<FileSystemEntity> possibleFolders = fileSystemRepository.findByPath(parentPathWithTrailingSlash + currentRelativePath);
                    if (possibleFolders.isEmpty()) {
                        returnList.add(new FileSystemUploadPreflightResponse(
                                currentEntityName,
                                currentRelativePath,
                                true,
                                false,
                                false,
                                inputSanitizerService.pathIsValid(currentRelativePath)));
                        break;
                    }

                    /*
                    list of folders.
                    user is owner -> in owner.
                    user is not in owner -> check shared folders for permissions if not allowed -> create own.
                    What if a user has two folders shared with the same name but no folder created on his/her own.
                     */

                    // check permission
                    List<FileSystemEntity> editableFolders =
                            possibleFolders.stream().filter(folder ->
                                    fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(folder, authenticatedUser, InteractionType.CHANGE)).collect(Collectors.toList());

                    if (editableFolders.isEmpty() {
                        noPermissions = true;


                        break;
                    }


                }
            }
        }
        return null;
    }
}
