package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John Doe", "john@example.com");
        testUserDto = new UserDto(1L, "John Doe", "john@example.com");
        newUserRequest = new NewUserRequest("John Doe", "john@example.com");
    }

    @Test
    void getUsers_ShouldReturnUserList() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findUsersWithIds(any(), any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        List<UserDto> result = userService.getUsers(List.of(1L), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository).findUsersWithIds(List.of(1L), PageRequest.of(0, 10));
    }

    @Test
    void createUser_WithValidData_ShouldReturnUserDto() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(newUserRequest)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.createUser(newUserRequest);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository).existsByEmail(newUserRequest.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowConflictException() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(newUserRequest));
        verify(userRepository).existsByEmail(newUserRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(any());
    }
}

