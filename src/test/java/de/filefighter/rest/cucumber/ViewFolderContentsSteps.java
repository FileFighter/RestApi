package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@Log4j2
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
        Assertions.assertTrue(found);
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
        Assertions.assertTrue(found);
    }

    @And("the response contains an empty list for files and folders")
    public void theResponseContainsAnEmptyListForFilesAndFolders() throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode())
            throw new AssertionError("Response was not an Array or empty.");

        Assertions.assertTrue(rootNode.isEmpty());
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
            Assertions.assertFalse(found);
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
        Assertions.assertTrue(found);
    }

    @And("the response contains the folder with name {string}")
    public void theResponseContainsTheFolderWithName(String name) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            String jName = node.get("name").asText();
            String jType = node.get("type").asText();
            log.debug("Check {} : {}", jType, "FOLDER");
            log.debug("Check {} : {}", jName, name);

            if (jName.equals(name) && jType.equals("FOLDER")) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    @And("the response contains the item with path {string} and name {string} and mimeType {string} and type {string} and size {double}")
    public void theResponseContainsTheFileWithPathAndNameAndMimeTypeAndTypeAndSize(String path, String name, String mimeType, String enumType, double size) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            String jName = node.get("name").asText();
            String jPath = node.get("path").asText();
            String jMimeType = node.get("mimeType").asText();
            String jType = node.get("type").asText();
            double jSize = node.get("size").asDouble();
            log.debug("Check {} : {}", jName, name);
            log.debug("Check {} : {}", jPath, path);
            log.debug("Check {} : {}", jMimeType, mimeType);
            log.debug("Check {} : {}", jType, enumType);
            log.debug("Check {} : {}", jSize, size);

            if ((jName.equals(name) &&
                    jPath.equals(path) &&
                    jMimeType.equals(mimeType) &&
                    jType.equals(enumType) &&
                    jSize == size)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    @And("the response contains the file with name {string} and size {double}")
    public void theResponseContainsTheFileWithNameAndSize(String name, double size) throws JsonProcessingException {
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(latestResponse.getBody());
        if (!rootNode.isContainerNode() || rootNode.isEmpty())
            throw new AssertionError("Response was not an Array or empty.");

        boolean found = false;
        for (JsonNode node : rootNode) {
            String jName = node.get("name").asText();
            double jSize = node.get("size").asDouble();
            log.debug("Check {} : {}", jSize, size);
            log.debug("Check {} : {}", jName, name);

            if (jName.equals(name) && jSize == size) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }
}
