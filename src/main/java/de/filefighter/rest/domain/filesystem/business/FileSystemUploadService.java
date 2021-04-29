package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.filesystem.data.dto.upload.PreflightResponse;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeUploadedException;
import de.filefighter.rest.domain.user.data.dto.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class FileSystemUploadService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final InputSanitizerService inputSanitizerService;

    public FileSystemUploadService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, InputSanitizerService inputSanitizerService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.inputSanitizerService = inputSanitizerService;
    }

    public FileSystemItem uploadFileSystemItem(long rootItemId, FileSystemUpload fileSystemUpload, User authenticatedUser) {
        return null;
    }

    public List<FileSystemUploadPreflightResponse> preflightUploadFileSystemItem(long rootItemId, List<FileSystemUpload> uploads, User authenticatedUser) {
        FileSystemEntity uploadParent = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == uploadParent)
            throw new FileSystemItemCouldNotBeUploadedException();

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.CHANGE)
                || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(uploadParent, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemCouldNotBeUploadedException();

        HashMap<String, PreflightResponse> responses = new HashMap<>();

        for (FileSystemUpload upload : uploads) {
            String[] paths = fileSystemHelperService.splitPathIntoEnitityPaths(upload.getPath(), uploadParent.getPath());

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
                }

                // TODO: build the response for the folder

            }
            log.debug("here is this file {}", upload);
            // here is the file.
            PreflightResponse fileResponse = handlePreflightFile(upload.getPath(), upload.getName(), responses, uploadParent, authenticatedUser);

            // TODO: build the response and add it to list
        }
        return null;
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
            List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndName(Arrays.asList(childrenIdsLong), currentFolderName);

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
            List<FileSystemEntity> alreadyExistingFilesWithSameName = fileSystemRepository.findAllByFileSystemIdInAndName(Arrays.asList(childrenIdsLong), currentFileName);

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