package com.reservas.service;

import com.reservas.dto.UsuarioRequest;
import com.reservas.entity.Usuario;
import com.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_conEmailYaRegistrado_lanzaExcepcion() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Nuevo Empleado");
        request.setEmail("existente@reservas.com");
        request.setTelefono("0999999999");
        request.setRol(Usuario.Rol.EMPLEADO);

        when(usuarioRepository.existsByEmail("existente@reservas.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.crearUsuario(request));
        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_conPassword_laGuardaCifrada() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Nuevo Empleado");
        request.setEmail("nuevo@reservas.com");
        request.setTelefono("0999999999");
        request.setRol(Usuario.Rol.EMPLEADO);
        request.setPassword("claveSegura");

        when(usuarioRepository.existsByEmail("nuevo@reservas.com")).thenReturn(false);
        when(passwordEncoder.encode("claveSegura")).thenReturn("hash-resultante");
        when(usuarioRepository.countReservasGestionadasByUsuario(any())).thenReturn(0);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setIdUsuario(9L);
            return usuario;
        });

        usuarioService.crearUsuario(request);

        verify(passwordEncoder).encode("claveSegura");
        verify(usuarioRepository).save(argThat(usuario -> "hash-resultante".equals(usuario.getPassword())));
    }
}
