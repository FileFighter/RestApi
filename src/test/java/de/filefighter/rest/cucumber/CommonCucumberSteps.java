package de.filefighter.rest.cucumber;

import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static de.filefighter.rest.domain.token.business.AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS;

public class CommonCucumberSteps extends CucumberIntegrationTest {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final FileSystemRepository fileSystemRepository;

    @Autowired
    public CommonCucumberSteps(UserRepository userRepository, AccessTokenRepository accessTokenRepository, FileSystemRepository fileSystemRepository) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.fileSystemRepository = fileSystemRepository;
    }

    @Given("database is empty")
    public void databaseIsEmpty() {
        userRepository.deleteAll();
        accessTokenRepository.deleteAll();
        fileSystemRepository.deleteAll();
    }

    @And("user {long} exists")
    public void userExists(long userId) {
        userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .build());
    }

    @And("user {long} has access token {string}")
    public void userHasAccessToken(long userId, String accessTokenValue) {
        accessTokenRepository.save(AccessTokenEntity
                .builder()
                .userId(userId)
                .value(accessTokenValue)
                .validUntil(Instant.now().getEpochSecond() + ACCESS_TOKEN_DURATION_IN_SECONDS)
                .build());
    }

    @And("user with id {long} exists and has username {string}, password {string} and refreshToken {string}")
    public void userWithIdExistsAndHasUsernamePasswordAndRefreshToken(long userId, String username, String password, String refreshTokenValue) {
        userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .username(username)
                .password(password)
                .refreshToken(refreshTokenValue)
                .build());
    }

    // file / folder
    @Given("{string} exists with id {long} and path {string}")
    public void existsWithIdAndPath(String fileOrFolder, long fsItemId) {
        if(fileOrFolder.equals("file")){
            fileSystemRepository.save(FileSystemEntity
                    .builder()
                    .isFile(true)
                    .id(fsItemId)
                    .create());
        }else if(fileOrFolder.equals("folder")){
            fileSystemRepository.save(FileSystemEntity
                    .builder()
                    .isFile(false)
                    .id(fsItemId)
                    .create());
        }else{
            throw new IllegalArgumentException("Found not valid string for FileOrFolder in Steps file.");
        }
    }

    @And("user {long} is owner of file or folder with id {long}")
    public void userIsOwnerOfFileOrFolderWithId(long userId, long fsItemId) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findById(fsItemId);
        if(null == fileSystemEntity){
            throw new IllegalArgumentException("FileSystemEntity was null.");
        }

        fileSystemEntity.setCreatedByUserId(userId);
        fileSystemRepository.save(fileSystemEntity);
    }

    //key: value for json type response.
    @Then("response contains key {string} and value {string}")
    public void responseContainsKeyAndValue(String key, String value) {
    }

    @And("response contains the user with id {long}")
    public void responseContainsTheUserWithId(long userId) {
    }

    @Then("response status code is {int}")
    public void responseStatusCodeIs(int httpStatusCode) {
    }

}
