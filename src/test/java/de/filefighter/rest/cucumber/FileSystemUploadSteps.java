package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.filesystem.data.dto.upload.CreateNewFolder;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.PreflightResponse;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@Log4j2
public class FileSystemUploadSteps extends RestApplicationIntegrationTest {

    private final ObjectMapper objectMapper;

    @Autowired
    public FileSystemUploadSteps(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @When("the user with token {string} wants to do a preflight containing a file with the name {string}, path {string}, mimeType {string} and size {double} to the folder with the id {long}")
    public void theUserWithTokenWantsToDoAPreflightContainingAFileWithTheNamePathMimeTypeAndSizeToTheFolderWithTheId(String accessToken, String name, String path, String mimeType, double size, long id) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + FS_BASE_URI + id + "/upload/preflight";

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authHeaderString);

        ArrayList<FileSystemUpload> uploadItem = new ArrayList<>();
        uploadItem.add(FileSystemUpload.builder()
                .name(name)
                .path(path)
                .mimeType(mimeType)
                .size(size)
                .build());

        String jsonBody = objectMapper.writeValueAsString(uploadItem);
        executeRestApiCall(HttpMethod.POST, url, header, jsonBody);
    }

    @Then("the response contains a entity with the path {string} that has the preflight response {string}")
    public void theResponseContainsAEntityThatHasThePreflightResponse(String path, String preflightResponse) throws JsonProcessingException {
        PreflightResponse response = Arrays.stream(PreflightResponse.values()).filter(preflight -> preflight.name().equals(preflightResponse))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Didnt't find Preflight Response with name: " + preflightResponse));

        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            if (node.get("path").asText().equals(path)) {
                boolean nameIsValidMatches = node.get("nameIsValid").asBoolean() == response.isNameIsValid();
                boolean nameAlreadyInUseMatches = node.get("nameAlreadyInUse").asBoolean() == response.isNameAlreadyInUse();
                boolean permissionIsSufficientMatches = node.get("permissionIsSufficient").asBoolean() == response.isPermissionIsSufficient();

                if (nameIsValidMatches && nameAlreadyInUseMatches && permissionIsSufficientMatches) {
                    found = true;
                } else {
                    log.debug("Found an entity with matching path but non matching values.");
                }
            }
        }
        Assertions.assertTrue(found);
    }

    @Then("the response contains a entity with the path {string} that has key {string} with value {string}")
    public void theResponseContainsAEntityWithThePathThatHasKeyWithValue(String path, String key, String value) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            if (node.get("path").asText().equals(path) &&
                    node.get(key).asText().equals(value)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    @When("the user with token {string} wants to upload a file with the name {string}, path {string}, mimeType {string} and size {double} to the folder with the id {long}")
    public void theUserWithTokenWantsToUploadAFileWithTheNamePathMimeTypeAndSizeToTheFolderWithTheId(String accessToken, String name, String path, String mimetype, double size, long id) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + FS_BASE_URI + id + "/upload";

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authHeaderString);

        String jsonBody = objectMapper.writeValueAsString(FileSystemUpload.builder()
                .name(name)
                .path(path)
                .mimeType(mimetype)
                .size(size)
                .build());

        executeRestApiCall(HttpMethod.POST, url, header, jsonBody);
    }

    @When("the user with token {string} wants to create a folder with name {string} in the the folder with the id {long}")
    public void theUserWithTokenWantsToCreateAFolderWithNameInTheTheFolderWithTheId(String accessToken, String name, long parentId ) throws JsonProcessingException {

        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + FS_BASE_URI + parentId + "/folder/create";

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authHeaderString);

        String jsonBody = objectMapper.writeValueAsString(CreateNewFolder.builder()
                .name(name)
                .build());

        executeRestApiCall(HttpMethod.POST, url, header, jsonBody);

    }
}
