package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.dto.File;
import de.filefighter.rest.domain.filesystem.data.dto.Folder;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.user.data.dto.User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FileSystemBusinessService {

    public FileSystemBusinessService() {

    }

    public static FolderContents getContentsOfFolder(String path, User authenticatedUser) {
        FolderContents folderContents;
        switch (path) {
            case "/":
                folderContents = FolderContents.builder()
                        .files(new File[]{new File(0, "Passwords.crypt", 420, 0, 1897550098, FileSystemType.TEXT, null)})
                        .folders(new Folder[]{
                                new Folder(1, "/dhbw", "DHBW", 87568438, 0, Instant.now().getEpochSecond(), null),
                                new Folder(2, "/homework", "Homework", 1897557698, 0, 1577836800, null)
                        })
                        .build();
                break;
            case "/bla":
                folderContents = FolderContents.builder()
                        .folders(new Folder[]{
                                new Folder(3, "/se", "SE", 18975576, 0, 1601148846, null),
                                new Folder(4, "/ti-3", "TI-3", 69, 0, 1599936800, null)
                        })
                        .files(new File[]{
                                new File(4, "WhatIsThis", 42, 0, 153354, FileSystemType.UNDEFINED, null),
                                new File(5, "HerrMeyerSieWissenDochImmerAlles.mp3", 27565846, 0, 1599147368, FileSystemType.AUDIO, null),
                                new File(6, "cucumberTestsWorkProve.mp4", 224850446, 0, 1602047368, FileSystemType.VIDEO, null),
                                new File(7, "WeirdScreenshot.jpg", 4866848, 0, 1599949968, FileSystemType.PICTURE, null),
                                new File(8, "ILikeThisFileType.md", 96643, 0, 1598888868, FileSystemType.TEXT, null),
                                new File(9, "MyFirstWebsite.html", 861858, 0, 1601584968, FileSystemType.TEXT, null),
                                new File(10, "JavaScriptFTW.js", 176643, 0, 1597388868, FileSystemType.TEXT, null),
                                new File(11, "TheyWillNeverKnow.crypt", 75896643, 0, 1600188868, FileSystemType.UNDEFINED, null),
                                new File(12, "Opportunismus und Repression.pdf", 4826643, 0, 1589998868, FileSystemType.PDF, null),
                                new File(13, "ProfsINeedToBribeOrCharm.txt", 153, 0, Instant.now().getEpochSecond(), FileSystemType.TEXT, null),
                                new File(14, "FinishedFileFighterBE.java", 846846643, 0, 1624752000, FileSystemType.TEXT, null),
                        })
                        .build();
                break;
            case "/fasel":
                folderContents = FolderContents.builder()
                        .files(new File[]{new File(15, "DummyFileInFasel.txt", 420, 0, Instant.now().getEpochSecond(), FileSystemType.TEXT, null)})
                        .folders(new Folder[]{new Folder(16, "/fasel/johnwishesforasecretchamber", "JohnWishesForASecretChamber", 2, 0, Instant.now().getEpochSecond(), null)})
                        .build();
                break;
            case "/fasel/johnwishesforasecretchamber":
                folderContents = FolderContents.builder()
                        .folders(new Folder[]{new Folder(17, "/fasel/johnwishesforasecretchamber/empty", "Empty", 0, 0, Instant.now().getEpochSecond(), null)})
                        .build();
                break;
            case "/fasel/johnwishesforasecretchamber/empty":
                folderContents = FolderContents.builder().build();
                break;
            default:
                throw new FileSystemContentsNotAccessibleException();

        }
        return folderContents;
    }
}
