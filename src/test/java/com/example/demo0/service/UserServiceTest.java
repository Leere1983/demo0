package com.example.demo0.service;

import com.example.demo0.entity.User;
import com.example.demo0.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
    }

    @Test
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Optional<User> found = userService.findById(1L);
        
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Optional<User> found = userService.findByUsername("testuser");
        
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindAll() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));
        
        List<User> users = userService.findAll();
        
        assertEquals(2, users.size());
    }

    @Test
    void testCreateUser() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User created = userService.create(testUser);
        
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
    }

    @Test
    void testCreateUserWithExistingUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> userService.create(testUser));
    }

    @Test
    void testUpdateUser() {
        User updatedUser = new User();
        updatedUser.setUsername("newname");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.update(1L, updatedUser);
        
        assertNotNull(result);
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        
        assertDoesNotThrow(() -> userService.delete(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNonExistentUser() {
        when(userRepository.existsById(99L)).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> userService.delete(99L));
    }
}