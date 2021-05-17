package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static de.filefighter.rest.configuration.RestConfiguration.*;


public class FileSystemDownloadSteps extends RestApplicationIntegrationTest {

    @ParameterType("\\[([0-9, ]*)\\]")
    public List<Long> listOfLongs(String longs) {
        return Arrays.stream(longs.split(", ?"))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @When("the user with token {string} wants to download the fileSystemItems with Ids {listOfLongs}")
    public void theUserWithTokenWantsToDownloadTheFileSystemItemsWithIds(String accessToken, List<Long> ids) {
        // parse ids
        StringBuilder idParamString = new StringBuilder("?ids=");
        for (Long l : ids) {
            idParamString.append(l);
        }

        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + FS_BASE_URI + "download" + idParamString;

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, url, header);
    }
}
