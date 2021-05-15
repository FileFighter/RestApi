package de.filefighter.rest.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class FIleSystemDownloadSteps {

    @ParameterType("\\[([0-9, ]*)\\]")
    public List<Long> listOfLongs(String integers) {
        return Arrays.stream(integers.split(", ?"))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @When("the user with token {string} wants to download the files with Ids {listOfLongs}")
    public void theUserWithTokenWantsToDownloadTheFilesWithIds(String token,List<Long> Ids) {
    }
}
