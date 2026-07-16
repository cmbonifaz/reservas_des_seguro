package com.reservas.service;

import com.reservas.dto.ReservaRequest;
import com.reservas.dto.ReservaResponse;
import com.reservas.entity.Reserva;
import com.reservas.entity.Servicio;
import com.reservas.entity.Usuario;
import com.reservas.repository.ReservaRepository;
import com.reservas.repository.ServicioRepository;
import com.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void crearReserva_conEmailNuevo_creaUsuarioYGuardaReservaComoPendiente() {
        // Arrange
        ReservaRequest request = new ReservaRequest();
        request.setNombre("Nuevo Cliente");
        request.setEmail("nuevo_cliente@reservas.com");
        request.setTelefono("0987654321");
        request.setIdServicio(1L);
        request.setFecha(LocalDate.of(2026, 8, 20));
        request.setHora(LocalTime.of(10, 0));
        request.setObservaciones("Prueba");

        // Mock: el usuario no existe, por lo que se debe crear uno nuevo.
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        
        // Mock de guardar el nuevo usuario
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario user = invocation.getArgument(0);
            user.setIdUsuario(2L);
            return user;
        });

        // Mock de obtener el servicio
        Servicio servicio = new Servicio();
        servicio.setIdServicio(1L);
        servicio.setNombreServicio("Corte de Cabello");
        servicio.setPrecio(125000);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        // Mock de guardar la reserva
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reserva = invocation.getArgument(0);
            reserva.setIdReserva(10L);
            return reserva;
        });

        // Act
        ReservaResponse response = reservaService.crearReserva(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getIdReserva());
        assertEquals("Pendiente", response.getEstado());
        assertEquals("Nuevo Cliente", response.getNombreCliente());
        assertEquals("nuevo_cliente@reservas.com", response.getEmailCliente());
        assertEquals("Corte de Cabello", response.getNombreServicio());

        // Verificar que se guardó el usuario con rol CLIENTE
        verify(usuarioRepository).save(argThat(usuario -> 
            "Nuevo Cliente".equals(usuario.getNombre()) &&
            "nuevo_cliente@reservas.com".equals(usuario.getEmail()) &&
            Usuario.Rol.CLIENTE.equals(usuario.getRol())
        ));

        // Verificar que se guardó la reserva
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void confirmarReserva_actualizaEstadoAConfirmada() {
        // Arrange
        Long idReserva = 100L;
        Usuario usuario = new Usuario("Cliente", "0981234567", "cliente@reservas.com", Usuario.Rol.CLIENTE);
        Servicio servicio = new Servicio("Masaje Relajante", 300000, "Masaje");
        
        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva);
        reserva.setUsuario(usuario);
        reserva.setServicio(servicio);
        reserva.setFecha(LocalDate.of(2026, 8, 20));
        reserva.setHora(LocalTime.of(15, 0));
        reserva.setEstado("Pendiente");

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva r = invocation.getArgument(0);
            return r;
        });

        // Act
        ReservaResponse response = reservaService.confirmarReserva(idReserva);

        // Assert
        assertNotNull(response);
        assertEquals(idReserva, response.getIdReserva());
        assertEquals("Confirmada", response.getEstado());

        // Verificar que se persistió el cambio de estado
        verify(reservaRepository).save(argThat(r -> 
            idReserva.equals(r.getIdReserva()) && 
            "Confirmada".equals(r.getEstado())
        ));
    }
}
