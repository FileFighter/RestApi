package de.filefighter.rest.domain.filesystem.type;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FileSystemTypeRepository {

    public FileSystemType findFileSystemTypeById(long id) {
        FileSystemType[] values = FileSystemType.values();
        for (FileSystemType type : values) {
            if (type.getId() == id) return type;
        }
        throw new IllegalArgumentException("No FileSystemType found for id: " + id);
    }

    // https://www.sitepoint.com/mime-types-complete-list/
    public FileSystemType parseMimeType(String mimeType) {
        FileSystemType returnValue = FileSystemType.UNDEFINED;

        // java sucks. Lets do kotlin next time.
        if (null == mimeType) {
            log.warn("Found null in mimeType");
            return FileSystemType.UNDEFINED;
        }

        if (mimeType.contains("text/")) {
            returnValue = FileSystemType.TEXT;
        } else if (mimeType.contains("video/")) {
            returnValue = FileSystemType.VIDEO;
        } else if (mimeType.contains("audio/")) {
            returnValue = FileSystemType.AUDIO;
        } else if (mimeType.contains("image/")) {
            returnValue = FileSystemType.IMAGE;
        } else if (mimeType.contains("application/")) {
            returnValue = FileSystemType.APPLICATION;
        }

        return returnValue;
    }
}
