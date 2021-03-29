package de.filefighter.rest.domain.filesystem.type;

import org.springframework.stereotype.Service;

@Service
public class FileSystemTypeRepository {

    public FileSystemType findFileSystemTypeById(long id) {
        FileSystemType[] values = FileSystemType.values();
        for (FileSystemType type : values) {
            if (type.getId() == id) return type;
        }
        throw new IllegalArgumentException("No FileSystemType found for id: " + id);
    }

    // https://www.sitepoint.com/mime-types-complete-list/
    // TODO: implement and test.
    public FileSystemType parseMimeType(String mimeType) {
        return FileSystemType.UNDEFINED;
    }
}
