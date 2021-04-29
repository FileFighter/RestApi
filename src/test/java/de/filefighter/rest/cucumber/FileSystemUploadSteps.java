package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

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
}
