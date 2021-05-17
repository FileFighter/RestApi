package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.filesystem.data.dto.upload.PreflightResponse;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeUploadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
public class FileSystemUploadService {

    // TODO: make the whole stuff caseinsensitive

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final InputSanitizerService inputSanitizerService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final MongoTemplate mongoTemplate;
    private final UserBusinessService userBusinessService;
    private final IdGenerationService idGenerationService;

    public FileSystemUploadService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, InputSanitizerService inputSanitizerService, FileSystemTypeRepository fileSystemTypeRepository, MongoTemplate mongoTemplate, UserBusinessService userBusinessService, IdGenerationService idGenerationService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.inputSanitizerService = inputSanitizerService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.mongoTemplate = mongoTemplate;
        this.userBusinessService = userBusinessService;
        this.idGenerationService = idGenerationService;
    }

    public List<FileSystemItem> uploadFileSystemItem(long rootItemId, FileSystemUpload fileSystemUpload, User authenticatedUser) {
        FileSystemEntity uploadParent = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == uploadParent)
            throw new FileSystemItemCouldNotBeUploadedException();

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.CHANGE)
                || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemCouldNotBeUploadedException();

        if (uploadParent.isFile() || uploadParent.getTypeId() != FileSystemType.FOLDER.getId())
            throw new FileSystemItemCouldNotBeUploadedException("Tried uploading to a file. Upload to a folder instead.");

        // get owner
        User ownerOfParent;
        try {
            ownerOfParent = userBusinessService.getUserById(uploadParent.getOwnerId());
        } catch (UserNotFoundException exception) {
            throw new FileFighterDataException("Owner of upload parent entity could not be found.");
        }

        // TODO: the thing is that folders that get uploaded will only have lowercase names.
        String[] paths = fileSystemHelperService.splitPathIntoEnitityPaths(fileSystemUpload.getPath().toLowerCase(), uploadParent.getPath().toLowerCase());

        List<FileSystemEntity> entitiesToUpdate = new ArrayList<>();
        List<FileSystemEntity> entitiesToCreate = new ArrayList<>();

        List<FileSystemItem> returnItems = new ArrayList<>();

        FileSystemEntity latestEntity = uploadParent;
        long timeStamp = fileSystemHelperService.getCurrentTimeStamp();

        for (int i = 0; i < paths.length - 1; i++) {
            String currentAbsolutePath = paths[i];
            String currentEntityName = fileSystemHelperService.getEntityNameFromPath(currentAbsolutePath);
            currentAbsolutePath = currentAbsolutePath.toLowerCase();

            log.debug("Checking folder path: {}", currentAbsolutePath);

            // does it exist?
            FileSystemEntity alreadyExistingFolder = fileSystemRepository.findByPathAndOwnerId(currentAbsolutePath, uploadParent.getOwnerId());
            if (null == alreadyExistingFolder) {
                // are you allowed to create it?
                if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(latestEntity, authenticatedUser, InteractionType.CHANGE)
                        || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(latestEntity, authenticatedUser, InteractionType.READ))
                    throw new FileSystemItemCouldNotBeUploadedException();

                // does a file with the same name already exist?
                Long[] childrenIdsLong = fileSystemHelperService.transformlongArrayToLong(latestEntity.getItemIds());
                List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndNameIgnoreCase(Arrays.asList(childrenIdsLong), currentEntityName);
                if (!alreadyExistingFilesWithSameName.isEmpty())
                    throw new FileSystemItemCouldNotBeUploadedException("A File with the same name already exists when creating the new folder " + currentEntityName);

                // create empty folder
                FileSystemEntity newFolder = FileSystemEntity.builder()
                        .fileSystemId(idGenerationService.consumeNext())
                        .isFile(false)
                        .visibleForUserIds(latestEntity.getVisibleForUserIds())
                        .visibleForGroupIds(latestEntity.getVisibleForGroupIds())
                        .editableFoGroupIds(latestEntity.getEditableFoGroupIds())
                        .editableForUserIds(latestEntity.getEditableForUserIds())
                        .ownerId(latestEntity.getOwnerId())
                        .lastUpdatedBy(authenticatedUser.getUserId())
                        .typeId(FileSystemType.FOLDER.getId())
                        .path(currentAbsolutePath)
                        .name(currentEntityName)
                        .lastUpdated(fileSystemHelperService.getCurrentTimeStamp())
                        .build();

                // add latestEntityTo list and add current id to itemids array.
                latestEntity.setItemIds(fileSystemHelperService.addLongToLongArray(latestEntity.getItemIds(), newFolder.getFileSystemId()));
                entitiesToUpdate.add(latestEntity);

                // set new folder entity as latest folder entity
                log.debug("Creating new Folder {}", newFolder);
                latestEntity = newFolder;
                entitiesToCreate.add(newFolder);
                returnItems.add(fileSystemHelperService.createDTO(newFolder, authenticatedUser, "/" + ownerOfParent.getUsername() + currentAbsolutePath));
            } else {
                // are you allowed to merge it?
                if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(alreadyExistingFolder, authenticatedUser, InteractionType.CHANGE)
                        || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(alreadyExistingFolder, authenticatedUser, InteractionType.READ))
                    throw new FileSystemItemCouldNotBeUploadedException();

                // if yes add alreadyExistingFolder to latest Folder entity
                log.debug("Merging existing Folder {}", alreadyExistingFolder);
                entitiesToUpdate.add(latestEntity);
                returnItems.add(fileSystemHelperService.createDTO(alreadyExistingFolder, authenticatedUser, "/" + ownerOfParent.getUsername() + currentAbsolutePath));
                latestEntity = alreadyExistingFolder;
            }
        }
        // here comes the file.
        log.debug("Checking file path: {} / {}", uploadParent.getPath().toLowerCase(), fileSystemUpload.getPath().toLowerCase());

        // are you allowed?
        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(latestEntity, authenticatedUser, InteractionType.CHANGE)
                || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(latestEntity, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemCouldNotBeUploadedException();

        // check for existing file or folder
        Long[] childrenIdsLong = fileSystemHelperService.transformlongArrayToLong(latestEntity.getItemIds());
        List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndNameIgnoreCase(Arrays.asList(childrenIdsLong), fileSystemUpload.getName());
        if (alreadyExistingFilesWithSameName.size() > 1)
            throw new FileFighterDataException("Found more than one entity with the same name in folder: " + latestEntity);

        // if name already exists and is folder -> exception. if file delete old and save new.
        if (!alreadyExistingFilesWithSameName.isEmpty()) {
            if (alreadyExistingFilesWithSameName.get(0).getTypeId() == FileSystemType.FOLDER.getId()) {
                throw new FileSystemItemCouldNotBeUploadedException("A Folder with the same name '" + fileSystemUpload.getName() + "' already exists.");
            }
            FileSystemEntity fileToOverwrite = alreadyExistingFilesWithSameName.get(0);
            log.debug("Found file to overwrite. Deleting it now. {}", fileToOverwrite);
            fileSystemHelperService.deleteAndUnbindFileSystemEntity(fileToOverwrite);
        }

        FileSystemEntity newFile = FileSystemEntity.builder()
                .fileSystemId(idGenerationService.consumeNext())
                .isFile(true)
                .visibleForUserIds(latestEntity.getVisibleForUserIds())
                .visibleForGroupIds(latestEntity.getVisibleForGroupIds())
                .editableFoGroupIds(latestEntity.getEditableFoGroupIds())
                .editableForUserIds(latestEntity.getEditableForUserIds())
                .ownerId(latestEntity.getOwnerId())
                .lastUpdatedBy(authenticatedUser.getUserId())
                .lastUpdated(timeStamp)
                .mimeType(fileSystemUpload.getMimeType())
                .typeId(fileSystemTypeRepository.parseMimeType(fileSystemUpload.getMimeType()).getId())
                .name(fileSystemUpload.getName())
                .size(fileSystemUpload.getSize())
                .build();

        // add latestEntityTo list and add current id to itemids array.
        // if the file was overwritten then remove the id from parent.
        long[] newIds = latestEntity.getItemIds();
        if (!alreadyExistingFilesWithSameName.isEmpty()) {
            newIds = Arrays.stream(latestEntity.getItemIds()).filter(id -> id != alreadyExistingFilesWithSameName.get(0).getFileSystemId()).toArray();
        }
        log.debug("Creating new File {}", newFile);
        latestEntity.setItemIds(fileSystemHelperService.addLongToLongArray(newIds, newFile.getFileSystemId()));
        entitiesToUpdate.add(latestEntity);
        entitiesToCreate.add(newFile);
        returnItems.add(fileSystemHelperService.createDTO(newFile, authenticatedUser, "/" + ownerOfParent.getUsername() + paths[paths.length - 1]));


        // TODO: size does not get updated up the tree

        // update everything except the itemIds, because that is done above.
        entitiesToUpdate.forEach(entity -> {
            Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(entity.getFileSystemId()));
            Update newUpdate = new Update();
            newUpdate.set("lastUpdated", timeStamp);
            newUpdate.set("lastUpdatedBy", authenticatedUser.getUserId());
            newUpdate.set("itemIds", entity.getItemIds());
            newUpdate.set("size", entity.getSize() + fileSystemUpload.getSize());
            mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
        });
        // create
        fileSystemRepository.insert(entitiesToCreate);

        // update timestamp from parent upwards
        fileSystemHelperService.recursivlyUpdateTimeStamps(uploadParent, authenticatedUser, timeStamp);

        return returnItems;
    }

    public List<FileSystemUploadPreflightResponse> preflightUploadFileSystemItem(long rootItemId, List<FileSystemUpload> uploads, User authenticatedUser) {
        FileSystemEntity uploadParent = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == uploadParent)
            throw new FileSystemItemCouldNotBeUploadedException();

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.CHANGE)
                || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemCouldNotBeUploadedException();

        List<FileSystemUploadPreflightResponse> preflightResponses = new ArrayList<>();
        HashMap<String, PreflightResponse> responses = new HashMap<>();

        for (FileSystemUpload upload : uploads) {
            if (null == upload)
                throw new RequestDidntMeetFormalRequirementsException("Upload was null");

            String[] paths = fileSystemHelperService.splitPathIntoEnitityPaths(upload.getPath(), uploadParent.getPath());
            String[] relativePath = fileSystemHelperService.splitPathIntoEnitityPaths(upload.getPath(), "");

            for (int i = 0; i < paths.length - 1; i++) {
                String currentAbsolutePath = paths[i];
                String currentFolderName = fileSystemHelperService.getEntityNameFromPath(currentAbsolutePath);

                PreflightResponse alreadyExistingResponse = responses.get(currentAbsolutePath);
                log.debug("Current path {} already has response: {}.", currentAbsolutePath, alreadyExistingResponse);
                log.debug("map: {}", responses);

                if (null == alreadyExistingResponse) {
                    PreflightResponse preflightResponse = handlePreflightFolder(currentAbsolutePath,
                            currentFolderName,
                            responses,
                            uploadParent, authenticatedUser);
                    log.debug("Path {} now has the response {}.", currentAbsolutePath, preflightResponse);
                    responses.put(currentAbsolutePath, preflightResponse);

                    // build the response for the folder
                    String relativeFolderPath = fileSystemHelperService.removeLeadingSlash(relativePath[i]);
                    preflightResponses.add(new FileSystemUploadPreflightResponse(
                            currentFolderName,
                            relativeFolderPath,
                            false,
                            preflightResponse.isPermissionIsSufficient(),
                            preflightResponse.isNameAlreadyInUse(),
                            preflightResponse.isNameIsValid()
                    ));
                }
            }
            log.debug("here is this file {}", upload);
            // here is the file.
            String absolutPathToFile = paths[paths.length - 1];
            PreflightResponse fileResponse = handlePreflightFile(absolutPathToFile, upload.getName(), responses, uploadParent, authenticatedUser);
            log.debug("Response: {} for upload {}", fileResponse, upload);

            // build the response and add it to list
            String relativeFilePathWithoutLeadingSlash = fileSystemHelperService.removeLeadingSlash(upload.getPath());
            preflightResponses.add(new FileSystemUploadPreflightResponse(
                    upload.getName(),
                    relativeFilePathWithoutLeadingSlash,
                    true,
                    fileResponse.isPermissionIsSufficient(),
                    fileResponse.isNameAlreadyInUse(),
                    fileResponse.isNameIsValid()
            ));
        }
        return preflightResponses;
    }

    public PreflightResponse handlePreflightFolder(String currentAbsolutePath, String currentFolderName, Map<String, PreflightResponse> responses, FileSystemEntity uploadParent, User authenticatedUser) {
        // Check if the current name is not valid
        if (!inputSanitizerService.pathIsValid(currentFolderName)) {
            return PreflightResponse.NAME_WAS_NOT_VALID;
        }

        // there was not a matching path in the map -> find a possible entity in the database
        FileSystemEntity alreadyExistingFolder = fileSystemRepository.findByPathAndOwnerId(currentAbsolutePath, uploadParent.getOwnerId());
        if (null == alreadyExistingFolder) {
            // current folder does not exist.
            String parentPath = fileSystemHelperService.getParentPathFromPath(currentAbsolutePath);

            // GET PARENT
            long[] parentsChildren = new long[0];
            FileSystemEntity parent = null;

            // 1. upload parent = parent
            if (uploadParent.getPath().equals(parentPath)) {
                parentsChildren = uploadParent.getItemIds();
                parent = uploadParent;
            } else {
                PreflightResponse alreadyHandledParent = responses.get(parentPath);

                if (alreadyHandledParent != null) {
                    // 2. parent is in map
                    switch (alreadyHandledParent) {
                        case FOLDER_CANT_BE_CREATED:

                        case FOLDER_CANT_BE_MERGED:

                        case STATEMENT_CANNOT_BE_MADE:

                        case NAME_WAS_NOT_VALID:
                            // When the parent name was not valid we cannot say something about the children.
                            return PreflightResponse.STATEMENT_CANNOT_BE_MADE;

                        case FOLDER_CAN_BE_CREATED:
                            return PreflightResponse.FOLDER_CAN_BE_CREATED;

                        case FOLDER_CAN_BE_MERGED:
                            // 3. get parent from db  (cache this with another map)
                            FileSystemEntity alreadyExistingParentFolder = fileSystemRepository.findByPathAndOwnerId(parentPath, uploadParent.getOwnerId());
                            if (alreadyExistingParentFolder == null) {
                                // 4. exception
                                throw new FileFighterDataException("Parent folder was not found while upload preflight.");
                            }
                            parent = alreadyExistingParentFolder;
                            parentsChildren = alreadyExistingParentFolder.getItemIds();
                            break;
                        default:
                            log.warn("Found enum type not explicitly handled {} when trying to handle parent {}.", alreadyHandledParent, parentPath);
                    }
                } else {
                    // parent needs to be in the map or the upload parent
                    throw new FileFighterDataException("Parent folder was not found while upload preflight.");
                }
            }
            if (null == parent)
                throw new FileFighterDataException("Parent was null.");

            // CHECK PERMISSIONS
            if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parent, authenticatedUser, InteractionType.READ) ||
                    !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parent, authenticatedUser, InteractionType.CHANGE)) {
                return PreflightResponse.FOLDER_CANT_BE_CREATED;
            }

            // CHECK FOR EXISTING FILE WITH SAME NAME. (we already checked for a folder.)
            Long[] childrenIdsLong = fileSystemHelperService.transformlongArrayToLong(parentsChildren);
            List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndNameIgnoreCase(Arrays.asList(childrenIdsLong), currentFolderName);

            if (!alreadyExistingFilesWithSameName.isEmpty()) {
                return PreflightResponse.FOLDER_CANT_BE_CREATED;
            }
            return PreflightResponse.FOLDER_CAN_BE_CREATED;
        } else {
            // a folder already exists with the current path.
            if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(alreadyExistingFolder, authenticatedUser, InteractionType.READ) ||
                    !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(alreadyExistingFolder, authenticatedUser, InteractionType.CHANGE)) {
                return PreflightResponse.FOLDER_CANT_BE_MERGED;
            } else {
                return PreflightResponse.FOLDER_CAN_BE_MERGED;
            }
        }
    }

    public PreflightResponse handlePreflightFile(String completePathWithFileName, String currentFileName, Map<String, PreflightResponse> responses, FileSystemEntity uploadParent, User authenticatedUser) {
        // Check if the current name is not valid
        if (!inputSanitizerService.pathIsValid(currentFileName)) {
            return PreflightResponse.NAME_WAS_NOT_VALID;
        }

        // there was not a matching path in the map -> find a possible folder with the same name as the file in the database
        FileSystemEntity alreadyExistingFolder = fileSystemRepository.findByPathAndOwnerId(completePathWithFileName, uploadParent.getOwnerId());
        if (null == alreadyExistingFolder) {
            // current path is not taken
            String parentPath = fileSystemHelperService.getParentPathFromPath(completePathWithFileName);

            // GET PARENT
            long[] parentsChildren = new long[0];
            FileSystemEntity parent = null;

            // 1. upload parent = parent
            if (uploadParent.getPath().equals(parentPath)) {
                parentsChildren = uploadParent.getItemIds();
                parent = uploadParent;
            } else {
                PreflightResponse alreadyHandledParent = responses.get(parentPath);

                if (alreadyHandledParent != null) {
                    // 2. parent is in map
                    switch (alreadyHandledParent) {
                        case FOLDER_CANT_BE_CREATED:

                        case FOLDER_CANT_BE_MERGED:

                        case STATEMENT_CANNOT_BE_MADE:

                        case NAME_WAS_NOT_VALID:
                            // When the parent name was not valid we cannot say something about the children.
                            return PreflightResponse.STATEMENT_CANNOT_BE_MADE;

                        case FOLDER_CAN_BE_CREATED:
                            return PreflightResponse.FILE_CAN_BE_CREATED;

                        case FOLDER_CAN_BE_MERGED:
                            // 3. get parent from db  (cache this with another map)
                            FileSystemEntity alreadyExistingParentFolder = fileSystemRepository.findByPathAndOwnerId(parentPath, uploadParent.getOwnerId());
                            if (alreadyExistingParentFolder == null) {
                                // 4. exception
                                throw new FileFighterDataException("Parent folder was not found while upload preflight.");
                            }
                            parent = alreadyExistingParentFolder;
                            parentsChildren = alreadyExistingParentFolder.getItemIds();
                            break;
                        default:
                            log.warn("Found enum type not explicitly handled {} when trying to handle parent {}.", alreadyHandledParent, parentPath);
                    }
                } else {
                    // parent needs to be in the map or the upload parent
                    throw new FileFighterDataException("Parent folder was not found while upload preflight.");
                }
            }
            if (null == parent)
                throw new FileFighterDataException("Parent was null.");

            // CHECK PERMISSIONS
            if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parent, authenticatedUser, InteractionType.READ) ||
                    !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parent, authenticatedUser, InteractionType.CHANGE)) {
                return PreflightResponse.FILE_CANT_BE_CREATED;
            }

            // CHECK FOR EXISTING FILE WITH SAME NAME. (we already checked for a folder.)
            Long[] childrenIdsLong = fileSystemHelperService.transformlongArrayToLong(parentsChildren);
            List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndNameIgnoreCase(Arrays.asList(childrenIdsLong), currentFileName);

            if (!alreadyExistingFilesWithSameName.isEmpty()) {
                return PreflightResponse.FILE_CAN_BE_OVERWRITEN;
            }
            return PreflightResponse.FILE_CAN_BE_CREATED;
        } else {
            // a folder already exists with the current path of the file, so you can't create it
            return PreflightResponse.FILE_CANT_BE_CREATED;
        }
    }
}