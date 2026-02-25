package org.example.bakeryapi.user;

import tools.jackson.databind.ObjectMapper;
import org.example.bakeryapi.common.exception.GlobalExceptionHandler;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.dto.UserRequest;
import org.example.bakeryapi.user.dto.UserResponse;
import org.example.bakeryapi.user.exception.EmailAlreadyExistsException;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUser_existingId_returnsUser() throws Exception {
        UserResponse user = new UserResponse(1L, "test@example.com", Role.USER, true);
        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getById(1L);
    }

    @Test
    void getByEmail_existingEmail_returnsUser() throws Exception {
        UserResponse user = new UserResponse(1L, "test@example.com", Role.USER, true);
        when(userService.getByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/users")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getByEmail("test@example.com");
    }

    @Test
    void createUser_validRequest_createsUser() throws Exception {
        UserRequest request = new UserRequest("new@example.com", "pass", Role.USER);
        UserResponse createdUser = new UserResponse(1L, request.email(), request.role(), true);

        when(userService.create(any(UserRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).create(request);
    }

    @Test
    void disableUser_callsService() throws Exception {
        mockMvc.perform(patch("/users/1/disable"))
                .andExpect(status().isNoContent());

        verify(userService).disableUser(1L);
    }

    @Test
    void enableUser_callsService() throws Exception {
        mockMvc.perform(patch("/users/1/enable"))
                .andExpect(status().isNoContent());

        verify(userService).enableUser(1L);
    }

    @Test
    void getUser_nonExistingId_returnsNotFound() throws Exception {
        when(userService.getById(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getById(999L);
    }

    @Test
    void getByEmail_nonExistingEmail_returnsNotFound() throws Exception {
        when(userService.getByEmail("missing@example.com"))
                .thenThrow(new UserNotFoundException("missing@example.com"));

        mockMvc.perform(get("/users")
                        .param("email", "missing@example.com"))
                .andExpect(status().isNotFound());

        verify(userService).getByEmail("missing@example.com");
    }

    @Test
    void createUser_existingEmail_returnsConflict() throws Exception {
        UserRequest request = new UserRequest("existing@example.com", "pass", Role.USER);
        when(userService.create(any(UserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).create(request);
    }

    @Test
    void createUser_invalidRequest_returnsBadRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest("", null, null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

}


