package de.filefighter.rest.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class FileSystemDownloadSteps {

    @ParameterType("\\[([0-9, ]*)\\]")
    public List<Long> listOfLongs(String longs) {
        return Arrays.stream(longs.split(", ?"))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @When("the user with token {string} wants to download the fileSystemItems with Ids {listOfLongs}")
    public void theUserWithTokenWantsToDownloadTheFileSystemItemsWithIds(String token,List<Long> Ids) {
    }
}
