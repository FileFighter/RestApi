package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewFolderContentsSteps extends RestApplicationIntegrationTest {

    private final ObjectMapper objectMapper;

    @Autowired
    public ViewFolderContentsSteps(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @When("user with token {string} wants to see the content of folder with path {string}")
    public void userWithTokenWantsToSeeTheContentOfFolderWithPath(String accessTokenValue, String path) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;
        String url = BASE_API_URI + FS_BASE_URI + "contents";

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authHeaderString);
        header.put(FS_PATH_HEADER, path);

        executeRestApiCall(HttpMethod.GET, url, header);
    }

    @And("the response contains the file with fileSystemId {long} and name {string}")
    public void theResponseContainsTheFileWithIdAndName(long fsItemId, String name) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            if (node.get("fileSystemId").asLong() == fsItemId &&
                    node.get("name").asText().equals(name) &&
                    !node.get("type").asText().equals("FOLDER"))
                found = true;
        }
        assertTrue(found);
    }

    @And("the response contains the file with name {string}")
    public void theResponseContainsTheFileWithName(String name) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            if (node.get("name").asText().equals(name) &&
                    !node.get("type").asText().equals("FOLDER"))
                found = true;
        }
        assertTrue(found);
    }

    @And("the response contains an empty list for files and folders")
    public void theResponseContainsAnEmptyListForFilesAndFolders() throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode())
            throw new AssertionError("Response was not an Array or empty.");

        assertTrue(rootNode.isEmpty());
    }

    @And("the response does not contains the file with fileSystemId {long} and name {string}")
    public void theResponseNotContainsTheFileWithFileSystemIdAndName(long fsItemId, String name) throws JsonProcessingException {

        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode())
            throw new AssertionError("Response was not an Array.");

        // if it's empty is also okay.
        if (!rootNode.isEmpty()) {
            boolean found = false;
            for (JsonNode node : rootNode) {
                if (node.get("fileSystemId").asLong() == fsItemId && node.get("name").asText().equals(name)) {
                    found = true;
                }
            }
            assertFalse(found);
        }
    }

    @And("the response contains the folder with fileSystemId {long} and name {string}")
    public void theResponseContainsTheFolderWithFileSystemIdAndName(long fileSystemId, String name) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            if (node.get("fileSystemId").asLong() == fileSystemId &&
                    node.get("name").asText().equals(name) &&
                    node.get("type").asText().equals("FOLDER"))
                found = true;
        }
        assertTrue(found);
    }
}
