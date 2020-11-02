package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.rest.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.security.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

class PermissionRestControllerUnitTest {

    private final PermissionRestServiceInterface permissionRestService = mock(PermissionRestService.class);
    private PermissionRestController permissionRestController;

    @BeforeEach
    void setUp() {
        permissionRestController = new PermissionRestController(permissionRestService);
    }

    @Test
    void getPermissionSetForFileOrFolder() {
        String token = "token";
        long id = 420;
        User dummyUser = User.builder().create();
        ResponseEntity<PermissionSet> expected = new ResponseEntity<>(new PermissionSet(null, null, new User[]{dummyUser}, null), OK);

        when(permissionRestService.getPermissionSetByIdAndToken(id, token)).thenReturn(expected);

        ResponseEntity<PermissionSet> actual = permissionRestController.getPermissionSetForFileOrFolder(420, token);

        assertEquals(expected, actual);
    }

    @Test
    void addUsersOrGroupsToPermissionSetForFileOrFolder() {
        String token = "token";
        long id = 420;
        ServerResponse dummyResponse = new ServerResponse("ok", "baum");
        PermissionRequest dummyRequest = new PermissionRequest();

        ResponseEntity<ServerResponse> expected = new ResponseEntity<>(dummyResponse, OK);


        when(permissionRestService.addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(dummyRequest, id, token)).thenReturn(expected);

        ResponseEntity<ServerResponse> actual = permissionRestController.addUsersOrGroupsToPermissionSetForFileOrFolder(420, dummyRequest, token);

        assertEquals(expected, actual);
    }

    @Test
    void removeUsersOrGroupsFromPermissionSetForFileOrFolder() {
        String token = "token";
        long id = 420;
        ServerResponse dummyResponse = new ServerResponse("ok", "baum");
        PermissionRequest dummyRequest = new PermissionRequest();

        ResponseEntity<ServerResponse> expected = new ResponseEntity<>(dummyResponse, OK);


        when(permissionRestService.removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(dummyRequest, id, token)).thenReturn(expected);

        ResponseEntity<ServerResponse> actual = permissionRestController.removeUsersOrGroupsFromPermissionSetForFileOrFolder(420, dummyRequest, token);

        assertEquals(expected, actual);
    }
}