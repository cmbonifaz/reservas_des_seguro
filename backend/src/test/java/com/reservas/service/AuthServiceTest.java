package com.reservas.service;

import com.reservas.dto.LoginRequest;
import com.reservas.dto.LoginResponse;
import com.reservas.entity.Usuario;
import com.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioMock;

    @BeforeEach
    public void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setNombre("Test Admin");
        usuarioMock.setEmail("admin@test.com");
        usuarioMock.setPassword("encodedPassword");
        usuarioMock.setRol(Usuario.Rol.ADMINISTRADOR);
        usuarioMock.setActivo(true);
    }

    @Test
    public void login_conCredencialesValidas_retornaTokenYDatosDelUsuario() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "validPassword");
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("validPassword", "encodedPassword")).thenReturn(true);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getToken().startsWith("admin-token-"));
        assertEquals("Test Admin", response.getNombre());
        assertEquals("admin@test.com", response.getEmail());
        assertEquals(Usuario.Rol.ADMINISTRADOR.getDisplayName(), response.getRol());

        verify(usuarioRepository, times(1)).findByEmail("admin@test.com");
        verify(passwordEncoder, times(1)).matches("validPassword", "encodedPassword");
    }

    @Test
    public void login_conPasswordIncorrecto_lanzaExcepcion() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "invalidPassword");
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("invalidPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Contraseña incorrecta", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("admin@test.com");
        verify(passwordEncoder, times(1)).matches("invalidPassword", "encodedPassword");
    }
}
